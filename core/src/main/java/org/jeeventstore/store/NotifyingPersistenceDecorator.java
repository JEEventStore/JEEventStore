/*
 * Copyright (c) 2013 Red Rainbow IT Solutions GmbH, Germany
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

package org.jeeventstore.store;

import org.jeeventstore.ConcurrencyException;
import java.util.Iterator;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.DuplicateCommitException;
import org.jeeventstore.EventStoreCommitNotifier;
import org.jeeventstore.EventStorePersistence;
import org.jeeventstore.StreamNotFoundException;

/**
 * A decorator for {@link EventStorePersistence} that initiates a notification
 * of listeners at the configured {@link EventStoreCommitNotifier} when
 * changes are persisted.
 */
public class NotifyingPersistenceDecorator implements EventStorePersistence {

    private EventStorePersistence persistence;
    private EventStoreCommitNotifier notifier;

    public NotifyingPersistenceDecorator(
            EventStorePersistence persistence,
            EventStoreCommitNotifier notifier) {
        this.persistence = persistence;
        this.notifier = notifier;
    }

    @Override
    public boolean existsStream(String bucketId, String streamId) {
        return persistence.existsStream(bucketId, streamId);
    }

    @Override
    public Iterator<ChangeSet> allChanges(String bucketId) {
        return persistence.allChanges(bucketId);
    }

    @Override
    public Iterator<ChangeSet> getFrom(String bucketId, String streamId, long minVersion, long maxVersion) 
            throws StreamNotFoundException {
        return persistence.getFrom(bucketId, streamId, minVersion, maxVersion);
    }

    @Override
    public void persistChanges(ChangeSet changeSet) 
            throws ConcurrencyException, DuplicateCommitException {
        persistence.persistChanges(changeSet);
        notifier.notifyListeners(changeSet);
    }
    
}
