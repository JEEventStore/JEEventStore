package org.jeeventstore.core.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.jeeventstore.core.ChangeSet;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Alexander Langer
 */
public class EventsIteratorTest {

    private List<Integer> randomdata(int count) {
        Random rand = new Random();
        List<Integer> list = new ArrayList<>();
        while (count-- > 0)
            list.add(rand.nextInt());
        return list;
    }

    private void compare(EventsIterator has, List<ChangeSet> wants) {
        for (ChangeSet cs: wants) {
            Iterator<Serializable> eit = cs.events();
            while (eit.hasNext()) {
                Serializable ev = eit.next();
                assertTrue(has.hasNext());
                assertEquals(ev, has.next());
            }
        }
        assertTrue(!has.hasNext());
    }
    
    @Test
    public void test_that_hasNext_is_false_at_end_of_changeset() {
        List<Integer> data = randomdata(1);
        ChangeSet cs = new DefaultChangeSet(null, null, 0, UUID.randomUUID(), data);
        List<ChangeSet> lcs = new ArrayList<>();
        lcs.add(cs);
        EventsIterator eit = new EventsIterator(lcs.iterator());
        assertTrue(eit.hasNext());
        compare(eit, lcs);
    }

    @Test
    public void test_that_all_events_are_there_in_order() {
        List<ChangeSet> lcs = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            List<Integer> data = randomdata(i % 5);
            assertEquals(data.size(), i % 5);
            ChangeSet cs = new DefaultChangeSet(null, null, 0, UUID.randomUUID(), data);
        }
        EventsIterator eit = new EventsIterator(lcs.iterator());
        compare(eit, lcs);
    }

    @Test
    public void test_empty() {
        List<ChangeSet> lcs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<Integer> data = randomdata(0);
            assertEquals(data.size(), 0);
            ChangeSet cs = new DefaultChangeSet(null, null, 0, UUID.randomUUID(), data);
        }
        EventsIterator eit = new EventsIterator(lcs.iterator());
        assertTrue(!eit.hasNext());
        compare(eit, lcs);
    }

}