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

import org.jeeventstore.EventStore;
import javax.ejb.EJB;
import org.jeeventstore.EventStoreCommitNotifier;
import org.jeeventstore.EventStorePersistence;
import org.jeeventstore.ReadableEventStream;
import org.jeeventstore.StreamNotFoundException;
import org.jeeventstore.WritableEventStream;

/**
 * The EventStoreService orchestrates the creation of event streams.
 * Meant to be deployed as stateless bean.
 * <p>
 * This event store implementation uses an optimistic lock strategy.
 * Since the existing events in an event stream are not exposed to a client
 * of a writable event stream, with an optimistic lock strategy a writable event
 * stream can quickly be created without querying the persistence layer,
 * generally improving performance when events are to be appended to the event stream.
 * For the creation of writable streams, no testing is performed whether a
 * stream with the given identifier already exists in the bucket
 * ({@link #createStream}) or whether the given {@code version} is actually
 * the latest version in the stream.  Commiting the changes to the stream
 * might therefore throw {@link ConcurrencyException}.
 * <p>
 * The bean expects two named EJBs to be injected:
 * <p>
 * The {@code persistence} is of type {@link EventStorePersistence} and
 *    provides the persistence to durable storage.
 * <p>
 * The {@code commitNotifier} is of type {@link EventStoreCommitNotifier} and
 *    provides the notification strategy.
 */
public class OptimisticEventStoreService implements EventStore {

    @EJB(name="commitNotifier")
    private EventStoreCommitNotifier persistenceNotifier;

    @EJB(name="persistence")
    private EventStorePersistence persistence;

    @Override
    public ReadableEventStream openStreamForReading(String bucketId, String streamId)
            throws StreamNotFoundException {
        return this.openStreamForReading(bucketId, streamId, Long.MAX_VALUE);
    }

    @Override
    public ReadableEventStream openStreamForReading(String bucketId, String streamId, long version)
            throws StreamNotFoundException {
        return OptimisticEventStream.createReadable(bucketId, streamId, version, persistence);
    }

    @Override
    public WritableEventStream createStream(String bucketId, String streamId) {
        return this.openStreamForWriting(bucketId, streamId, 0l);
    }

    @Override
    public WritableEventStream openStreamForWriting(
            String bucketId, String streamId, long version) {
        NotifyingPersistenceDecorator deco = new NotifyingPersistenceDecorator(persistence, persistenceNotifier);
        return OptimisticEventStream.createWritable(bucketId, streamId, version, deco);
    }

    @Override
    public boolean existsStream(String bucketId, String streamId) {
        return persistence.existsStream(bucketId, streamId);
    }

}
