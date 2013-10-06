package org.jeeventstore.core.notifier;

import org.jeeventstore.core.ChangeSet;

/**
 * An EventStoreCommitNotification is used to inform interested
 * parties about changes that have been committed to the event store.
 * @author Alexander Langer
 */
public interface EventStoreCommitNotification {

    ChangeSet changes();
    
}
