package org.jeeventstore.persistence.jpa;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.persistence.EntityManager;

public class SimplePersistenceContextProvider implements PersistenceContextProvider {

    // injected by deployment descriptor
    private EntityManager entityManager;

    @PostConstruct
    public void init() {
        if (entityManager == null)
            throw new IllegalStateException("No EntityManager has been injected");
    }

    @Override
    @Lock(LockType.READ)
    public EntityManager entityManagerForReading(String bucketId) {
        return entityManager;
    }

    @Override
    @Lock(LockType.READ)
    public EntityManager entityManagerForWriting(String bucketId) {
        return entityManager;
    }

}
