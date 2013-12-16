package org.jeeventstore.store;

import org.jeeventstore.ConcurrencyException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.DuplicateCommitException;
import org.jeeventstore.EventStoreCommitListener;
import org.jeeventstore.EventStoreCommitNotifier;
import org.jeeventstore.EventStorePersistence;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 */
public class NotifyingPersistenceDecoratorTest implements EventStorePersistence {

    private boolean received;
    private boolean existsStreamCalled;
    private Iterator<ChangeSet> changeSetIterator;
    private ChangeSet persistedChangeset;
    
    @BeforeMethod(alwaysRun = true)
    public void clear() {
        received = false;
        existsStreamCalled = false;
        changeSetIterator = null;
        persistedChangeset = null;
    }

    @Test
    public void test_existsStream() {
        NotifyingPersistenceDecorator decorator = new NotifyingPersistenceDecorator(this, null);
        assertEquals(existsStreamCalled, false);
        assertEquals(decorator.existsStream("FOO", "BAR"), true);
        assertEquals(existsStreamCalled, true);
        assertEquals(decorator.existsStream("FOO", "BAR"), false);
        assertEquals(existsStreamCalled, false);
    }

    @Test
    public void test_allChanges() {
        NotifyingPersistenceDecorator decorator = new NotifyingPersistenceDecorator(this, null);
        assertNull(this.changeSetIterator);
        assertEquals(decorator.allChanges("DUMMY"), this.changeSetIterator);
    }

    @Test
    public void test_getFrom() {
        NotifyingPersistenceDecorator decorator = new NotifyingPersistenceDecorator(this, null);
        assertNull(this.changeSetIterator);
        assertEquals(decorator.getFrom(null, null, 0, Long.MAX_VALUE), this.changeSetIterator);
    }

    @Test
    public void test_persistChanges() throws DuplicateCommitException {
        NotifyingPersistenceDecorator decorator = new NotifyingPersistenceDecorator(
                this, new TestNotifier());
        assertNull(this.persistedChangeset);
        assertTrue(!this.received);
        ChangeSet cs = new TestChangeSet("TEST_BUCKET", "blabla");
        try {
            decorator.persistChanges(cs);
        } catch (ConcurrencyException e) { }
        assertTrue(this.received);
        assertEquals(this.persistedChangeset, cs);
    }

    @Test
    public void test_no_notification_on_exception() throws DuplicateCommitException {
        NotifyingPersistenceDecorator decorator = new NotifyingPersistenceDecorator(
                this, new TestNotifier());
        assertNull(this.persistedChangeset);
        assertTrue(!this.received);
        try {
            decorator.persistChanges(null);
            fail("Should have failed by now");
        } catch (ConcurrencyException e) {
            // expected
        }
        assertTrue(!this.received);
    }
    
    private class TestNotifier implements EventStoreCommitNotifier {
        @Override
        public void notifyListeners(ChangeSet changeSet) {
            received = true;
        }
        @Override
        public void addListener(String bucketId, EventStoreCommitListener listener) { }
        @Override
        public void removeListener(String bucketId, EventStoreCommitListener listener) { }
    }
    
    @Override
    public boolean existsStream(String bucketId, String streamId) {
        return this.existsStreamCalled = !this.existsStreamCalled;
    }

    @Override
    public Iterator<ChangeSet> allChanges(String bucketId) {
        List<ChangeSet> list = new ArrayList<>();
        list.add(new DefaultChangeSet("TEST", "FOO", 1l, UUID.randomUUID().toString(), new ArrayList<Serializable>()));
        this.changeSetIterator = list.iterator();
        return this.changeSetIterator;
    }

    @Override
    public Iterator<ChangeSet> getFrom(String bucketId, String streamId, long minVersion, long maxVersion) {
        return this.allChanges(bucketId);
    }

    @Override
    public void persistChanges(ChangeSet changeSet) throws ConcurrencyException {
        if (changeSet == null)
            throw new ConcurrencyException();
        this.persistedChangeset = changeSet;
    }

}