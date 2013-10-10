package org.jeeventstore.core.notifier;

import java.util.UUID;
import org.jboss.arquillian.testng.Arquillian;
import org.jeeventstore.core.ChangeSet;
import org.jeeventstore.core.store.TestChangeSet;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Alexander Langer
 */
public abstract class AbstractEventStoreCommitNotifierTest 
        extends Arquillian
        implements EventStoreCommitListener {

    public final static long WAIT_TIME = 1000l;

    protected abstract EventStoreCommitNotifier instance();

    protected ChangeSet changeSet(String id) {
        return new TestChangeSet(id);
    }

    private boolean caught = false;
    private ChangeSet caughtCS = null;

    @Override
    public void receive(EventStoreCommitNotification notification) {
        caught = true;
        caughtCS = notification.changes();
    }

    private void clear() {
        sleep(WAIT_TIME);
        caught = false;
        caughtCS = null;
        try {
            this.instance().removeListener(this);
        } catch (Exception e) {
            // might happen, since we are just cleaning up
        }
        assertFalse(caught);
        assertNull(caughtCS);
    }

    @Test
    public void test_add_and_remove_listener_works() {
        this.clear();
        EventStoreCommitNotifier instance = this.instance();
        instance.addListener(this);
        instance.removeListener(this);
    }

    @Test
    public void test_notification() {
        this.clear();
        EventStoreCommitNotifier instance = this.instance();
        instance.addListener(this);
        String id = UUID.randomUUID().toString();
        ChangeSet cs = changeSet(id);
        instance.notifyListeners(cs);
        sleep(WAIT_TIME * 2);
        // must have received by now
        assertTrue(caught);
        assertNotNull(caughtCS);
        assertEquals(caughtCS.bucketId(), id);
    }

    @Test
    public void test_notification_after_removal() {
        this.clear();
        EventStoreCommitNotifier instance = this.instance();
        instance.addListener(this);
        instance.removeListener(this);
        String id = UUID.randomUUID().toString();
        ChangeSet cs = changeSet(id);
        instance.notifyListeners(cs);
        sleep(WAIT_TIME * 2);
        // if delivering, it would have received by now
        assertFalse(caught);
        assertNull(caughtCS);
    }

    @Test
    public void test_notification_when_not_listening() {
        this.clear();
        EventStoreCommitNotifier instance = this.instance();
        String id = UUID.randomUUID().toString();
        ChangeSet cs = changeSet(id);
        instance.notifyListeners(cs);
        sleep(WAIT_TIME * 2);
        // if delivering, it would have received by now
        assertFalse(caught);
        assertNull(caughtCS);
    }

    @Test
    public void test_remove_nonlistener_fails() {
        this.clear();
        EventStoreCommitNotifier instance = this.instance();
        try {
            instance.removeListener(this);
            fail("Should have failed by now");
        } catch (Exception e) {
            // ok
        }
    }

    @Test
    public void test_multiple_add_fails() {
        this.clear();
        EventStoreCommitNotifier instance = this.instance();
        instance.addListener(this);
        try {
            instance.addListener(this);
            fail("Should have failed by now");
        } catch (Exception e) {
            // ok
        }
    }

    protected void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // ignore
        }
    }

}