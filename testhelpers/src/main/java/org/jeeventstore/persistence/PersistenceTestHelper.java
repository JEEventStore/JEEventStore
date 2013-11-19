package org.jeeventstore.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
import org.jeeventstore.store.DefaultChangeSet;
import org.jeeventstore.util.IteratorUtils;
import static org.testng.Assert.*;

/**
 *
 * @author Alexander Langer
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
                            UUID.randomUUID(),
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
                        UUID.randomUUID(),
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
        Iterator<ChangeSet> it = persistence.allChanges("DEFAULT");
        int count = 0;
        long lastVersion = 0;
        while (it.hasNext()) {
            ChangeSet cs = it.next();
            assertTrue(lastVersion <= cs.streamVersion());
            assertEquals(cs.bucketId(), "DEFAULT");
            lastVersion = Math.max(lastVersion, cs.streamVersion());
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

 
    // null arguments test fuer alle funktionen, muessen IllegalArgumentException werfen
    
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
