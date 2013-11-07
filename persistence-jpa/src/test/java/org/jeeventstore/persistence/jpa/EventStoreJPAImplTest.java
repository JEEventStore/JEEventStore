//package de.redrainbow.care.common.port.adapter.event.store.jpa;
//
//import org.jeeventstore.core.DefaultDeployment;
//import de.redrainbow.care.common.event.Event;
//import de.redrainbow.care.common.event.store.EventStore;
//import org.jeeventstore.core.notifier.EventStoreChangeListener;
//import org.jeeventstore.core.EventStoreAppendedEventStream;
//import de.redrainbow.care.common.event.store.EventStoreException;
//import de.redrainbow.care.common.event.store.EventStream;
//import de.redrainbow.care.common.event.store.EventStreamId;
//import de.redrainbow.care.common.port.adapter.event.TestEvent;
//import java.io.File;
//import java.nio.file.Files;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.UUID;
//import javax.ejb.EJB;
//import javax.ejb.EJBException;
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//import javax.transaction.RollbackException;
//import org.apache.commons.collections.IteratorUtils;
//import org.jboss.arquillian.container.test.api.Deployment;
//import org.jboss.arquillian.testng.Arquillian;
//import org.jboss.shrinkwrap.api.ShrinkWrap;
//import org.jboss.shrinkwrap.api.exporter.ZipExporter;
//import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
//import org.jboss.shrinkwrap.api.spec.JavaArchive;
//import org.testng.annotations.Test;
//import static org.testng.Assert.*;
//
///**
// * @author alex
// */
//public class EventStoreJPAImplTest extends Arquillian implements EventStoreChangeListener {
//
//    @Deployment
//    public static EnterpriseArchive deployment() {
//        EnterpriseArchive ear = DefaultDeployment.ear("org.jeeventstore:jeeventstore-core");
//        ear.addAsModule(ShrinkWrap.create(JavaArchive.class)
//                .addAsManifestResource(new File("src/test/resources/META-INF/beans.xml"))
//                .addAsManifestResource(new File("src/test/resources/META-INF/persistence.xml"))
//                .addAsManifestResource(
//                    new File("src/test/resources/META-INF/ejb-jar-EventStoreJPAImplTest.xml"),
//                    "ejb-jar.xml")
//                .addClass(TestEvent.class)
//		.addClass(AsyncTester.class)
//                .addClass(EventStoreJPAImplTest.class)
//                );
//        try {
//            File cp = new File("/tmp/test.ear");
//            if (Files.exists(cp.toPath()))
//                Files.delete(cp.toPath());
//            ear.as(ZipExporter.class).exportTo(cp);
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            e.printStackTrace();;
//        }
//        return ear;
//    }
//
//    @EJB(beanName = "EventStoreForEventStoreJPAImplTest")
//    private EventStore eventStore;
//
//    @PersistenceContext
//    private EntityManager em;
//
//    @EJB
//    private AsyncTester asyncTester;
//
//    static boolean caught = false;
//    static List<Event> caughtEvents;
//
//    @Override
//    public void notifyAppend(EventStoreAppendedEventStream stream) {
//        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<< EventStoreJPAImplNGTest OBSERVE >>>>>>>>>>>>>>>>>>> ");
//        caught = true;
//        caughtEvents = stream.events();
//    }
//
//    private List<Event> events(int num) {
//        List<Event> events = new ArrayList<Event>();
//        for (int idx = 1; idx <= num; idx++)
//            events.add(new TestEvent(idx));
//        return events;
//    }
//
//    @Test
//    public void testAppend() {
//        List<Event> events = events(4);
//
//        EventStreamId streamId = new EventStreamId(UUID.randomUUID().toString());
//        this.eventStore.appendWith(streamId, events);
//        
//        EventStream eventStream = this.eventStore.fullEventStreamFor(streamId);
//        assertEquals(eventStream.version(), 4);
//        assertEquals(eventStream.events().size(), 4);
//        for (int idx = 1; idx <= 4; idx++) {
//            Event event = eventStream.events().get(idx - 1);
//            assertEquals(idx, ((TestEvent) event).payload);
//        }
//    }
//
////    /**
////     * The following test FAILS if   @GeneratedValue(strategy = GenerationType.SEQUENCE)
////     * is used for EventStoreJPAEntry's @Id
////     * PostgreSQL sequence numbers may contain 'holes' due to rollbacks.
////     * This tests against it.
////     */
////    @Test
////    public void test_parallel_insert_with_roolback_keeps_sequence_numbers_in_order() {
////	eventStore.appendSingleEvent(new EventStreamId("PREFILL"), new TestEvent(11));
////	asyncTester.test1();
////	asyncTester.test2();
////
////	try {
////	    Thread.sleep(10000);
////	} catch (InterruptedException e) {
////	    // ignore
////	}
////
////	System.out.println("Lade alle events aus store");
////	List<SequencedEvent> events = eventStore.retrieveEvents(0l);
////	for (SequencedEvent se : events) {
////	    System.out.println("Got event with seqNumber = " + se.sequenceNumber());
////	}
////	assertEquals(events.get(0).sequenceNumber(), 1l);
////	assertEquals(events.get(1).sequenceNumber(), 2l); // will be 3 when not properly in order
////    }
//
//    @Test
//    public void test_that_nonexistent_stream_throws() {
//        EventStreamId streamId = new EventStreamId(UUID.randomUUID().toString());
//        try {
//            EventStream eventStream = this.eventStore.fullEventStreamFor(streamId);
//            fail("should have thrown by now");
//        } catch (EJBException e) {
//            if (e.getCause() instanceof EventStoreException) {
//                // ok
//            } else {
//                fail("threw unexpected exception, expected EventStoreException");
//            }
//        }
//    }
//
//    @Test
//    public void test_that_events_since_empty_throws() {
//        List<Event> events = events(4);
//        EventStreamId streamId = new EventStreamId(UUID.randomUUID().toString());
//        this.eventStore.appendWith(streamId, events);
//        EventStream eventStream = this.eventStore.fullEventStreamFor(streamId);
//        try {
//            EventStreamId highId = streamId.withStreamVersion(eventStream.version()+1);
//            this.eventStore.eventStreamSince(highId);
//            fail("should have thrown by now");
//        } catch (EJBException e) {
//            if (e.getCause() instanceof EventStoreException) {
//                // ok
//            } else {
//                fail("threw unexpected exception, expected EventStoreException");
//            }
//        }
//    }
//
//    // optimistic lock test
//    @Test
//    public void testAppendWrongVersion() {
//        caught = false;
//        List<Event> events = events(10);
//        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());
//        System.out.println("Starting regular event store append, this is expected to complete");
//        this.eventStore.addAppendListener(this);
//        appendAndWait(eventId, events); // also wait a bit to make the notification catch
//        System.out.println("Regular appending completed");
//        assertTrue(caught);
//        EventStream eventStream = this.eventStore.fullEventStreamFor(eventId);
//        assertEquals(10, eventStream.version());
//        assertEquals(10, eventStream.events().size());
//
//        events.clear();
//        events.add(new TestEvent(11));
//        caught = false;
//
//        try {
//            System.out.println("About to insert wrong version");
//            appendAndWait(eventId.withStreamVersion(9), events);
//            fail("Should have thrown an exception.");
//        } catch (EJBException e) {
//            System.out.println("CAUGHT EJBException (as expected)");
//            if (e.getCause() instanceof EventStoreException) {
//                // ok
//            } if (e.getCause() instanceof RollbackException) {
//                // also ok
//            } else {
//                fail("threw unexpected exception, expected EventStoreAppendException, but got: " + e.getCause());
//            }
//        }
//
//        // must not have notified the event handler
//        assertFalse(caught);
//        // must still be the same size
//        eventStream = this.eventStore.fullEventStreamFor(eventId);
//        assertEquals(10, eventStream.version());
//        assertEquals(10, eventStream.events().size());
//
//        //this should succeed in principle, but it will actually fail, because
//        // the current transaction is doomed due to the previous rollback
//        //System.out.println("before last insert");
//        //this.eventStore.appendWith(eventId.withStreamVersion(11), events);
//    }
//
//    @Test
//    public void testEventStreamSince() {
//        List<Event> events = events(10);
//        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());
//        this.eventStore.appendWith(eventId, events);
//
//        for (int idx = 10; idx >= 1; --idx) {
//            EventStream eventStream = this.eventStore.eventStreamSince(eventId.withStreamVersion(idx));
//            assertEquals(10, eventStream.version());
//            assertEquals(10 - idx + 1, eventStream.events().size());
//            Event domainEvent = eventStream.events().get(0);
//            assertEquals(idx, ((TestEvent) domainEvent).payload);
//        }
//
//        try {
//            this.eventStore.eventStreamSince(eventId.withStreamVersion(11));
//            fail("Should have thrown an exception.");
//        } catch (EJBException e) {
//            if (e.getCause() instanceof EventStoreException) {
//                // ok
//            } else {
//                fail("threw unexpected exception, expected EventStoreException");
//            }
//        }
//    }
//
//    @Test
//    public void testFullEventStreamForStreamName() {
//        List<Event> events = events(3);
//        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());
//        this.eventStore.appendWith(eventId, events);
//        EventStream eventStream = this.eventStore.fullEventStreamFor(eventId);
//
//        assertEquals(eventStream.version(), 3);
//        assertEquals(eventStream.events().size(), 3);
//
//        events.clear();
//        events.add(new TestEvent(4));
//        this.eventStore.appendWith(eventId.withStreamVersion(eventStream.version()), events);
//
//        eventStream = this.eventStore.fullEventStreamFor(eventId);
//        assertEquals(eventStream.version(), 4);
//        assertEquals(eventStream.events().size(), 4);
//
//        for (int idx = 1; idx <= 4; ++idx) {
//            Event domainEvent = eventStream.events().get(idx - 1);
//            assertEquals(idx, ((TestEvent) domainEvent).payload);
//        }
//    }
//
//    @Test
//    public void testRetrieveEvents() {
//        List<Event> events = events(10);
//        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());
//
//        // first save current state
//        Iterator<Event> allEventsIterator = this.eventStore.eventsIterator();
//        List<Event> allEventsList = IteratorUtils.toList(allEventsIterator);
//        int offset = allEventsList.size();
//
//        // now append
//        this.eventStore.appendWith(eventId, events);
//
//        Iterator<Event> newEventsIterator = this.eventStore.eventsIterator();
//        List<Event> newEvents = IteratorUtils.toList(newEventsIterator);
//        assertEquals(newEvents.size() - offset, 10);
//        for (int i = 1; i <= 10; i++)
//            assertEquals(((TestEvent) newEvents.get(offset - 1 + i)).payload, i);
//    }
//
//    @Test
//    public void test_notify() {
//        caught = false;
//        assertFalse(caught);
//        List<Event> events = events(1);
//        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());
//        this.eventStore.addAppendListener(this);
//        appendAndWait(eventId, events);
//        assertTrue(caught);
//    }
//
//    @Test
//    public void test_hasEventStream() {
//        List<Event> events = events(1);
//        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());
//	assertEquals(this.eventStore.hasEventStream(eventId), false);
//        this.eventStore.appendWith(eventId, events);
//	assertEquals(this.eventStore.hasEventStream(eventId), true);
//    }
//
//    @Test
//    public void test_appendSingle() {
//        List<Event> events = events(10);
//        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());
//	for (Event e: events)
//	    this.eventStore.appendSingleEvent(eventId, e);
//    }
//
//    @Test
//    public void test_appendSingle_with_version_check() {
//        List<Event> events = events(3);
//        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());
//	this.eventStore.appendSingleEvent(eventId, events.get(0));
//	this.eventStore.appendSingleEvent(eventId, events.get(1));
//	try {
//	    this.eventStore.appendSingleEvent(eventId.withStreamVersion(1), events.get(2));
//	    fail("should have failed by now");
//	} catch (EJBException e) {
//	    // expected
//	}
//
//    }
//
//    private void appendAndWait(EventStreamId streamId, List<Event> events) {
//        this.eventStore.appendWith(streamId, events);
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            // ignore
//        }
//    }
//
//}