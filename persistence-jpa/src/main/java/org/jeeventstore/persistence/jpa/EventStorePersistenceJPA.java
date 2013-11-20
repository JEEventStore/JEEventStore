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
 * EventStorePersistence utilizing JPA.  Stateless.
 * @author alex
 */
public class EventStorePersistenceJPA implements EventStorePersistence {

    private static final Logger log = Logger.getLogger(EventStorePersistenceJPA.class.getName());
 
    // injected by deployment descriptor
    private EntityManager entityManager;

    @EJB(name="serializer")
    private EventSerializer serializer;
    
    @Resource(name="fetchBatchSize")
    private Integer fetchBatchSize = 100;

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
                query.orderBy(
                        builder.asc(root.get(EventStoreEntry_.streamVersion)),
                        builder.asc(root.get(EventStoreEntry_.id)));
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