package org.jeeventstore.core.store;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.persistence.Persistence;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jeeventstore.core.ChangeSet;
import org.jeeventstore.core.ConcurrencyException;
import org.jeeventstore.core.DefaultDeployment;
import org.jeeventstore.core.DuplicateCommitException;
import org.jeeventstore.core.ReadableEventStream;
import org.jeeventstore.core.WritableEventStream;
import org.jeeventstore.core.notifier.AbstractEventStoreCommitNotifierTest;
import static org.jeeventstore.core.notifier.AbstractEventStoreCommitNotifierTest.WAIT_TIME;
import org.jeeventstore.core.notifier.EventStoreCommitListener;
import org.jeeventstore.core.notifier.EventStoreCommitNotification;
import org.jeeventstore.core.notifier.EventStoreCommitNotifier;
import org.jeeventstore.core.notifier.SyncEventStoreCommitNotifierTest;
import org.jeeventstore.core.persistence.EventStorePersistence;
import static org.testng.Assert.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

public class EventStoreServiceTest extends Arquillian 
    implements EventStoreCommitListener {

    // these must be static to have them set from different threads
    protected static boolean caught = false;
    protected static ChangeSet caughtCS = null;

    @Override
    public void receive(EventStoreCommitNotification notification) {
        caught = true;
        caughtCS = notification.changes();
    }

    @Deployment
    public static EnterpriseArchive deployment() {
        EnterpriseArchive ear = DefaultDeployment.ear("org.jeeventstore:jeeventstore-core");
        ear.addAsModule(ShrinkWrap.create(JavaArchive.class)
                .addAsManifestResource(new File("src/test/resources/META-INF/beans.xml"))
                .addAsManifestResource(
                    new File("src/test/resources/META-INF/ejb-jar-EventStoreServiceTest.xml"),
                             "ejb-jar.xml")
                .addClass(MockPersistence.class)
                .addClass(EventStoreServiceTest.class)
                .addClass(TestUtils.class)
                .addClass(TestChangeSet.class)
                );
        return ear;
    }

    @EJB
    private EventStorePersistence persistence;

    @EJB
    private EventStoreCommitNotifier notifier;

    @EJB
    private EventStore eventStore;

    private void cleanup() {
        try {
            notifier.addListener(this);
        } catch (IllegalStateException e) {
            // ignore
        }
        caught = false;
        caughtCS = null;
        try {
            persistence.persistChanges(MockPersistence.resetCommand());
        } catch (ConcurrencyException e) {
            // ignore
        }
    }

    private void fill(String bucketId, String streamId, int count) throws Exception {
        WritableEventStream wes = eventStore.createStream(bucketId, streamId);
        for (int i = 0; i < count; i++) {
            List<Integer> data = TestUtils.randomdata(i % 5 + 1);
            for (Integer ii : data)
                wes.append(ii);
            wes.commit(UUID.randomUUID());
        }
    }

    @Test
    public void test_openStreamForReading() throws Exception {
        cleanup();
        fill("BUCKET_ID", "FOO", 10);
        ReadableEventStream res = eventStore.openStreamForReading("BUCKET_ID", "FOO");
        assertEquals(res.bucketId(), "BUCKET_ID");
        assertEquals(res.streamId(), "FOO");
        assertEquals(res.version(), 10);
    }

    @Test 
    public void test_openStreamForReading_nonexistent() throws Exception {
        cleanup();
        try {
            eventStore.openStreamForReading("", "");
            fail("Should have failed by now");
        } catch (EJBException e) {
            if (e.getCause().getClass().equals(StreamNotFoundException.class)) {
                // ok
            } else {
                throw e;
            }
        }
    }
    
    @Test
    public void test_openStreamForReading_versioned() throws Exception {
        cleanup();
        fill("BUCKET_ID", "FOO", 10);
        ReadableEventStream res = eventStore.openStreamForReading("BUCKET_ID", "FOO", 6);
        assertEquals(res.bucketId(), "BUCKET_ID");
        assertEquals(res.streamId(), "FOO");
        assertEquals(res.version(), 6);
    }

    @Test
    public void testExistsStream() throws Exception {
        cleanup();
        assertEquals(eventStore.existsStream("FOO", "BAR"), false);
        fill("FOO", "BAR", 1);
        assertEquals(eventStore.existsStream("FOO", "BAR"), true);
    }

    @Test
    public void testCreateStream() throws Exception {
        cleanup();
        WritableEventStream wes = eventStore.createStream("BUCKET_ID", "CREATED");
        assertEquals(wes.version(), 0);
        testWriting(wes);
        assertEquals("BUCKET_ID", wes.bucketId());
        assertEquals("CREATED", wes.streamId());
    }
    
    @Test
    public void testOpenStreamForWriting() throws Exception {
        cleanup();
        fill("BUCKET_ID", "FOO", 10);
        WritableEventStream wes = eventStore.openStreamForWriting("BUCKET_ID", "FOO", 10);
        assertEquals(wes.version(), 10);
        testWriting(wes);
        assertEquals("BUCKET_ID", wes.bucketId());
        assertEquals("FOO", wes.streamId());
    }

    @Test
    public void testOpenStreamForWritingConcurrency() throws Exception {
        cleanup();
        fill("BUCKET_ID", "FOO", 10);
        caught = false;
        caughtCS = null;
        WritableEventStream wes = eventStore.openStreamForWriting("BUCKET_ID", "FOO", 8);
        wes.append("bla");
        try {
            wes.commit(UUID.randomUUID());
            fail("Should have failed by now");
        } catch (ConcurrencyException e) {
            // expected
        }
        assertTrue(!caught);
        assertTrue(caughtCS == null);
        assertEquals("BUCKET_ID", wes.bucketId());
        assertEquals("FOO", wes.streamId());
    }

    private void testWriting(WritableEventStream wes) throws Exception {
        long initialVersion = wes.version();
        List<Integer> data = TestUtils.randomdata(10);
        for (int i = 0; i < 6; i++)
            wes.append(data.get(i));
        UUID c1id = UUID.randomUUID();
        wes.commit(c1id);
        assertEquals(wes.version(), initialVersion + 1);

        assertTrue(caught);
        assertNotNull(caughtCS);
        assertEquals(wes.bucketId(), caughtCS.bucketId());
        assertEquals(wes.streamId(), caughtCS.streamId());
        assertEquals(initialVersion + 1, caughtCS.streamVersion());
        assertEquals(c1id, caughtCS.changeSetId());
        assertEquals(data.subList(0, 6), TestUtils.toList(caughtCS.events()));

        for (int i = 6; i < 10; i++)
                wes.append(data.get(i));
        UUID c2id = UUID.randomUUID();
        wes.commit(c2id);
        assertEquals(initialVersion + 2, wes.version());

        Iterator<ChangeSet> allit = persistence.allChanges();
        List<Serializable> events = TestUtils.toList(new EventsIterator<>(allit));
        assertTrue(events.size() >= 10);
        events = events.subList(events.size() - 10, events.size());
        assertEquals(data, events);

        assertTrue(caught);
        assertNotNull(caughtCS);
        assertEquals(wes.bucketId(), caughtCS.bucketId());
        assertEquals(wes.streamId(), caughtCS.streamId());
        assertEquals(initialVersion + 2, caughtCS.streamVersion());
        assertEquals(c2id, caughtCS.changeSetId());
        assertEquals(data.subList(6, 10), TestUtils.toList(caughtCS.events()));

    }

    
}
