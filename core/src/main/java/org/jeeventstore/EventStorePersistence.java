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

package org.jeeventstore;

import java.util.Iterator;

/**
 * Persistence provider for the event store.
 */
public interface EventStorePersistence {

    /**
     * Tests whether an event stream with identifier {@code streamId} exists
     * in the bucket identified by {@code bucketId}.
     * 
     * @param bucketId  the identifier of the bucket to which the stream belongs, not null
     * @param streamId  the identifier of the stream that is tested for existence, not null
     * @return  whether the specified stream exists
     */
    boolean existsStream(String bucketId, String streamId);
    
    /**
     * Gets an iterator to all changes persisted to the given bucket.
     * The order of events is consistent on a per-stream basis, but no
     * other ordering guarantee can be given.
     * <p>
     * Some persistence implementations support deferred ("streamed") loading
     * of events from the persistence store.
     * It depends on the persistence implementation whether {@link #allChanges}
     * requires an open transaction and requires {@link Iterator#next()} to be
     * called within this open transaction.
     * 
     * @param bucketId  the identifier of the bucket from which the changes are fetched, not null
     * @return  the iterator to the changes
     */
    Iterator<ChangeSet> allChanges(String bucketId);

    /**
     * Gets an iterator to all changes ({@link ChangeSet}s) between version
     * {@code minVersion} (exclusive) and version {@code maxVersion} (inclusive)
     * in the specified event stream.
     * The order of the respective {@link ChangeSet#streamVersion} is guaranteed
     * to be strictly increasing.
     * <p>
     * Some persistence implementations support deferred ("streamed") loading
     * of events from the persistence store.
     * It depends on the persistence implementation whether {@link #getFrom}
     * requires an open transaction and requires {@link Iterator#next()} to be
     * called within this open transaction.
     * 
     * @param bucketId  the identifier of the bucket to which the stream belongs, not null
     * @param streamId  the identifier of the stream that is to be retrieved from, not null
     * @param minVersion  the minimum version (exclusive) of the {@link ChangeSet} to fetch
     * @param maxVersion  the maximum version (inclusive) of the {@link ChangeSet} to fetch
     * @return  the iterator to the {@link ChangeSet}s
     */
    Iterator<ChangeSet> getFrom(
            String bucketId,
            String streamId,
            long minVersion,
            long maxVersion);

    /**
     * Persists the given {@link ChangeSet} to the durable storage.
     * 
     * @param changeSet  the changes to be persisted, not null
     * @throws ConcurrencyException  if there already exists a {@link ChangeSet}
     *  within the respective stream that has the same {@link ChangeSet#streamVersion() version}
     *  (Not supported with all implementations)
     * @throws DuplicateCommitException  if there already exists a {@link ChangeSet}
     *  with the same id (Not supported with all implementations)
     */
    void persistChanges(ChangeSet changeSet)
            throws ConcurrencyException, DuplicateCommitException;
    
}