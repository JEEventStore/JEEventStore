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

/**
 * Responsible for notifying interested listeners about committed changes to the event store.
 */
public interface EventStoreCommitNotifier {

    /**
     * Notifies all registered listeners about the committed {@link ChangeSet}.
     * Non-delivered notifications are not persisted during application
     * restarts to avoid expensive 2-phase-commits.  If the server crashes or
     * stops while the notification is in progress, clients are expected to 
     * recover manually on the next server startup (e.g., by replaying the full
     * event store on application startup using {@link EventStorePersistence#allChanges}).
     * 
     * @param changeSet  the change set that has been committed, not null
     */
    void notifyListeners(ChangeSet changeSet);

    /**
     * Adds an interested listener.
     * The listener must not be registered already.
     * 
     * @param listener  the listener that shall be added, not null
     */
    void addListener(EventStoreCommitListener listener);

    /**
     * Removes a registered listener.
     * The listener must be registered.
     * 
     * @param listener  the listener that shall be removed, not null
     */
    void removeListener(EventStoreCommitListener listener);
    
}
