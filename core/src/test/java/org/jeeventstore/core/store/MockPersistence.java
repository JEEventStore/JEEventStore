package org.jeeventstore.core.store;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jeeventstore.core.ChangeSet;
import org.jeeventstore.core.ConcurrencyException;
import org.jeeventstore.core.persistence.EventStorePersistence;

/**
 *
 * @author Alexander Langer
 */
public class MockPersistence implements EventStorePersistence {

    private final String bucketId;
    private final String streamId;
    private final List<ChangeSet> changeSets = new ArrayList<>();

    public MockPersistence(String bucketId, String streamId) {
        this.bucketId = bucketId;
        this.streamId = streamId;
    }

    @Override
    public boolean existsStream(String bucketId, String streamId) {
        return true;
    }

    @Override
    public Iterator<ChangeSet> allChanges() {
        return changeSets.iterator();
    }

    @Override
    public Iterator<ChangeSet> getFrom(String bucketId, String streamId, long minVersion, long maxVersion) {
        if (!this.bucketId.equals(bucketId) || !this.streamId.equals(streamId))
            return new ArrayList<ChangeSet>().iterator();
        int max = Math.min(changeSets.size(), (int) maxVersion);
        int min = Math.min(changeSets.size(), (int) minVersion);
        return changeSets.subList(min, max).iterator();
    }

    @Override
    public void persistChanges(ChangeSet changeSet) throws ConcurrencyException {
        // version 0 -> changeset.size() == 0, i.e. wrong if size > 0 
        // version 1 -> size == 1
        // etc

        // we add the changeSet first, such that we can ignore the
        // exception in tests where we actually want to create an invalid stream.
        changeSets.add(changeSet);
        if (changeSets.size() != changeSet.streamVersion())
            throw new ConcurrencyException();
    }
    
}
