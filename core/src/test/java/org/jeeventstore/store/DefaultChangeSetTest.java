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
import java.util.UUID;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.util.IteratorUtils;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class DefaultChangeSetTest {

    private final static String BUCKET_ID = "TEST_BUCKET";
    private final static String STREAM_ID = "TEST_STREAM";
    private final static long STREAM_VERSION = 28;
    private final static String CHANGE_SET_ID = UUID.randomUUID().toString();
    private final static List<Serializable> EVENTS = new ArrayList<Serializable>();
    
    private ChangeSet get() {
        if (EVENTS.isEmpty())
            EVENTS.add("Hello!");
        return new DefaultChangeSet(BUCKET_ID, STREAM_ID, STREAM_VERSION, CHANGE_SET_ID, EVENTS);
    }

    @Test
    public void test_that_values_are_valid() {
        ChangeSet cs = get();
        assertEquals(cs.bucketId(), BUCKET_ID);
        assertEquals(cs.streamId(), STREAM_ID);
        assertEquals(cs.streamVersion(), STREAM_VERSION);
        assertEquals(cs.changeSetId(), CHANGE_SET_ID);
        assertEquals(IteratorUtils.toList(cs.events()), IteratorUtils.toList(EVENTS.iterator()));
        assertTrue(cs.events().hasNext());
    }

    @Test
    public void test_that_events_are_readonly() {
        ChangeSet cs = get();
        Iterator<Serializable> it = cs.events();
        assertTrue(it.hasNext());
        it.next();
        try {
            it.remove();
            fail("Should have failed by now");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void test_invalid_args() {
        try {
            new DefaultChangeSet(null, STREAM_ID, STREAM_VERSION, CHANGE_SET_ID, EVENTS);
            fail("Should have failed by now");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            new DefaultChangeSet(BUCKET_ID, null, STREAM_VERSION, CHANGE_SET_ID, EVENTS);
            fail("Should have failed by now");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            new DefaultChangeSet(BUCKET_ID, STREAM_ID, -5, CHANGE_SET_ID, EVENTS);
            fail("Should have failed by now");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // must work
        new DefaultChangeSet(BUCKET_ID, STREAM_ID, 0, CHANGE_SET_ID, EVENTS);

        try {
            new DefaultChangeSet(BUCKET_ID, STREAM_ID, STREAM_VERSION, null, EVENTS);
            fail("Should have failed by now");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            new DefaultChangeSet(BUCKET_ID, STREAM_ID, STREAM_VERSION, CHANGE_SET_ID, null);
            fail("Should have failed by now");
        } catch (NullPointerException e) {
            // expected
        }

    }

}
