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

/**
 * An asynchronous event store commit notifier.
 * Notifies registered listeners asynchronously in a different thread, i.e.,
 * the notifyListeners() method does not block.  The notifier begins to
 * notify listeners when the transaction surrounding the call to notifyListeners()
 * commits. If the surrounding transaction is rolled back, the listeners are 
 * not notified.  If the transaction from which notifyListeners() is called
 * is committed, the actual notification is scheduled and will run in a new
 * transaction.  The listeners are therefore notified in a new transaction
 * and cannot cause the commit to the event store to fail.
 * If one of the listeners causes the notification-transaction to fail,
 * the notification is re-tried automatically until it succeeds.
 * 
 * The notification order is currently in the order of registration, but this is
 * not a guarantee and might change in the future.
 * 
 * Glassfish requires {@link TimedObject}.
 * 
 * @author Alexander Langer
 */
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
        } catch (Exception e) {
            log.info("Error performing notification: " + e.getMessage());
        }
    }
    
}