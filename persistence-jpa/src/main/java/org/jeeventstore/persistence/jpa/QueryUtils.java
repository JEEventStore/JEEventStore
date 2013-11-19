package org.jeeventstore.persistence.jpa;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author Alexander Langer
 */
public class QueryUtils {

    public static TypedQuery<EventStoreEntry> buildQuery(
            EntityManager entityManager,
            CriteriaQueryBuilder cqb) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<EventStoreEntry> query = builder.createQuery(EventStoreEntry.class);
        Root<EventStoreEntry> root = query.from(EventStoreEntry.class);
        query.select(root);
        cqb.addPredicates(builder, query, root);
        cqb.addOrderBy(builder, query, root);
        return entityManager.createQuery(query);
    }

    public static Long countResults(EntityManager entityManager, CriteriaQueryBuilder cqb) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<EventStoreEntry> root = countQuery.from(EventStoreEntry.class);
        countQuery.select(builder.count(root));
        cqb.addPredicates(builder, countQuery, root);
        return entityManager.createQuery(countQuery).getSingleResult();
    }


    
}
