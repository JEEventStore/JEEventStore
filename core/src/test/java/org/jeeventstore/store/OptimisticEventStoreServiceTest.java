package org.jeeventstore.store;

import org.jeeventstore.StreamNotFoundException;
import org.jeeventstore.ConcurrencyException;
import org.jeeventstore.EventStore;
import org.jeeventstore.persistence.MockPersistence;
import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.DuplicateCommitException;
import org.jeeventstore.ReadableEventStream;
import org.jeeventstore.WritableEventStream;
import org.jeeventstore.EventStoreCommitListener;
import org.jeeventstore.EventStoreCommitNotification;
import org.jeeventstore.EventStoreCommitNotifier;
import org.jeeventstore.EventStorePersistence;
import org.jeeventstore.notifier.SyncEventStoreCommitNotifier;
import org.jeeventstore.util.IteratorUtils;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class OptimisticEventStoreServiceTest extends Arquillian 
    implements EventStoreCommitListener {

    public final static String BUCKET_ID = "BUCKET_ID";

    // these must be static to have them set from different threads
    protected static boolean caught = false;
    protected static ChangeSet caughtCS = null;

    @Override
    public void receive(EventStoreCommitNotification notification) {
        caught = true;
        caughtCS = notification.changes();
    }

    @Deployment
    public static Archive<?> deployment() {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsModule(ShrinkWrap.create(JavaArchive.class, "ejb.jar")
                        .addAsManifestResource(new File("src/test/resources/META-INF/beans.xml"))
                        .addAsManifestResource(new File(
                                "src/test/resources/META-INF/ejb-jar-OptimisticEventStoreServiceTest.xml"),
                                "ejb-jar.xml")
                        .addPackage(ChangeSet.class.getPackage())
                        .addPackage(EventStoreCommitNotifier.class.getPackage())
                        .addPackage(OptimisticEventStoreService.class.getPackage())
                        .addPackage(SyncEventStoreCommitNotifier.class.getPackage())
                        .addClass(IteratorUtils.class)
                        .addClass(MockPersistence.class)
                        .addClass(EventStorePersistence.class)
                );
        return ear;
    }

    @EJB(lookup = "java:global/test/ejb/EventStorePersistence")
    private EventStorePersistence persistence;

    @EJB(lookup = "java:global/test/ejb/EventStoreCommitNotifier")
    private EventStoreCommitNotifier notifier;

    @EJB(lookup = "java:global/test/ejb/EventStore")
    private EventStore eventStore;

    private void cleanup() {
        try {
            notifier.addListener(BUCKET_ID, this);
        } catch (IllegalStateException | EJBException e) {
            // ignore
        }
        caught = false;
        caughtCS = null;
        try {
            persistence.persistChanges(MockPersistence.resetCommand());
        } catch (ConcurrencyException | DuplicateCommitException | EJBException e) {
            // ignore
        }
    }

    private void fill(String bucketId, String streamId, int count) throws Exception {
        WritableEventStream wes = eventStore.createStream(bucketId, streamId);
        for (int i = 0; i < count; i++) {
            List<Integer> data = TestUtils.randomdata(i % 5 + 1);
            for (Integer ii : data)
                wes.append(ii);
            wes.commit(UUID.randomUUID().toString());
        }
    }

    @Test
    public void test_openStreamForReading() throws Exception {
        cleanup();
        fill(BUCKET_ID, "FOO", 10);
        ReadableEventStream res = eventStore.openStreamForReading(BUCKET_ID, "FOO");
        assertEquals(res.bucketId(), BUCKET_ID);
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
        fill(BUCKET_ID, "FOO", 10);
        ReadableEventStream res = eventStore.openStreamForReading(BUCKET_ID, "FOO", 6);
        assertEquals(res.bucketId(), BUCKET_ID);
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
        WritableEventStream wes = eventStore.createStream(BUCKET_ID, "CREATED");
        assertEquals(wes.version(), 0);
        testWriting(wes);
        assertEquals(BUCKET_ID, wes.bucketId());
        assertEquals("CREATED", wes.streamId());
    }
    
    @Test
    public void testOpenStreamForWriting() throws Exception {
        cleanup();
        fill(BUCKET_ID, "FOO", 10);
        WritableEventStream wes = eventStore.openStreamForWriting(BUCKET_ID, "FOO", 10);
        assertEquals(wes.version(), 10);
        testWriting(wes);
        assertEquals(BUCKET_ID, wes.bucketId());
        assertEquals("FOO", wes.streamId());
    }

    @Test
    public void testOpenStreamForWritingConcurrency() throws Exception {
        cleanup();
        fill(BUCKET_ID, "FOO", 10);
        caught = false;
        caughtCS = null;
        WritableEventStream wes = eventStore.openStreamForWriting(BUCKET_ID, "FOO", 8);
        wes.append("bla");
        try {
            wes.commit(UUID.randomUUID().toString());
            fail("Should have failed by now");
        } catch (ConcurrencyException e) {
            // expected
        }
        assertTrue(!caught);
        assertTrue(caughtCS == null);
        assertEquals(BUCKET_ID, wes.bucketId());
        assertEquals("FOO", wes.streamId());
    }

    private void testWriting(WritableEventStream wes) throws Exception {
        long initialVersion = wes.version();
        List<Integer> data = TestUtils.randomdata(10);
        for (int i = 0; i < 6; i++)
            wes.append(data.get(i));
        String c1id = UUID.randomUUID().toString();
        wes.commit(c1id);
        assertEquals(wes.version(), initialVersion + 1);

        assertTrue(caught);
        assertNotNull(caughtCS);
        assertEquals(wes.bucketId(), caughtCS.bucketId());
        assertEquals(wes.streamId(), caughtCS.streamId());
        assertEquals(initialVersion + 1, caughtCS.streamVersion());
        assertEquals(c1id, caughtCS.changeSetId());
        assertEquals(data.subList(0, 6), IteratorUtils.toList(caughtCS.events()));

        for (int i = 6; i < 10; i++)
                wes.append(data.get(i));
        String c2id = UUID.randomUUID().toString();
        wes.commit(c2id);
        assertEquals(initialVersion + 2, wes.version());

        Iterator<ChangeSet> allit = persistence.allChanges(wes.bucketId());
        List<Serializable> events = IteratorUtils.toList(new EventsIterator<>(allit));
        assertTrue(events.size() >= 10);
        events = events.subList(events.size() - 10, events.size());
        assertEquals(data, events);

        assertTrue(caught);
        assertNotNull(caughtCS);
        assertEquals(wes.bucketId(), caughtCS.bucketId());
        assertEquals(wes.streamId(), caughtCS.streamId());
        assertEquals(initialVersion + 2, caughtCS.streamVersion());
        assertEquals(c2id, caughtCS.changeSetId());
        assertEquals(data.subList(6, 10), IteratorUtils.toList(caughtCS.events()));

    }

    
}
