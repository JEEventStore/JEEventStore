package org.jeeventstore.store;

import org.jeeventstore.persistence.MockPersistence;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.ReadWriteEventStream;
import org.jeeventstore.ReadableEventStream;
import org.jeeventstore.WritableEventStream;
import org.jeeventstore.EventStorePersistence;
import org.jeeventstore.ConcurrencyException;
import org.jeeventstore.DuplicateCommitException;
import org.jeeventstore.util.IteratorUtils;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Alexander Langer
 */
public class OptimisticEventStreamTest {

    private EventStorePersistence persistence;

    private final static String BUCKET_ID = "BUCKET_ID";
    private final static String STREAM_ID = "STREAM_ID";
    private final static int NUM_CHANGESETS = 100;

    private List<ChangeSet> data;

    @BeforeMethod(alwaysRun = true)
    public void init() {
        this.persistence = new MockPersistence();
        data = TestUtils.createChangeSets(BUCKET_ID, STREAM_ID, 1, NUM_CHANGESETS);
        try {
            for (ChangeSet cs : data)
                persistence.persistChanges(cs);
        } catch (ConcurrencyException | DuplicateCommitException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test_create_writeable() {
        WritableEventStream oes = OptimisticEventStream.createWritable(BUCKET_ID, STREAM_ID, 28l, persistence);
        assertEquals(oes.bucketId(), BUCKET_ID);
        assertEquals(oes.streamId(), STREAM_ID);
        assertEquals(28l, oes.version());
    }

    @Test
    public void test_create_readable() {
        assertNotNull(persistence);
        ReadableEventStream oes = OptimisticEventStream.createReadable(BUCKET_ID, STREAM_ID, 28l, persistence);
        assertEquals(oes.bucketId(), BUCKET_ID);
        assertEquals(oes.streamId(), STREAM_ID);
        assertEquals(28l, oes.version());
    }

    @Test
    public void test_create_readwritable() {
        assertNotNull(persistence);
        ReadWriteEventStream oes = OptimisticEventStream.createReadWritable(
                BUCKET_ID, STREAM_ID, 28l, persistence);
        assertEquals(oes.bucketId(), BUCKET_ID);
        assertEquals(oes.streamId(), STREAM_ID);
        assertEquals(28l, oes.version());
    }

    @Test
    public void test_readable_nonexistent() throws Exception {
        persistence.persistChanges(MockPersistence.resetCommand());
        try {
            ReadableEventStream oes = OptimisticEventStream.createReadable("wrong", "stream", 28l, persistence);
            fail("Should have failed by now");
        } catch (StreamNotFoundException e) {
            // ok
        }
    }

    @Test
    public void test_read_max_version() {
        ReadableEventStream oes = OptimisticEventStream.createReadable(BUCKET_ID, STREAM_ID,
                Integer.MAX_VALUE, persistence);
        compareData(new EventsIterator(data.iterator()), oes.events());
    }

    @Test
    public void test_read_intermediate_version() {
        ReadableEventStream oes = OptimisticEventStream.createReadable(BUCKET_ID, STREAM_ID, 56, persistence);
        compareData(new EventsIterator(data.subList(0, 56).iterator()), oes.events());
    }

    @Test
    public void test_invalid_persistence_getFrom() {
        // create an invalid persistence
        List<ChangeSet> invalid = TestUtils.createChangeSets(BUCKET_ID, STREAM_ID, 2, 10);
        for(ChangeSet cs: invalid) {
            try {
                persistence.persistChanges(cs);
            } catch (ConcurrencyException | DuplicateCommitException e) {
                // ignore on purpose, since we aim for a broken persistence
            }
        }
        assertEquals(NUM_CHANGESETS+ 9, number_of_persisted_changesets());
        try {
            ReadableEventStream oes = OptimisticEventStream.createReadable(BUCKET_ID, STREAM_ID,
                    Integer.MAX_VALUE, persistence);
            fail("Should have failed by now");
        } catch (IllegalStateException e) {
            // ok
        }
    }

    @Test
    public void test_regular_commit_readwritable() throws Exception {
        OptimisticEventStream oes = (OptimisticEventStream) OptimisticEventStream
                .createReadWritable(BUCKET_ID, STREAM_ID, NUM_CHANGESETS, persistence);

        List<ChangeSet> beforePersisted = IteratorUtils.toList(persistence.allChanges(BUCKET_ID));

        // now commit some data
        List<Integer> ints = TestUtils.randomdata(10);
        for (Integer i : ints)
            oes.append(i);
        UUID commitId = UUID.randomUUID();
        try {
            oes.commit(commitId);
        } catch (DuplicateCommitException | ConcurrencyException e) {
            throw new RuntimeException(e);
        }

        // check that the stream has updated accordingly
        assertEquals(oes.bucketId(), BUCKET_ID);
        assertEquals(oes.streamId(), STREAM_ID);
        assertEquals(NUM_CHANGESETS + 1, oes.version());
        List<Serializable> afterEventsInStream = IteratorUtils.toList(oes.events());
        List<Serializable> newEventsInStream = afterEventsInStream.subList(
                afterEventsInStream.size() - ints.size(), afterEventsInStream.size());
        assertEquals(ints, newEventsInStream);
        comparePersisted(beforePersisted, oes.version(), ints);
    }

    @Test
    public void test_regular_commit_write_only() throws Exception {
        OptimisticEventStream oes = (OptimisticEventStream) OptimisticEventStream
                .createWritable(BUCKET_ID, STREAM_ID, NUM_CHANGESETS, persistence);

        List<ChangeSet> beforePersisted = IteratorUtils.toList(persistence.allChanges(BUCKET_ID));

        // now commit some data
        List<Integer> ints = TestUtils.randomdata(10);
        for (Integer i : ints)
            oes.append(i);
        UUID commitId = UUID.randomUUID();
        try {
            oes.commit(commitId);
        } catch (DuplicateCommitException | ConcurrencyException e) {
            throw new RuntimeException(e);
        }

        comparePersisted(beforePersisted, NUM_CHANGESETS + 1, ints);
    }

    @Test
    public void test_non_commit_of_null_events() throws Exception {
        assertEquals(NUM_CHANGESETS, number_of_persisted_changesets());
        OptimisticEventStream oes = (OptimisticEventStream) OptimisticEventStream
                .createWritable(BUCKET_ID, STREAM_ID, NUM_CHANGESETS, persistence);
        oes.append(null);
        oes.append(null);
        oes.append(null);
        oes.append(null);
        oes.commit(UUID.randomUUID());
        assertEquals(NUM_CHANGESETS, oes.version());
        assertEquals(NUM_CHANGESETS, number_of_persisted_changesets());
    }

    @Test
    public void test_non_commit_of_empty_changeset() throws Exception {
        assertEquals(NUM_CHANGESETS, number_of_persisted_changesets());
        OptimisticEventStream oes = (OptimisticEventStream) OptimisticEventStream
                .createWritable(BUCKET_ID, STREAM_ID, NUM_CHANGESETS, persistence);
        oes.commit(UUID.randomUUID());
        assertEquals(NUM_CHANGESETS, oes.version());
        assertEquals(NUM_CHANGESETS, number_of_persisted_changesets());
    }

    @Test
    public void test_commit_flushes_uncommitted_events() throws Exception {
        assertEquals(NUM_CHANGESETS, number_of_persisted_changesets());
        OptimisticEventStream oes = (OptimisticEventStream) OptimisticEventStream
                .createWritable(BUCKET_ID, STREAM_ID, NUM_CHANGESETS, persistence);
        oes.append(new Long(8));
        oes.commit(UUID.randomUUID());
        assertEquals(NUM_CHANGESETS + 1, oes.version());
        assertEquals(NUM_CHANGESETS + 1, number_of_persisted_changesets());
        oes.commit(UUID.randomUUID());
        assertEquals(NUM_CHANGESETS + 1, oes.version());
        assertEquals(NUM_CHANGESETS + 1, number_of_persisted_changesets());
    }

    @Test
    public void test_commit_with_concurrency_exception() throws Exception {
        assertEquals(NUM_CHANGESETS, number_of_persisted_changesets());
        OptimisticEventStream oes = (OptimisticEventStream) OptimisticEventStream
                .createWritable(BUCKET_ID, STREAM_ID, 10, persistence);
        oes.append(new Long(8));
        try {
            oes.commit(UUID.randomUUID());
            fail("Should have failed by now");
        } catch (ConcurrencyException e) {
            // expect this
        }
        assertEquals(10, oes.version());
    }

    @Test
    public void test_rollback() throws Exception {
        assertEquals(NUM_CHANGESETS, number_of_persisted_changesets());
        OptimisticEventStream oes = (OptimisticEventStream) OptimisticEventStream
                .createWritable(BUCKET_ID, STREAM_ID, NUM_CHANGESETS, persistence);
        oes.append(new Long(8));
        oes.rollback();
        assertEquals(NUM_CHANGESETS, oes.version());
        assertEquals(NUM_CHANGESETS, number_of_persisted_changesets());

        oes.commit(UUID.randomUUID()); // empty commit
        assertEquals(NUM_CHANGESETS, oes.version());
        assertEquals(NUM_CHANGESETS, number_of_persisted_changesets());

        oes.append(new Long(42));
        oes.commit(UUID.randomUUID());
        assertEquals(NUM_CHANGESETS + 1, oes.version());
        assertEquals(NUM_CHANGESETS + 1, number_of_persisted_changesets());
    }

    @Test
    public void test_rollback_followed_by_commit() throws Exception {
        assertEquals(NUM_CHANGESETS, number_of_persisted_changesets());
        OptimisticEventStream oes = (OptimisticEventStream) OptimisticEventStream
                .createWritable(BUCKET_ID, STREAM_ID, NUM_CHANGESETS, persistence);
        oes.append(new Long(8));
        oes.rollback();
        oes.append(new Long(127));
        assertEquals(NUM_CHANGESETS, oes.version());
        assertEquals(NUM_CHANGESETS, number_of_persisted_changesets());

        oes.commit(UUID.randomUUID());
        assertEquals(NUM_CHANGESETS + 1, oes.version());
        assertEquals(NUM_CHANGESETS + 1, number_of_persisted_changesets());

        // test that only last change added after rollback has been persisted
        List<ChangeSet> changes = IteratorUtils.toList(persistence.allChanges(BUCKET_ID));
        ChangeSet cs = changes.get(changes.size() - 1);
        List<Serializable> all = IteratorUtils.toList(cs.events());
        assertEquals(1, all.size());
        assertEquals(new Long(127), all.get(0));
    }

    @Test
    public void test_that_unpopulated_stream_throws() throws Exception {
        OptimisticEventStream oes = (OptimisticEventStream) OptimisticEventStream
                .createWritable(BUCKET_ID, STREAM_ID, NUM_CHANGESETS, persistence);
        oes.append(new Long(8));
        oes.commit(UUID.randomUUID());
        try {
            oes.events();
            fail("Should have failed by now");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    private void compareData(Iterator<Serializable> datait, Iterator<Serializable> streamit) {
        assertEquals(IteratorUtils.toList(datait), IteratorUtils.toList(streamit));
    }

    private void comparePersisted(
            List<ChangeSet> beforePersisted,
            long expectedVersion,
            List expectedEvents) {

        // check that the events have correctly been committed to the persistence
        List<ChangeSet> afterPersisted = IteratorUtils.toList(persistence.allChanges(BUCKET_ID));
        assertEquals(afterPersisted.size(), beforePersisted.size() + 1);
        ChangeSet persistedCS = afterPersisted.get(afterPersisted.size()-1);

        assertEquals(persistedCS.bucketId(), BUCKET_ID);
        assertEquals(persistedCS.streamId(), STREAM_ID);
        assertEquals(expectedVersion, persistedCS.streamVersion());
        List<Serializable> persistedEvents = IteratorUtils.toList(persistedCS.events());
        assertEquals(expectedEvents, persistedEvents);
    }
            
    private long number_of_persisted_changesets() {
        Iterator<ChangeSet> allit = persistence.allChanges(BUCKET_ID);
        List<ChangeSet> all = IteratorUtils.toList(allit);
        return all.size();
    }

}