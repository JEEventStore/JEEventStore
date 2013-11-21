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

package org.jeeventstore.persistence.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.EventSerializer;
import org.jeeventstore.store.DefaultChangeSet;

/**
 *
 * @author Alexander Langer
 */
public class LazyLoadIterator implements Iterator<ChangeSet> {

    private final static Logger log = Logger.getLogger(LazyLoadIterator.class.getName());

    private final EntityManager entityManager;
    private final TypedQuery<EventStoreEntry> query;
    private final EventSerializer serializer;
    private final long numResults;
    private int fetchBatchSize = 50;

    private int current = 0;
    private final List<EventStoreEntry> currentBatch = new ArrayList<>(fetchBatchSize);

    public LazyLoadIterator(
            EntityManager entityManager,
            CriteriaQueryBuilder cbq,
            EventSerializer serializer) {

        this.entityManager = entityManager;
        this.query = QueryUtils.buildQuery(entityManager, cbq);
        this.serializer = serializer;
        this.numResults = QueryUtils.countResults(entityManager, cbq);
    }

    @Override
    public boolean hasNext() {
        return current < numResults;
    }

    @Override
    public ChangeSet next() {
        if (!hasNext())
            throw new IllegalStateException("No next ChangeSet");
        int offset = current % fetchBatchSize;
        if (offset == 0l)
            fetch();
        EventStoreEntry entry = currentBatch.get(offset);
        current++;
        List<? extends Serializable> events = serializer.deserialize(entry.body());
        return new DefaultChangeSet(
                entry.bucketId(),
                entry.streamId(),
                entry.streamVersion(),
                UUID.fromString(entry.changeSetId()),
                events);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException(
                this.getClass().getSimpleName() +
                "#remove() is not supported .");
    }

    public LazyLoadIterator setFetchBatchSize(int fetchBatchSize) {
        this.fetchBatchSize = fetchBatchSize;
        return this;
    }
    
    private void fetch() {
        log.log(Level.FINE, "Fetching results {0}-{1} of {2}",
                new Object[]{
                    Integer.toString(current+1),
                    Long.toString(Math.min(numResults, fetchBatchSize * (current/fetchBatchSize + 1))),
                    Long.toString(numResults)});
        query.setMaxResults(fetchBatchSize);
        query.setFirstResult(current);
        List<EventStoreEntry> list = query.getResultList();

        currentBatch.clear();
        for (EventStoreEntry e : list) {
            entityManager.detach(e);
            currentBatch.add(e);
        }
    }
    
}