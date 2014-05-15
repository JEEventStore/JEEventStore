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

package org.jeeventstore.notifier;

import org.jeeventstore.EventStoreCommitListener;
import org.jeeventstore.EventStoreCommitNotification;
import org.jeeventstore.EventStoreCommitNotifier;
import java.util.UUID;
import org.jboss.arquillian.testng.Arquillian;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.store.TestChangeSet;
import static org.testng.Assert.*;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

public abstract class AbstractEventStoreCommitNotifierTest 
        extends Arquillian
        implements EventStoreCommitListener {

    public final static long WAIT_TIME = 1000l;
    public final static String DEFAULT_BUCKET = "BUCKET_ID";

    protected abstract EventStoreCommitNotifier instance();

    protected ChangeSet changeSet(String id) {
        return changeSet(DEFAULT_BUCKET, id);
    }

    protected ChangeSet changeSet(String bucketId, String commitId) {
        return new TestChangeSet(bucketId, commitId);
    }

    // these must be static to have them set from different threads
    protected static boolean caught = false;
    protected static ChangeSet caughtCS = null;

    @AfterTest
    public void cleanup() {
        this.clear();
    }

    @Override
    public void receive(EventStoreCommitNotification notification) {
        caught = true;
        caughtCS = notification.changes();
    }

    protected void clear() {
        sleep(WAIT_TIME);
        caught = false;
        caughtCS = null;
        try {
            this.instance().removeListener(DEFAULT_BUCKET, this);
        } catch (Exception e) {
            // might happen, since we are just cleaning up
        }
        assertFalse(caught);
        assertNull(caughtCS);
    }

    @Test
    public void test_add_and_remove_listener_works() {
        this.clear();
        EventStoreCommitNotifier instance = this.instance();
        instance.addListener(DEFAULT_BUCKET, this);
        instance.removeListener(DEFAULT_BUCKET, this);
    }

    @Test
    public void test_notification() {
        this.clear();
        EventStoreCommitNotifier instance = this.instance();
        instance.addListener(DEFAULT_BUCKET, this);
        String id = UUID.randomUUID().toString();
        ChangeSet cs = changeSet(id);
        instance.notifyListeners(cs);
        sleep(WAIT_TIME * 2);
        // must have received by now
        assertTrue(caught);
        assertNotNull(caughtCS);
        assertEquals(caughtCS.streamId(), id);
    }

    @Test
    public void test_notification_after_removal() {
        this.clear();
        EventStoreCommitNotifier instance = this.instance();
        instance.addListener(DEFAULT_BUCKET, this);
        instance.removeListener(DEFAULT_BUCKET, this);
        String id = UUID.randomUUID().toString();
        ChangeSet cs = changeSet(id);
        instance.notifyListeners(cs);
        sleep(WAIT_TIME * 2);
        // if delivering, it would have received by now
        assertFalse(caught);
        assertNull(caughtCS);
    }

    @Test
    public void test_notification_when_not_listening() {
        this.clear();
        EventStoreCommitNotifier instance = this.instance();
        String id = UUID.randomUUID().toString();
        ChangeSet cs = changeSet(id);
        instance.notifyListeners(cs);
        sleep(WAIT_TIME * 2);
        // if delivering, it would have received by now
        assertFalse(caught);
        assertNull(caughtCS);
    }

    @Test
    public void test_remove_nonlistener_fails() {
        this.clear();
        EventStoreCommitNotifier instance = this.instance();
        try {
            instance.removeListener(DEFAULT_BUCKET, this);
            fail("Should have failed by now");
        } catch (Exception e) {
            // ok
        }
    }

    @Test
    public void test_multiple_add_fails() {
        this.clear();
        EventStoreCommitNotifier instance = this.instance();
        instance.addListener(DEFAULT_BUCKET, this);
        try {
            instance.addListener(DEFAULT_BUCKET, this);
            fail("Should have failed by now");
        } catch (Exception e) {
            // ok
        }
    }

    @Test
    public void test_different_buckets() {
        TestListener[] l = new TestListener[3];
        for (int i = 0; i < 3; i++)
            l[i] = new TestListener();
        EventStoreCommitNotifier instance = this.instance();
        instance.addListener("BUCKET1", l[0]);
        instance.addListener("BUCKET1", l[2]);
        instance.addListener("BUCKET2", l[1]);
        instance.addListener("BUCKET2", l[2]);

        ChangeSet cs1 = changeSet("BUCKET1", UUID.randomUUID().toString());
        ChangeSet cs2 = changeSet("BUCKET2", UUID.randomUUID().toString());

        instance.notifyListeners(cs1);
        this.sleep(WAIT_TIME * 2);
        assertNotNull(l[0].notification);
        assertEquals(l[0].notification.changes(), cs1);
        assertTrue(l[1].notification == null);
        assertNotNull(l[2].notification);
        assertEquals(l[2].notification.changes(), cs1);

        for (int i = 0; i < 3; i++)
            l[0].notification = null;
        instance.notifyListeners(cs2);
        this.sleep(WAIT_TIME * 2);
        assertTrue(l[0].notification == null);
        assertNotNull(l[1].notification);
        assertEquals(l[1].notification.changes(), cs2);
        assertNotNull(l[2].notification);
        assertEquals(l[2].notification.changes(), cs2);
    }

    protected void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // ignore
        }
    }

}