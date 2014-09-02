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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.ConcurrencyException;
import org.jeeventstore.DuplicateCommitException;
import org.jeeventstore.EventStorePersistence;
import org.jeeventstore.StreamNotFoundException;
import org.jeeventstore.store.TestChangeSet;

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
    public Iterator<ChangeSet> getFrom(String bucketId, String streamId, long minVersion, long maxVersion) 
            throws StreamNotFoundException {
        if (maxVersion == Long.MAX_VALUE)
            maxVersion = Integer.MAX_VALUE;
        int max = Math.min(changeSets.size(), (int) maxVersion);
        int min = Math.min(changeSets.size(), (int) minVersion);
        return changeSets.subList(min, max).iterator();
    }

    @Override
    public void persistChanges(ChangeSet changeSet) 
            throws ConcurrencyException, DuplicateCommitException {
        if (RESET.equals(changeSet.streamId())) {
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
        return new TestChangeSet("DEFAULT_BUCKET", RESET);
    }
    
}
