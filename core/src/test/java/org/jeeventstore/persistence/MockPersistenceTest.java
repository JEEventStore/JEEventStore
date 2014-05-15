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

package org.jeeventstore.persistence;

import org.jeeventstore.EventStorePersistence;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.ConcurrencyException;
import org.jeeventstore.DuplicateCommitException;
import org.jeeventstore.store.DefaultChangeSet;
import org.jeeventstore.store.TestUtils;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MockPersistenceTest {

    private EventStorePersistence persistence;

    @BeforeMethod
    public void init() {
        this.persistence = new MockPersistence();
        List<ChangeSet> data = TestUtils.createChangeSets("", "", 1, 100);
        try {
            for (ChangeSet cs : data)
                persistence.persistChanges(cs);
        } catch (ConcurrencyException | DuplicateCommitException e) {
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
    private void testConcurrencyFailure() throws DuplicateCommitException {
        List<Integer> data = TestUtils.randomdata(10);
        ChangeSet cs = new DefaultChangeSet("", "", 100, UUID.randomUUID().toString(), data);
        try {
            persistence.persistChanges(cs);
            fail("Should have failed by now");
        } catch (ConcurrencyException e) {
            // ok
        }
    }

}