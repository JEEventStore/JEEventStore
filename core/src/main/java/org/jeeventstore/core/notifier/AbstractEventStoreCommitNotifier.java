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

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Lock;
import javax.ejb.LockType;
import org.jeeventstore.core.ChangeSet;

/**
 * Base implementation implementing common functionality of commit notifiers.
 * 
 * @author Alexander Langer
 */
public abstract class AbstractEventStoreCommitNotifier 
        implements EventStoreCommitNotifier {
    
    private List<EventStoreCommitListener> listeners = new ArrayList<>();

    /**
     * @see EventStoreCommitNotifier#addListener(org.jeeventstore.core.notifier.EventStoreCommitListener) 
     */
    @Override
    @Lock(LockType.WRITE)
    public void addListener(EventStoreCommitListener listener) {
        if (this.listeners.contains(listener))
            throw new IllegalStateException("Listener already listening.");
        this.listeners.add(listener);
    }

    /**
     * @see EventStoreCommitNotifier#removeListener(org.jeeventstore.core.notifier.EventStoreCommitListener) 
     */
    @Override
    @Lock(LockType.WRITE)
    public void removeListener(EventStoreCommitListener listener) {
        if (!this.listeners.contains(listener))
            throw new IllegalStateException("Listener not found.");
        this.listeners.remove(listener);
    }

    /**
     * Perform the actual notification for a given notification.
     * Iterates over all registered listeners and lets them receive the given
     * notification.
     * @param notification The notification that the listeners shall receive.
     */
    protected void performNotification(EventStoreCommitNotification notification) {
        for (EventStoreCommitListener l : listeners)
            l.receive(notification);
    }

    /**
     * Perform the actual notification for a given change set.
     * Iterates over all registered listeners and lets them receive a
     * notification for the given change set.
     * @param changeSet The change set that listeners shall be notified of.
     */
    protected void performNotification(ChangeSet changeSet) {
        EventStoreCommitNotification notification = new DefaultCommitNotification(changeSet);
        this.performNotification(notification);
    }

}
