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

import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import org.jeeventstore.core.ChangeSet;

/**
 * An asynchronous event store commit notifier.
 * Notifies interested listeners asynchronously in a different thread, i.e.,
 * the notifyListeners() method does not block.  The notifier begins to
 * notify listeners when the transaction surrounding the call to notifyListeners()
 * commits. If the surrounding transaction is rolled back, the listeners are 
 * not notified.  If the transaction from which notifyListeners() is called
 * is committed, the actual notification is scheduled and will run in a new
 * transaction.  The listeners are therefore running in a new transaction.
 * If one of the listeners causes the notification-transaction to fail,
 * the notification is re-tried automatically (as per EJB's TimerService semantics).
 * 
 * The notification order is currently in the order of registration, but this is
 * not a guarantee and might change in the future.
 * 
 * @author Alexander Langer
 */
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class AsyncEventStoreCommitNotifier
        extends AbstractEventStoreCommitNotifier 
        implements EventStoreCommitNotifier {

    private Logger log = Logger.getLogger(AsyncEventStoreCommitNotifier.class.getName());

    @Resource
    private TimerService timerService;

    private long retryInterval = 100;

    @PostConstruct
    public void init() {
        try {
            InitialContext ic = new InitialContext();
            Long tmpRetry = (Long) ic.lookup("java:comp/env/retryInterval");
            if (tmpRetry != null)
                retryInterval = tmpRetry;
        } catch (NameNotFoundException e) {
            log.info("Error looking up retryInterval variable, falling back to default of " + retryInterval + "ms");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Lock(LockType.READ)
    public void notifyListeners(ChangeSet changeSet) {
        scheduleListenerNotification(changeSet);
    }
    
    private void scheduleListenerNotification(ChangeSet changeSet) {
        /*
         * Persistent times are very expensive (requires a 2PC to the
         * timer storage), and we actually do not need them by the semantics
         * of notifyListeners()
         */
        TimerConfig config = new TimerConfig(changeSet, false);
        // create an interval timer that is cancelled once the notifications
        // suceeded
        timerService.createIntervalTimer(0, retryInterval, config);
    }
    
    /**
      * The timer will only fire if the transaction in which the timer was
      * created was committed successfully.
      * @param timer 
      */
    @Timeout
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void notifyAppendListeners(Timer timer) {
        ChangeSet changeSet = (ChangeSet) timer.getInfo();
        this.performNotification(changeSet);
        timer.cancel();
    }
    
}