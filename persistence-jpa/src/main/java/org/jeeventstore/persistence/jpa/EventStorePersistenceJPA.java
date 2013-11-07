package org.jeeventstore.persistence.jpa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jeeventstore.ChangeSet;
import org.jeeventstore.ConcurrencyException;
import org.jeeventstore.EventStorePersistence;
import org.jeeventstore.EventSerializer;

/**
 * EventStorePersistence utilizing JPA.  Stateless.
 * @author alex
 */
public class EventStorePersistenceJPA implements EventStorePersistence {

    private static final Logger log = Logger.getLogger(EventStorePersistenceJPA.class.getName());
 
    // to be injected by bean
    private EntityManager entityManager;

    @EJB
    private EventSerializer serializer;

    @PostConstruct
    public void init() {
        System.out.println("############# EM = " + entityManager);
    }

//
//    @Override
//    public Iterator<PersistedChangeSet> allChanges() {
//        List<JPAEventStoreEntry> results = persistenceContextProvider
//                .entityManager()
//                .createQuery("FROM "+JPAEventStoreEntry.class.getCanonicalName()+" e ORDER BY e.id",
//                    JPAEventStoreEntry.class)
//                .getResultList();
//        return Collections.unmodifiableList(this.buildList(results)).iterator();
//    }
//
//    @Override
//    public Iterator<PersistedChangeSet> getChangesInStream(
//            String bucketId,
//            String streamId,
//            long minVersion,
//            long maxVersion) {
//
//        List<JPAEventStoreEntry> results = persistenceContextProvider
//                .entityManager()
//                .createQuery("FROM "+JPAEventStoreEntry.class.getCanonicalName()+" e WHERE " +
//                    "e.bucketId = :bucketId AND " +
//                    "e.streamId = :streamId AND " +
//                    "e.streamVersion >= :minVersion AND " + 
//                    "e.streamVersion <= :maxVersion " + 
//                    "ORDER BY e.id", JPAEventStoreEntry.class)
//                .setParameter("bucketId", bucketId)
//                .setParameter("streamId", streamId)
//                .setParameter("minVersion", minVersion)
//                .setParameter("maxVersion", maxVersion)
//                .getResultList();
//        return Collections.unmodifiableList(this.buildList(results)).iterator();
//    }
//
//    private List<PersistedChangeSet> buildList(List<JPAEventStoreEntry> results) {
//        List<PersistedChangeSet> sets = new ArrayList<>();
//        for (JPAEventStoreEntry entry: results) {
//                List<EventMessage> messages = serializer.deserialize(entry.body());
//            sets.add(new StoredChangeSet(
//                    entry.bucketId(),
//                    entry.streamId(),
//                    entry.streamVersion(),
//                    UUID.fromString(entry.changeSetId()),
//                    entry.persistedAt(),
//                    messages));
//        }
//        return sets;
//    }
//
//    @Override
//    public void persistChanges(PersistableChangeSet changes) {
//	Validate.notNull(changes, "change must not be null");
//        String body = serializer.serialize(changes.events());
//	log.fine("writing " + changes.changeSetId() + ": " + body);
//        JPAEventStoreEntry entry = new JPAEventStoreEntry(
//                changes.bucketId(),
//                changes.streamId(),
//                changes.streamVersion() + 1,
//                System.currentTimeMillis(),
//                changes.changeSetId().toString(),
//                body);
//        persistenceContextProvider.entityManager().persist(entry);
//        log.info("wrote " + changes.changeSetId() + " to event store, has number #" + entry.id());
//    }
//
//    private Long queryCurrentStreamVersion(String bucketId, String streamId) {
//        return persistenceContextProvider
//                .entityManager()
//		.createQuery("SELECT MAX(e.streamVersion) FROM EventStoreJPAEntry e WHERE " +
//                    "e.bucketId = :bucketId AND " +
//                    "e.streamId = :streamId", Long.class)
//                .setParameter("bucketId", bucketId)
//                .setParameter("streamId", streamId)
//		.getSingleResult();
//    }
//
//    @Override
//    public boolean existsStream(String bucketId, String streamId) {
//        Long current = queryCurrentStreamVersion(bucketId, streamId);
//        return current != null;
//    }

    @Override
    public boolean existsStream(String bucketId, String streamId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<ChangeSet> allChanges() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<ChangeSet> getFrom(String bucketId, String streamId, long minVersion, long maxVersion) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void persistChanges(ChangeSet changeSet) throws ConcurrencyException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}