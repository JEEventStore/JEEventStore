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

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Utility functions for use with the JPA {@link CriteriaQuery} API.
 */
class QueryUtils {

    /**
     * Create a new query for fetching objects from the database.
     * Uses {@link CriteriaQueryBuilder#addPredicates} and
     * {@link CriteriaQueryBuilder#addOrderBy}
     * 
     * @param entityManager  the {@link EntityManager} that manages the {@link EventStoreEntry} entity
     * @param cqb  the criteria query builder
     * @return  the built query, ready for retrieving results
     */
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

    /**
     * Counts the number of entities (rows) matching the criteria specified
     * in the given {@link CriteriaQueryBuilder}.
     * Uses {@link CriteriaQueryBuilder#addPredicates} only.
     * 
     * @param entityManager  the {@link EntityManager} used to count the number
     *      of matching {@link EventStoreEntry} entities
     * @param cqb  the criteria query builder
     * @return  the number of entities that matched the criteria
     */
    public static Long countResults(EntityManager entityManager, CriteriaQueryBuilder cqb) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<EventStoreEntry> root = countQuery.from(EventStoreEntry.class);
        countQuery.select(builder.count(root));
        cqb.addPredicates(builder, countQuery, root);
        return entityManager.createQuery(countQuery).getSingleResult();
    }


    
}
