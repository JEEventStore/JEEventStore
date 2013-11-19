package org.jeeventstore.persistence.jpa;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

/**
 *
 * @author Alexander Langer
 */
@StaticMetamodel(EventStoreEntry.class)
public class EventStoreEntry_ {

    public static volatile SingularAttribute<EventStoreEntry, Long> id;
    public static volatile SingularAttribute<EventStoreEntry, String> bucketId;
    public static volatile SingularAttribute<EventStoreEntry, String> streamId;
    public static volatile SingularAttribute<EventStoreEntry, Long> streamVersion;
    public static volatile SingularAttribute<EventStoreEntry, Long> persistedAt;
    public static volatile SingularAttribute<EventStoreEntry, String> changeSetId;
    public static volatile SingularAttribute<EventStoreEntry, String> body;

}
