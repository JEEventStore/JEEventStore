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

package org.jeeventstore.core.notifier;

import org.jeeventstore.core.ChangeSet;

/**
 * The EventStoreCommitNotifier is responsible for notifying interested
 * listeners about committed changes to the event store.
 * 
 * @author Alexander Langer
 */
public interface EventStoreCommitNotifier {

    /**
     * Notify all registered listeners about the committed ChangeSet.
     * @param changeSet The change set that has been committed.
     * Method must be thread-safe.
     */
    void notifyListeners(ChangeSet changeSet);

    /**
     * Add/register a listener.
     * The listener must not be registered already, otherwise an exception
     * is thrown.
     * @param listener The listener that shall be added.
     * Method must be thread-safe.
     */
    void addListener(EventStoreCommitListener listener);

    /**
     * Remove a registered listener.
     * The listener must be registered, otherwise an exception is thrown.
     * @param listener The listener that shall be removed.
     * Method must be thread-safe.
     */
    void removeListener(EventStoreCommitListener listener);
    
}
