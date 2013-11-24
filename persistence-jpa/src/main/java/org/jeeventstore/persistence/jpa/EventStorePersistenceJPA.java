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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.ConcurrencyException;
import org.jeeventstore.DuplicateCommitException;
import org.jeeventstore.EventStorePersistence;
import org.jeeventstore.EventSerializer;

/**
 * EventStorePersistence utilizing JPA.  
 * To be configured as a stateless EJB.
 * <p>
 * See {@code src/main/sql/*.sql} for suitable table definitions.
 * <p>
 * The following EJBs and services are expected to be injected:
 * <p>
 * {@code serializer} of type {@link EventSerializer} denotes the serialization
 *    strategy used to serialize objects before persisting them into the database
 * <p>
 * {@code entityManager} is the persistence context used to store events.
 * <p>
 * This EJB accepts the following configuration parameters:
 * <p>
 * Env-entry {@code fetchBatchSize} (optional, default: 500): The batch size for database fetches (number
 *   of rows to be retrieved in a single call).
 */
public class EventStorePersistenceJPA implements EventStorePersistence {

    private static final Logger log = Logger.getLogger(EventStorePersistenceJPA.class.getName());
 
    // injected by deployment descriptor
    private EntityManager entityManager;

    @EJB(name="serializer")
    private EventSerializer serializer;
    
    @Resource(name="fetchBatchSize")
    private Integer fetchBatchSize = 500;

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Iterator<ChangeSet> allChanges(final String bucketId) {
        if (bucketId == null)
            throw new IllegalArgumentException("bucketId must not be null");
        return fetchResults(new CriteriaQueryBuilder() {
            @Override
            public void addPredicates(CriteriaBuilder builder,
                    CriteriaQuery<?> query, Root<EventStoreEntry> root) {
                query.where(builder.equal(root.get(EventStoreEntry_.bucketId), bucketId));
            }
            @Override
            public void addOrderBy(CriteriaBuilder builder,
                    CriteriaQuery<?> query, Root<EventStoreEntry> root) {
                query.orderBy(builder.asc(root.get(EventStoreEntry_.id)));
            }
        });
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Iterator<ChangeSet> getFrom(
            final String bucketId, final String streamId,
            final long minVersion, final long maxVersion) {
        
        if (bucketId == null)
            throw new IllegalArgumentException("bucketId must not be null");
        if (streamId == null)
            throw new IllegalArgumentException("streamId must not be null");

        return fetchResults(streamQueryBuilder(bucketId, streamId, minVersion, maxVersion));
    }

    @Override
    public boolean existsStream(String bucketId, String streamId) {
        if (bucketId == null)
            throw new IllegalArgumentException("bucketId must not be null");
        if (streamId == null)
            throw new IllegalArgumentException("streamId must not be null");

        CriteriaQueryBuilder cqb = streamQueryBuilder(bucketId, streamId, 0, Long.MAX_VALUE);
        return QueryUtils.countResults(entityManager, cqb) > 0;
    }

    @Override
    public void persistChanges(ChangeSet changeSet) throws ConcurrencyException, DuplicateCommitException {
        if (changeSet == null)
            throw new IllegalArgumentException("changeSet must not be null");

        List<Serializable> list = new ArrayList<>();
        Iterator<Serializable> it = changeSet.events();
        while (it.hasNext())
            list.add(it.next());

        String body = serializer.serialize(list);
	log.log(Level.FINE, "writing {0}: {1}",
                new Object[]{changeSet.changeSetId(), body});
        EventStoreEntry entry = new EventStoreEntry(
                changeSet.bucketId(),
                changeSet.streamId(),
                changeSet.streamVersion(), 
                System.currentTimeMillis(),
                changeSet.changeSetId().toString(),
                body);
        entityManager.persist(entry);
        log.log(Level.FINE, "wrote ChangeSet {0} to event store, has id #{1}",
                new Object[]{changeSet.changeSetId(),
                    Long.toString(entry.id() == null ? -1 : entry.id())});
    }

    private CriteriaQueryBuilder streamQueryBuilder(
            final String bucketId, final String streamId,
            final long minVersion, final long maxVersion) {

        return new CriteriaQueryBuilder() {
            @Override
            public void addPredicates(CriteriaBuilder builder,
                    CriteriaQuery<?> query, Root<EventStoreEntry> root) {
                Predicate matchingBucketId = builder.equal(root.get(EventStoreEntry_.bucketId), bucketId);
                Predicate matchingStreamId = builder.equal(root.get(EventStoreEntry_.streamId), streamId);
                Predicate versionGt = builder.gt(root.get(EventStoreEntry_.streamVersion), minVersion);
                Predicate versionLet = builder.le(root.get(EventStoreEntry_.streamVersion), maxVersion);
                query.where(builder.and(matchingBucketId, matchingStreamId, versionGt, versionLet));
            }
            @Override
            public void addOrderBy(CriteriaBuilder builder,
                    CriteriaQuery<?> query, Root<EventStoreEntry> root) {
                query.orderBy(builder.asc(root.get(EventStoreEntry_.streamVersion)));
            }
        };
    }

    private Iterator<ChangeSet> fetchResults(CriteriaQueryBuilder cqb) {
        return new LazyLoadIterator(entityManager, cqb, serializer)
                .setFetchBatchSize(fetchBatchSize);
    }

}