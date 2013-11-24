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

package org.jeeventstore.notifier;

import org.jeeventstore.EventStoreCommitNotifier;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import org.jeeventstore.ChangeSet;

/**
 * A synchronous event store commit notifier.
 * Notifies interested listeners in the same thread and transaction that also
 * commits the changes to the event store.  Note that therefore listeners may 
 * cause a rollback of the committing transaction.  This might be desired
 * in some circumstances, but if you want to de-couple the committing to
 * the event store from the notification, use the
 * {@link AsyncEventStoreCommitNotifier} instead.
 * <p>
 * The notification order is currently
 * in the order of registration, but this is not a guarantee and might change
 * in the future.
 */
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class SyncEventStoreCommitNotifier
        extends AbstractEventStoreCommitNotifier 
        implements EventStoreCommitNotifier {

    @Override
    @Lock(LockType.READ)
    public void notifyListeners(ChangeSet changeSet) {
        if (changeSet == null)
            throw new IllegalArgumentException("changeSet must not be null");
        this.performNotification(changeSet);
    }
    
}
