package org.jeeventstore.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.ConcurrencyException;
import org.jeeventstore.DuplicateCommitException;
import org.jeeventstore.EventStorePersistence;
import org.jeeventstore.TestUTF8Utils;
import org.jeeventstore.store.DefaultChangeSet;
import org.jeeventstore.util.IteratorUtils;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 */
@Singleton
@LocalBean
@Startup
public class PersistenceTestHelper {

    @EJB(lookup = "java:global/test/ejb/EventStorePersistence")
    private EventStorePersistence persistence;

    private static final int COUNT_TEST = 150;

    private List<ChangeSet> data_default = new ArrayList<ChangeSet>();
    private List<ChangeSet> data_test = new ArrayList<ChangeSet>();

    @PostConstruct
    public void init() {
        System.out.println("Loading data @ " + this.getClass().getName());
        try {
            List<Integer> ids = new ArrayList<>();
            for (int i = 0; i < 103; i++)
                ids.add(i);
            for (int j = 0; j < 50; j++) { // increasing stream version
                // shuffle order of writes to mimic real-world behaviour (at least a bit)
                Collections.shuffle(ids);
                for (Integer i: ids) {
                    if (j > i % 37)
                        continue;
                    ChangeSet cs = new DefaultChangeSet(
                            "DEFAULT",
                            "TEST_" + i,
                            j,
                            UUID.randomUUID().toString(),
                            new ArrayList<Serializable>());
                    data_default.add(cs);
                    persistence.persistChanges(cs);
                }
            }
            for (int i = 0; i < 150; i++) {
                ChangeSet cs = new DefaultChangeSet(
                        "TEST",
                        "SINGLE_STREAM" + i,
                        i,
                        UUID.randomUUID().toString(),
                        new ArrayList<Serializable>());
                data_test.add(cs);
                persistence.persistChanges(cs);
            }
        } catch (ConcurrencyException | DuplicateCommitException e) {
            // ignore
        }
        System.out.println((data_default.size() + data_test.size()) + " entities loaded.");
    }

    public void test_allChanges_testBucket() {
        List<? extends Serializable> list = new ArrayList<Serializable>();
        Iterator<ChangeSet> it = persistence.allChanges("TEST");
        compare(it, data_test, false);
    }

    public void test_allChanges_inorder() {
        System.out.println("starteding test_allChanges_inorder");
        Iterator<ChangeSet> it = persistence.allChanges("DEFAULT");
        Map<String, Long> versions = new HashMap<>();
        int count = 0;
        while (it.hasNext()) {
            ChangeSet cs = it.next();
            assertEquals(cs.bucketId(), "DEFAULT");
            Long lastVersion = versions.get(cs.streamId());
            System.out.println ("Last in " + cs.streamId() + ": " + lastVersion + ", this: " + cs.streamVersion());
            if (lastVersion == null)
                lastVersion = 0l;
            assertTrue(lastVersion <= cs.streamVersion());
            versions.put(cs.streamId(), cs.streamVersion());
            count++;
        }
        assertEquals(count, data_default.size());
    }

    public void test_getFrom_regular() {
        List<ChangeSet> expected = filter("DEFAULT", "TEST_45", 0, Long.MAX_VALUE);
        Iterator<ChangeSet> have = persistence.getFrom("DEFAULT", "TEST_45", 0, Long.MAX_VALUE);
        compare(have, expected, true);
    }

    public void test_getFrom_substream() {
        List<ChangeSet> expected = filter("DEFAULT", "TEST_65", 4, 10);
        Iterator<ChangeSet> have = persistence.getFrom("DEFAULT", "TEST_65", 4, 10);
        compare(have, expected, true);
    }

    public void test_allChanges_nullarg() {
        try {
            persistence.allChanges(null);
            fail("Should have failed by now");
        } catch (EJBException e) {
            // expected
        }
    }

    public void test_getFrom_nullarg() {
        try {
            persistence.getFrom(null, "TEST", 0, Long.MAX_VALUE);
            fail("Should have failed by now");
        } catch (EJBException e) {
            // expected
        }
        try {
            persistence.getFrom("DEFAULT", null, 0, Long.MAX_VALUE);
            fail("Should have failed by now");
        } catch (EJBException e) {
            // expected
        }
    }

    public void test_large_event() {
        String streamId = UUID.randomUUID().toString();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 15000 / 32; i++)
            builder.append(UUID.randomUUID().toString());
        String body = builder.toString();

        store(streamId, streamId, body);
        String result = (String) load(streamId, streamId);
        assertEquals(body, result);
    }

    /**
     * Test against broken UTF8 encodings in the PostgreSQL/Hibernate combination,
     * when the @Lob annotation is used.
     * When database field is NOT of type TEXT 
     * see https://hibernate.atlassian.net/browse/HHH-6127
     */
    public void test_utf8_chars() {
        String streamId = UUID.randomUUID().toString();

        String body = "ÜÄÖüäöß / " 
                + TestUTF8Utils.unicodeString() + " / "
                + TestUTF8Utils.utf8String();
        store(streamId, streamId, body);
        String result = (String) load(streamId, streamId);
        TestUTF8Utils.println("####### Result as loaded from DB: " + result);
        assertEquals(body, result);
    }

    private List<ChangeSet> getFrom(String bucketId, String streamId) {
        assertTrue(persistence.existsStream(bucketId, streamId));
        Iterator<ChangeSet> it = persistence.getFrom(bucketId, streamId, 0, Long.MAX_VALUE);
        return IteratorUtils.toList(it);
    }

    private List<ChangeSet> filter(String bucketId, String streamId, long minVersion, long maxVersion) {
        List<ChangeSet> res = new ArrayList<>();
        for (ChangeSet cs: data_default) {
            if (!cs.bucketId().equals(bucketId))
                continue;
            if (!cs.streamId().equals(streamId))
                continue;
            res.add(cs);
        }
        if (maxVersion == Long.MAX_VALUE)
            maxVersion = Integer.MAX_VALUE - 1;
        int min = Math.min(res.size(), (int) minVersion + 1);
        int max = Math.min(res.size(), (int) maxVersion + 1);
        return res.subList(min, max);
    }

    private void store(String bucketId, String streamId, Serializable data) {
        List<Serializable> events = new ArrayList<>();
        events.add(data);
        try {
            ChangeSet cs = new DefaultChangeSet(
                    bucketId, streamId, 1, 
                    UUID.randomUUID().toString(),
                    events);
            persistence.persistChanges(cs);
        } catch (ConcurrencyException | DuplicateCommitException e) {
            throw new RuntimeException(e);
        }
    }

    private Serializable load(String bucketId, String streamId) {
        List<ChangeSet> sets = getFrom(streamId, streamId);
        assertEquals(1, sets.size());
        ChangeSet cs = sets.get(0);
        List<Serializable> result = IteratorUtils.toList(cs.events());
        assertEquals(1, result.size());
        return result.get(0);
    }

    private void compare(Iterator<ChangeSet> it, List<ChangeSet> data, boolean deepverify) {
        int count = 0;
        long lastVersion = 0;
        while (it.hasNext()) {
            assertTrue(count < data.size());
            ChangeSet found = it.next();
            ChangeSet wanted = data.get(count);
            assertTrue(lastVersion <= found.streamVersion());
            assertEquals(found.bucketId(), wanted.bucketId());
            if (deepverify) {
                assertEquals(found.streamId(), wanted.streamId());
                assertEquals(found.streamVersion(), wanted.streamVersion());
                assertEquals(found.changeSetId(), wanted.changeSetId());
                assertEquals(IteratorUtils.toList(found.events()),
                        IteratorUtils.toList(wanted.events()));
            }
            lastVersion = Math.max(lastVersion, found.streamVersion());
            count++;
        }
        assertEquals(count, data.size());
    }
    
}
