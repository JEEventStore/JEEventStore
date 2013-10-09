package org.jeeventstore.core.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.jeeventstore.core.ChangeSet;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

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
    public UUID changeSetId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<Serializable> events() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}