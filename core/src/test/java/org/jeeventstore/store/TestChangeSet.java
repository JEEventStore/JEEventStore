package org.jeeventstore.store;

import java.io.Serializable;
import java.util.Iterator;
import org.jeeventstore.ChangeSet;

/**
 * A mocked ChangeSet used in unit tests.
 */
public class TestChangeSet implements ChangeSet {

    private String bucketId;
    private String id;

    public TestChangeSet(String bucketId, String id) {
        this.bucketId = bucketId;
        this.id = id;
    }

    @Override
    public String bucketId() {
        return this.bucketId;
    }

    @Override
    public String streamId() {
        return this.id;
    }

    @Override
    public long streamVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String changeSetId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<Serializable> events() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}