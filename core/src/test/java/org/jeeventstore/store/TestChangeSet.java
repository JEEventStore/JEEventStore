package org.jeeventstore.store;

import java.io.Serializable;
import java.util.Iterator;
import org.jeeventstore.ChangeSet;

/**
 * A mocked ChangeSet used in unit tests.
 */
public class TestChangeSet implements ChangeSet {

    private String id;

    public TestChangeSet(String id) {
        this.id = id;
    }

    @Override
    public String bucketId() {
        return this.id;
    }

    @Override
    public String streamId() {
        throw new UnsupportedOperationException("Not supported yet.");
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