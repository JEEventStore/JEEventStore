/*
 * Copyright (c) 2013 Red Rainbow IT Solutions GmbH, Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.jeeventstore.store;

import org.jeeventstore.StreamNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jeeventstore.EventStorePersistence;
import org.jeeventstore.ConcurrencyException;
import org.jeeventstore.DuplicateCommitException;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.ReadWriteEventStream;
import org.jeeventstore.ReadableEventStream;
import org.jeeventstore.WritableEventStream;

/**
 * An event stream that uses optimistic locking.
 * Supports reading and writing.
 */
final class OptimisticEventStream 
        implements ReadWriteEventStream {

    private static final Logger log = Logger.getLogger(OptimisticEventStream.class.getName());

    private final String bucketId;                      // the ID of the bucket this streams belongs to
    private final String streamId;                      // the ID of this stream within the bucket
    private long version = 0l;                          // the version of this stream

    private final List<ChangeSet> committedChanges;     // change sets that have been persisted
    private final List<Serializable> appendedEvents;    // events that have been appended but not yet persisted

    private final EventStorePersistence persistence;    // the persistence service

    private OptimisticEventStream(
            String bucketId,
            String streamId, 
            long version,
            EventStorePersistence persistence) {
        
        this.bucketId = bucketId;
        this.streamId = streamId;
        this.version = version;
        this.committedChanges = new ArrayList<>();
        this.appendedEvents = new ArrayList<>();
        this.persistence = persistence;
    }

    /**
     * Factory method to create a new writable event stream supporting optimistic locking.
     * Does not query the persistence layer upon creation.
     * 
     * @param bucketId  the identifier of the bucket the stream belongs to
     * @param streamId  the identifier of the stream
     * @param version   the version of the stream
     * @param persistence  the persistence layer to which changes are to be committed
     * @return  the stream
     */
    protected static WritableEventStream createWritable(
            String bucketId,
            String streamId, 
            long version,
            EventStorePersistence persistence) {
        
        return new OptimisticEventStream(bucketId, streamId, version, persistence);
    }

    /**
     * Factory method to create a new read and writable event stream supporting optimistic locking.
     * Queries the persistence layer upon creation to populate the stream
     * 
     * @param bucketId  the identifier of the bucket the stream belongs to
     * @param streamId  the identifier of the stream
     * @param version   the version of the stream
     * @param persistence  the persistence layer to which changes are to be committed
     * @return  the stream
     */
    protected static ReadWriteEventStream createReadWritable(
            String bucketId,
            String streamId, 
            long version,
            EventStorePersistence persistence) {

        OptimisticEventStream oes = new OptimisticEventStream(bucketId, streamId, 0, persistence);
        oes.populateToVersion(version);
        if (oes.committedChanges.isEmpty())
            throw new StreamNotFoundException();
        return oes;
    }

    /**
     * Factory method to create a new readable event stream.
     * Queries the persistence layer upon creation to populate the stream.
     * 
     * @param bucketId  the identifier of the bucket the stream belongs to
     * @param streamId  the identifier of the stream
     * @param version   the version of the stream to be read
     * @param persistence  the persistence layer to which changes are to be committed
     * @return   the stream
     */
    protected static ReadableEventStream createReadable(
            String bucketId,
            String streamId, 
            long version,
            EventStorePersistence persistence) {
        
        return createReadWritable(bucketId, streamId, version, persistence);
    }

    @Override
    public String bucketId() {
        return this.bucketId;
    }

    @Override
    public String streamId() {
        return this.streamId;
    }

    @Override
    public long version() {
        return this.version;
    }

    @Override
    public Iterator<Serializable> events() {
        if (this.version != this.committedChanges.size())
            throw new IllegalStateException("Cannot retrieve events from unpopulated stream");
        return new EventsIterator(this.committedChanges.iterator());
    }

    @Override
    public void append(Serializable event) {
        if (event == null)
            return;
        log.log(Level.FINE, "Appending event: {0}", event);
        this.appendedEvents.add(event);
    }

    @Override
    public void commit(UUID commitId) 
            throws DuplicateCommitException, ConcurrencyException {
        if (commitId == null)
            throw new IllegalArgumentException("commitId must not be null");
        log.log(Level.FINE, "Attempting to commit changes: {0}", this.streamId);
        if (!hasChanges())
            return;
        this.persistChanges(commitId);
    }

    @Override
    public void rollback() {
        this.clearChanges();
    }

    private void persistChanges(UUID commitId)
            throws ConcurrencyException, DuplicateCommitException {

        long newversion = version + 1;
        ChangeSet changes = new DefaultChangeSet(
                bucketId,
                streamId,
                newversion,
                commitId,
                appendedEvents);
        List<ChangeSet> clist = new ArrayList<>();
        clist.add(changes);

        log.log(Level.FINE, "Persisting commit #{3} {0} into stream {4}/{1}",
                new Object[]{commitId, streamId, newversion, bucketId});
        persistence.persistChanges(changes);
        this.populateWith(clist.iterator());
        this.clearChanges();
    }

    private void clearChanges() {
        log.log(Level.FINE, "clearing uncommitted changes in stream: {0}", streamId);
        this.appendedEvents.clear();
    }

    /**
     * Queries the persistence layer to populate the stream up until version
     * {@code version}.
     * 
     * @param version  the maximum version to retrieve
     */
    protected void populateToVersion(long version) {
        Iterator<ChangeSet> commits = persistence.getFrom(
                bucketId, streamId, this.version, version);
        this.populateWith(commits);
    }

    private void populateWith(Iterator<? extends ChangeSet> changes) {
        while (changes.hasNext()) {
            ChangeSet changeSet = changes.next();
            if (this.version + 1 != changeSet.streamVersion()) {
                String msg = String.format(
                        "Unexpected stream version: %d in stream %s, current is %d",
                        changeSet.streamVersion(),
                        streamId,
                        this.version);
                log.severe(msg);
                throw new IllegalStateException(msg);
            }
            this.committedChanges.add(changeSet);
            this.version++;
        }
    }

    private boolean hasChanges() {
        return !this.appendedEvents.isEmpty();
    }
    
}