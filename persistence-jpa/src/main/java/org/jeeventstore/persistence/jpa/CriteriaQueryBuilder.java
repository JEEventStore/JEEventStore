package org.jeeventstore.persistence.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author Alexander Langer
 */
public interface CriteriaQueryBuilder {

    public void addPredicates(CriteriaBuilder builder,
            CriteriaQuery<?> criteria, Root<EventStoreEntry> root);

    public void addOrderBy(CriteriaBuilder builder,
            CriteriaQuery<?> criteria, Root<EventStoreEntry> root);
    
}
