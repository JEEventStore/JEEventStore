package org.jeeventstore.persistence;

import org.jeeventstore.EventStorePersistence;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.ConcurrencyException;
import org.jeeventstore.store.DefaultChangeSet;
import org.jeeventstore.store.TestUtils;
import static org.junit.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Alexander Langer
 */
public class MockPersistenceTest {

    private EventStorePersistence persistence;

    @BeforeMethod
    public void init() {
        this.persistence = new MockPersistence();
        List<ChangeSet> data = TestUtils.createChangeSets("", "", 1, 100);
        try {
            for (ChangeSet cs : data)
                persistence.persistChanges(cs);
        } catch (ConcurrencyException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test_reset() throws Exception {
        Iterator<ChangeSet> it = persistence.allChanges("DEFAULT");
        assertTrue(it.hasNext());
        persistence.persistChanges(MockPersistence.resetCommand());
        it = persistence.allChanges("DEFAULT");
        assertTrue(!it.hasNext());
    }

    @Test
    public void test_getFromZero() {
        Iterator<ChangeSet> it = persistence.getFrom("", "", 0, 10);
        verify(it, 1, 10);
        assertTrue(!it.hasNext());
    }

    @Test
    public void test_getAllFromZero() {
        Iterator<ChangeSet> it = persistence.getFrom("", "", 0, Integer.MAX_VALUE);
        verify(it, 1, 100);
        assertTrue(!it.hasNext());
    }

    @Test
    public void test_getFrom() {
        Iterator<ChangeSet> it = persistence.getFrom("", "", 34, 57);
        verify(it, 35, 57);
    }

    @Test
    public void test_getFrom_Max() {
        Iterator<ChangeSet> it = persistence.getFrom("", "", 34, Integer.MAX_VALUE);
        verify(it, 35, 100);
        assertTrue(!it.hasNext());
    }

    private void verify(Iterator<ChangeSet> it, int from, int to) {
        for (int i = from; i <= to; i++) {
            assertTrue(it.hasNext());
            ChangeSet cs = it.next();
            assertEquals(i, cs.streamVersion());
        }
    }

    @Test
    private void testConcurrencyFailure() {
        List<Integer> data = TestUtils.randomdata(10);
        ChangeSet cs = new DefaultChangeSet("", "", 100, UUID.randomUUID(), data);
        try {
            persistence.persistChanges(cs);
            fail("Should have failed by now");
        } catch (ConcurrencyException e) {
            // ok
        }
    }

}