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
 * This interface abstract away any specific persistence implementation
 * from the event store.
 * 
 * @author Alexander Langer
 */
public interface EventStorePersistence {

    /**
     * Test whether an event stream with identifier {@code streamId} exists
     * in the bucket identified by {@code bucketId}.
     * @param bucketId The identifier of the bucket to which the stream belongs.
     * @param streamId The identifier of the stream that is tested for existence.
     * @return true iff the specified stream exists.
     */
    boolean existsStream(String bucketId, String streamId);
    
    /**
     * Load all changes that have been persisted into the given bucket.
     * The order of events is consistent on a per-stream basis, but no
     * other ordering guarantee can be given.
     * 
     * As the {@link ChangeSet}s might be lazy-loaded, {@link Iterator#next()}
     * may only be called within the same transaction that was open in
     * the call to {@link allChanges} (if transactions are used).
     * 
     * @param bucketId The identifier of the bucket from which the changes are fetched.
     * @return An iterator to the changes.
     */
    Iterator<ChangeSet> allChanges(String bucketId);

    /**
     * Get all {@link ChangeSet}s between {@code minVersion} (exclusive) and
     * {@code maxVersion} (inclusive) in the specified event stream.
     * The order of the respective {@link ChangeSet#streamVersion()} is guaranteed
     * to be strictly increasing.
     * 
     * As the {@link ChangeSet}s might be lazy-loaded, {@link Iterator#next()}
     * may only be called within the same transaction that was open in
     * the call to {@code getFrom()} (if transactions are used).
     * 
     * @param bucketId The identifier of the bucket to which the stream belongs.
     * @param streamId The identifier of the stream that is to be retrieved from.
     * @param minVersion The minimum version (exclusive) of the {@link ChangeSet} to fetch.
     * @param maxVersion The maximum version (inclusive) of the {@link ChangeSet} to fetch.
     *      The version of the last {@link ChangeSet} fetched is typically smaller.
     * @return An iterator to the {@link ChangeSet}s in the stream in order of their version.
     */
    Iterator<ChangeSet> getFrom(
            String bucketId,
            String streamId,
            long minVersion,
            long maxVersion);

    /**
     * Persist the given {@link ChangeSet} to the persistence store.
     * @param changeSet The changes to be persisted.
     * @throws ConcurrencyException if there already exists a {@link ChangeSet}
     *  within the respective stream that has the same {@link ChangeSet#streamVersion() version}
     *  (Not supported with all implementations!).
     * @throws DuplicateCommitException if there already exists a {@link ChangeSet}
     *  with the same id (Not supported with all implementations!).
     */
    void persistChanges(ChangeSet changeSet)
            throws ConcurrencyException, DuplicateCommitException;
    
}