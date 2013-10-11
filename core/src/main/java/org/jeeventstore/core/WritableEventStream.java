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
import org.jeeventstore.core.persistence.EventStorePersistence;
import org.jeeventstore.core.store.ConcurrencyException;
import org.jeeventstore.core.store.DuplicateCommitException;

/**
 * A writable event stream is an event stream to which new events
 * can be appended and which can commit the changes to the event store.
 * This can be used to append events to an event stream without an extra
 * database round trip required for loading the events from the event store.
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
     */
    void commit(UUID commitId) 
            throws DuplicateCommitException, ConcurrencyException;

    /**
     * Clear all uncommitted changes.
     */
    void rollback();
    
}
