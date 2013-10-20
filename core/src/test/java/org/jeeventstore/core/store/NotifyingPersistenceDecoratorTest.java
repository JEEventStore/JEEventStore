package org.jeeventstore.core.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jeeventstore.core.ChangeSet;
import org.jeeventstore.core.ConcurrencyException;
import org.jeeventstore.core.notifier.EventStoreCommitListener;
import org.jeeventstore.core.notifier.EventStoreCommitNotifier;
import org.jeeventstore.core.persistence.EventStorePersistence;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 *
 * @author Alexander Langer
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
        assertEquals(decorator.allChanges(), this.changeSetIterator);
    }

    @Test
    public void test_getFrom() {
        NotifyingPersistenceDecorator decorator = new NotifyingPersistenceDecorator(this, null);
        assertNull(this.changeSetIterator);
        assertEquals(decorator.allChanges(), this.changeSetIterator);
    }

    @Test
    public void test_persistChanges() {
        // hier auch auf notification testen
        NotifyingPersistenceDecorator decorator = new NotifyingPersistenceDecorator(
                this, new TestNotifier());
        assertNull(this.persistedChangeset);
        assertTrue(!this.received);
        ChangeSet cs = new TestChangeSet("blabla");
        try {
            decorator.persistChanges(cs);
        } catch (ConcurrencyException e) { }
        assertTrue(this.received);
        assertEquals(this.persistedChangeset, cs);
    }
    



    private class TestNotifier implements EventStoreCommitNotifier {
        @Override
        public void notifyListeners(ChangeSet changeSet) {
            received = true;
        }
        @Override
        public void addListener(EventStoreCommitListener listener) { }
        @Override
        public void removeListener(EventStoreCommitListener listener) { }
    }
    
    @Override
    public boolean existsStream(String bucketId, String streamId) {
        return this.existsStreamCalled = !this.existsStreamCalled;
    }

    @Override
    public Iterator<ChangeSet> allChanges() {
        List<ChangeSet> list = new ArrayList<>();
        list.add(new DefaultChangeSet(null, null, 1l, null, new ArrayList<Serializable>()));
        this.changeSetIterator = list.iterator();
        return this.changeSetIterator;
    }

    @Override
    public Iterator<ChangeSet> getFrom(String bucketId, String streamId, long minVersion, long maxVersion) {
        return this.allChanges();
    }

    @Override
    public void persistChanges(ChangeSet changeSet) throws ConcurrencyException {
        this.persistedChangeset = changeSet;
    }

}