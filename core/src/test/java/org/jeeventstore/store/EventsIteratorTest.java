package org.jeeventstore.store;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jeeventstore.ChangeSet;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Alexander Langer
 */
public class EventsIteratorTest {

    @Test
    public void test_that_hasNext_is_false_at_end_of_changeset() {
        List<Integer> data = TestUtils.randomdata(1);
        ChangeSet cs = new DefaultChangeSet(null, null, 0, UUID.randomUUID(), data);
        List<ChangeSet> lcs = new ArrayList<>();
        lcs.add(cs);
        EventsIterator eit = new EventsIterator(lcs.iterator());
        assertTrue(eit.hasNext());
        TestUtils.compare(eit, lcs);
    }

    @Test
    public void test_that_all_events_are_there_in_order() {
        List<ChangeSet> lcs = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            List<Integer> data = TestUtils.randomdata(i % 5);
            assertEquals(data.size(), i % 5);
            ChangeSet cs = new DefaultChangeSet(null, null, 0, UUID.randomUUID(), data);
        }
        EventsIterator eit = new EventsIterator(lcs.iterator());
        TestUtils.compare(eit, lcs);
    }

    @Test
    public void test_empty() {
        List<ChangeSet> lcs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<Integer> data = TestUtils.randomdata(0);
            assertEquals(data.size(), 0);
            ChangeSet cs = new DefaultChangeSet(null, null, 0, UUID.randomUUID(), data);
        }
        EventsIterator eit = new EventsIterator(lcs.iterator());
        assertTrue(!eit.hasNext());
        TestUtils.compare(eit, lcs);
    }

}