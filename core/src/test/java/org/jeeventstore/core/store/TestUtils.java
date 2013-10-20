package org.jeeventstore.core.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.jeeventstore.core.ChangeSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
}
