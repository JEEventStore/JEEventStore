package org.jeeventstore.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.testng.Arquillian;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.ConcurrencyException;
import org.jeeventstore.DuplicateCommitException;
import org.jeeventstore.EventStorePersistence;
import org.jeeventstore.TestUTF8Utils;
import org.jeeventstore.store.DefaultChangeSet;
import org.jeeventstore.util.IteratorUtils;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Alexander Langer
 */
public class AbstractPersistenceTest extends Arquillian {

    @EJB(lookup = "java:global/test/ejb/PersistenceTestHelper")
    private PersistenceTestHelper testHelper;

    @EJB(lookup = "java:global/test/ejb/EventStorePersistence")
    private EventStorePersistence persistence;

    protected EventStorePersistence getPersistence() {
        return this.persistence;
    }

    @Test
    public void test_allChanges_testBucket() {
        testHelper.test_allChanges_testBucket();
    }

    @Test
    public void test_allChanges_inorder() {
        testHelper.test_allChanges_inorder();
    }

    @Test
    public void test_getFrom_regular() {
        testHelper.test_getFrom_regular();
    }

    @Test
    public void test_getFrom_substream() {
        testHelper.test_getFrom_substream();
    }

    @Test
    public void test_optimistic_lock() throws DuplicateCommitException {
        ChangeSet cs = new DefaultChangeSet(
                "DEFAULT",
                "TEST_49",
                5, // exists
                UUID.randomUUID().toString(),
                new ArrayList<Serializable>());
        try {
            persistence.persistChanges(cs);
            fail("Should have failed by now");
        } catch (EJBException | ConcurrencyException e) {
            // expected
        }
    }

    @Test
    public void test_duplicate_commit() throws ConcurrencyException {
        String id = UUID.randomUUID().toString();
        try {
            for (int i = 0; i < 2; i++)
                persistence.persistChanges(new DefaultChangeSet(
                        "DEFAULT",
                        "TEST_DUPLICATE",
                        i + 1,
                        id,
                        new ArrayList<Serializable>()));
            fail("Should have failed by now");
        } catch (EJBException | DuplicateCommitException e) {
            // expected
        }

    }
    
    @Test
    public void test_allChanges_nullarg() {
        testHelper.test_allChanges_nullarg();
    }

    @Test
    public void test_getFrom_nullarg() {
        testHelper.test_getFrom_nullarg();
    }

    @Test
    public void test_existsStream_nullarg() {
        try {
            persistence.existsStream(null, "TEST");
            fail("Should have failed by now");
        } catch (EJBException e) {
            // expected
        }
        try {
            persistence.existsStream("DEFAULT", null);
            fail("Should have failed by now");
        } catch (EJBException e) {
            // expected
        }
    }

    @Test
    public void test_persistChanges_nullarg() throws ConcurrencyException, DuplicateCommitException {
        try {
            persistence.persistChanges(null);
            fail("Should have failed by now");
        } catch (EJBException e) {
            // expected
        }
    }

    @Test
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
    @Test
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
        List<ChangeSet> sets = testHelper.getFrom(streamId, streamId);
        assertEquals(1, sets.size());
        ChangeSet cs = sets.get(0);
        List<Serializable> result = IteratorUtils.toList(cs.events());
        assertEquals(1, result.size());
        return result.get(0);
    }

}
