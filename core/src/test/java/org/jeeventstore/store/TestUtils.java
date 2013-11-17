package org.jeeventstore.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.jeeventstore.ChangeSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.collections.Lists;

/**
 *
 * @author Alexander Langer
 */
public class TestUtils {

    public static List<Integer> randomdata(int count) {
        Random rand = new Random();
        List<Integer> list = new ArrayList<>();
        while (count-- > 0)
            list.add(rand.nextInt());
        return list;
    }

    public static void compare(EventsIterator has, List<ChangeSet> wants) {
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

    public static List<ChangeSet> createChangeSets(String bId, String sId, int minversion, int maxversion) {
        List<ChangeSet> list = new ArrayList<>();
        for (int i = minversion; i <= maxversion; i++) {
            List<Integer> data = TestUtils.randomdata(i % (maxversion - minversion + 5));
            ChangeSet cs = new DefaultChangeSet(bId, sId, i, UUID.randomUUID(), data);
            list.add(cs);
        }
        return list;
    }

}
