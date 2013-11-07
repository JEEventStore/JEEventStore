package org.jeeventstore.notifier;

import org.jeeventstore.EventStoreCommitNotification;
import org.jeeventstore.EventStoreCommitNotifier;
import java.io.File;
import java.util.UUID;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.store.TestChangeSet;
import org.jeeventstore.tests.DefaultDeployment;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Alexander Langer
 */
public class AsyncEventStoreCommitNotifierTest extends AbstractEventStoreCommitNotifierTest {

    @Deployment
    public static Archive<?> deployment() {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsModule(ShrinkWrap.create(JavaArchive.class, "ejb.jar")
                        .addAsManifestResource(new File("src/test/resources/META-INF/beans.xml"))
                        .addAsManifestResource(
                            new File("src/test/resources/META-INF/ejb-jar-AsyncEventStoreCommitNotifierTest.xml"),
                            "ejb-jar.xml")
                        .addPackage(AsyncEventStoreCommitNotifier.class.getPackage())
                        .addPackage(ChangeSet.class.getPackage())
                        .addClass(TestChangeSet.class)
                );
        return ear;
    }

    @EJB(lookup = "java:global/test/ejb/EventStoreCommitNotifier")
    private EventStoreCommitNotifier notifier;

    @EJB(lookup = "java:global/test/ejb/TransactionHelper")
    private TransactionHelper helper;

    @Override
    protected EventStoreCommitNotifier instance() {
        return this.notifier;
    }

    private int triesUntilSuccess = 0;
    private long waitTime = 0;

    @Override
    public void receive(EventStoreCommitNotification notification) {
        if (triesUntilSuccess-- <= 0) {
            // order is important, sleep must come first
            this.sleep(this.waitTime);
            super.receive(notification);
        } else
            throw new RuntimeException("Failed because tries left: " + triesUntilSuccess);
    }

    @Override
    protected void clear() {
        super.clear();
        this.triesUntilSuccess = 0;
        this.waitTime = 0;
    }

    @Test
    public void test_asynchronous_delivery() {
        this.clear();
        EventStoreCommitNotifier instance = this.instance();
        instance.addListener(this);
        String id = UUID.randomUUID().toString();
        ChangeSet cs = changeSet(id);

        this.waitTime = WAIT_TIME * 2;
        // should return immediately
        instance.notifyListeners(cs);

        // since receive() is sleeping WAIT_TIME * 2, it must not have
        // caught yet.  We still wait WAIT_TIME to be sure no race condition
        // occurs when setting the caught flag.
        this.sleep(WAIT_TIME);
        assertEquals(caught, false);

        // now we wait long enough to guarantee delivery
        this.sleep(WAIT_TIME * 3);
        assertEquals(caught, true);
        assertEquals(caughtCS.bucketId(), id);
    }

    @Test
    public void test_that_there_is_no_notification_on_rollback() {
        this.clear();
        EventStoreCommitNotifier instance = this.instance();
        instance.addListener(this);
        String id = UUID.randomUUID().toString();
        ChangeSet cs = changeSet(id);
        helper.submit_then_rollback(cs, WAIT_TIME);
        this.sleep(WAIT_TIME * 2);
        assertEquals(caught, false);
    }

    @Test
    public void test_that_notification_is_rescheduled_on_exception() {
        this.clear();
        this.triesUntilSuccess = 20;
        // retry interval is 1 sec on Glassfish, wait twice the amount
        int waited = triesUntilSuccess * 2;
        EventStoreCommitNotifier instance = this.instance();
        instance.addListener(this);
        String id = UUID.randomUUID().toString();
        ChangeSet cs = changeSet(id);
        helper.submit_and_commit(cs);

        while (waited-- > 0) {
            this.sleep(WAIT_TIME );
            if (caught)
                break;
        }
        assertEquals(caught, true);
        assertEquals(caughtCS.bucketId(), id);
    }
    
}