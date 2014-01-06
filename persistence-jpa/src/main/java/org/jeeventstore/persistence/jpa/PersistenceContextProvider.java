package org.jeeventstore.persistence.jpa;

import javax.persistence.EntityManager;

public interface PersistenceContextProvider {

    EntityManager entityManagerForReading(String bucketId);
    EntityManager entityManagerForWriting(String bucketId);
    
}
