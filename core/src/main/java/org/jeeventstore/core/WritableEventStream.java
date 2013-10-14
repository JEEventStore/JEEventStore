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

package org.jeeventstore.core;

import java.io.Serializable;
import java.util.UUID;

/**
 * A writable event stream is an event stream to which new events
 * can be appended and which can commit the changes to the event store.
 * Since the existing events in an event stream are not exposed to a client
 * of a writable event stream, a writable event stream can quickly be created
 * without querying the database for the existing events, which improves
 * performance when events are to be appended to the event stream.
 * 
 * @author Alexander Langer
 */
public interface WritableEventStream extends VersionedEventStream {

    /**
     * Append the given event to the event stream.
     * @param event The event to be appended.
     */
    void append(Serializable event);

    /**
     * Commits the changes to durable storage.
     * @param commitId The value which uniquely identifies the commit.
     * @throws DuplicateCommitException
     * @throws ConcurrencyException
     */
    void commit(UUID commitId) 
            throws DuplicateCommitException, ConcurrencyException;

    /**
     * Clear all uncommitted changes.
     */
    void rollback();
    
}
