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
import java.io.Serializable;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJBException;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.TimedObject;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.EventStoreCommitListener;

/**
 * An asynchronous event store commit notifier.
 * Notifies registered listeners asynchronously in a different thread, i.e.,
 * the {@link #notifyListeners} method does not block.  The notifier begins to
 * notify listeners when the transaction surrounding the call to {@link #notifyListeners}
 * commits. If this surrounding transaction is rolled back, the listeners are 
 * not notified.  If the transaction from which {@link #notifyListeners} is called
 * is committed, the actual notification is scheduled and will run in a new
 * transaction.  The listeners are therefore notified in a new transaction
 * and cannot cause the commit to the event store to fail.
 * <p>
 * Note: If one of the listeners causes the notification-transaction to fail,
 * the notification is rescheduled and notifies <i>all</i> listeners again
 * until it succeeds to deliver the notification to all listeners. 
 * This is an "at-least-once" guarantee, i.e., for each commit to the event store
 * it is guaranteed that {@link EventStoreCommitListener#receive} is called
 * at least once for this commit.  However, it may be called multiple times
 * per commit, so the listeners should make sure that receiving notifications
 * is an idempotent operation.
 * <p>
 * The retry interval for failed notifications can be configured with the
 * {@code retryInterval} environment entry (in milliseconds) on application
 * servers that support this (supported: JBoss AS, TomEE; not supported: Glassfish (fixed @ 1000 ms)).
 * The default value is 100ms.
 * <p>
 * The notification order is currently in the order of registration, but this is
 * not a guarantee and might change in the future.
 * 
 */
// Note: Glassfish requires {@link TimedObject}.
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class AsyncEventStoreCommitNotifier
        extends AbstractEventStoreCommitNotifier 
        implements EventStoreCommitNotifier, TimedObject, Serializable {

    private static final Logger log = Logger.getLogger(AsyncEventStoreCommitNotifier.class.getName());

    @Resource
    private TimerService timerService;

    @Resource(name = "retryInterval")
    private long retryInterval = 100;

    @Override
    @Lock(LockType.READ)
    public void notifyListeners(ChangeSet changeSet) {
        if (changeSet == null)
            throw new IllegalArgumentException("changeSet must not be null");
        /*
         * Schedule the actual notification into a new thread by creating
         * an create an EJB TimerService interval timer that tries to notify
         * all listeners until all listeners have been notified and then
         * cancels itself.  The timer is created transient (non-persistent),
         * since persistent timers do require a 2-phase-commit to the storage
         * engine(s) and are therefore very expensive.  We do not need persistent
         * timers by the semantics of notifyListeners()
         */
        TimerConfig config = new TimerConfig(changeSet, false);
        timerService.createIntervalTimer(0, retryInterval, config);
    }
    
    /**
     * Handle the EJB TimerService timeout and notify any registered append
     * listeners. The timer will only fire if the transaction in which the timer
     * was created was committed successfully.
     * @param timer 
     */
    @Timeout
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Override
    public void ejbTimeout(Timer timer) {
        try {
            ChangeSet changeSet = (ChangeSet) timer.getInfo();
            this.performNotification(changeSet);
            // if we reach this line, no exception was thrown, i.e., the
            // notification was successful and the timer can be cancelled
            timer.cancel();
        } catch (javax.ejb.NoSuchObjectLocalException e) {
            // "timer has expired or has been cancelled"
            // If the timer was canceled, this is no problem (on busy loads, it
            // might happen that the timer is reschudeled shortly after it was
            // canceled; in either case, canceled means the listeners have been notified.
        }
    }
    
}