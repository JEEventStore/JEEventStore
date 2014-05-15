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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jeeventstore.ChangeSet;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class EventsIteratorTest {

    @Test
    public void test_that_hasNext_is_false_at_end_of_changeset() {
        List<Integer> data = TestUtils.randomdata(1);
        ChangeSet cs = new DefaultChangeSet("BUCKET", "STREAM", 0, UUID.randomUUID().toString(), data);
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
            ChangeSet cs = new DefaultChangeSet("BUCKET", "STREAM", 0, UUID.randomUUID().toString(), data);
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
            ChangeSet cs = new DefaultChangeSet("BUCKET", "STREAM", 0, UUID.randomUUID().toString(), data);
        }
        EventsIterator eit = new EventsIterator(lcs.iterator());
        assertTrue(!eit.hasNext());
        TestUtils.compare(eit, lcs);
    }

}