package org.jeeventstore.core.notifier;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;
import org.jeeventstore.core.ChangeSet;

/**
 *
 * @author langer
 */
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
