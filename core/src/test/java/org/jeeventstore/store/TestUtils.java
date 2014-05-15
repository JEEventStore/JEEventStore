/*
 * Copyright (c) 2013-2014 Red Rainbow IT Solutions GmbH, Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
            ChangeSet cs = new DefaultChangeSet(bId, sId, i, UUID.randomUUID().toString(), data);
            list.add(cs);
        }
        return list;
    }

}
