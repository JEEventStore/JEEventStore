/*
 * Copyright (c) 2013-2014 Red Rainbow IT Solutions GmbH, Germany
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
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;
import org.jeeventstore.ChangeSet;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class TransactionHelper {

    @EJB
    private EventStoreCommitNotifier notifier;

    @Resource
    private UserTransaction userTransaction;

    public void submit_then_rollback(ChangeSet cs, long waittime) {
	try {
	    userTransaction.begin();
            notifier.notifyListeners(cs);
	    System.out.println("Appended, now waiting before rollback");
	    Thread.sleep(waittime);
	    userTransaction.rollback();
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    public void submit_and_commit(ChangeSet cs) {
	try {
            userTransaction.begin();
            notifier.notifyListeners(cs);
            userTransaction.commit();
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }
    
}
