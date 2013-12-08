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

import java.io.Serializable;

/**
 * An event stream to which new events can be appended and committed
 * to the event store.
 */
public interface WritableEventStream extends VersionedEventStream {

    /**
     * Append the given event to the event stream.
     * 
     * @param event  the event to be appended, not null
     */
    void append(Serializable event);

    /**
     * Commits the changes to durable storage.
     * 
     * @param commitId  the value that uniquely identifies the commit, null returns empty-handed
     * @throws DuplicateCommitException  if a commit with the same id already exists in the bucket
     * @throws ConcurrencyException  if a {@ChangeSet} with the same streamVersion already exists
     *   in the durable storage.
     */
    void commit(String commitId) 
            throws DuplicateCommitException, ConcurrencyException;

    /**
     * Clear all uncommitted changes.
     */
    void rollback();
    
}
