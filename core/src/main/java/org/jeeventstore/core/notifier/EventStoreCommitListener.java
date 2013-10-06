package org.jeeventstore.core.notifier;

/**
 * An EventStoreCommitListener is awaiting notifications about
 * changes that have been committed to the event store.
 * @author Alexander Langer
 */
public interface EventStoreCommitListener {

    void receive(EventStoreCommitNotification notification);
    
}