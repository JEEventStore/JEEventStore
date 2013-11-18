package org.jeeventstore.persistence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.ConcurrencyException;
import org.jeeventstore.DuplicateCommitException;
import org.jeeventstore.EventStorePersistence;
import org.jeeventstore.store.TestChangeSet;

/**
 *
 * @author Alexander Langer
 */
public class MockPersistence implements EventStorePersistence {

    private static final String RESET = "RESET";
    private final List<ChangeSet> changeSets = new ArrayList<>();

    public MockPersistence() { }

    @Override
    public boolean existsStream(String bucketId, String streamId) {
        return !changeSets.isEmpty();
    }

    @Override
    public Iterator<ChangeSet> allChanges(String bucketId) {
        return changeSets.iterator();
    }

    @Override
    public Iterator<ChangeSet> getFrom(String bucketId, String streamId, long minVersion, long maxVersion) {
        if (maxVersion == Long.MAX_VALUE)
            maxVersion = Integer.MAX_VALUE;
        int max = Math.min(changeSets.size(), (int) maxVersion);
        int min = Math.min(changeSets.size(), (int) minVersion);
        return changeSets.subList(min, max).iterator();
    }

    @Override
    public void persistChanges(ChangeSet changeSet) 
            throws ConcurrencyException, DuplicateCommitException {
        if (RESET.equals(changeSet.bucketId())) {
            this.cleanup();
            return;
        }
        // version 0 -> changeset.size() == 0, i.e. wrong if size > 0 
        // version 1 -> size == 1
        // etc

        // we add the changeSet first, such that we can ignore the
        // exception in tests where we actually want to create an invalid stream.
        changeSets.add(changeSet);
        if (changeSets.size() != changeSet.streamVersion())
            throw new ConcurrencyException();
    }

    private void cleanup() {
        changeSets.clear();
    }

    public static ChangeSet resetCommand() {
        return new TestChangeSet(RESET);
    }
    
}
