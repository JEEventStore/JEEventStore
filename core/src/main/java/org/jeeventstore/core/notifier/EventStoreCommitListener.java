package org.jeeventstore.core.notifier;

/**
 * An EventStoreCommitListener is awaiting notifications about
 * changes that have been committed to the event store.
 * 
 * @author Alexander Langer
 */
public interface EventStoreCommitListener {

    /**
     * Receive a notification that a change set has been committed to the
     * event store.
     * @param notification The actual notification.
     * This method must be thread-safe.
     */
    void receive(EventStoreCommitNotification notification);
    
}