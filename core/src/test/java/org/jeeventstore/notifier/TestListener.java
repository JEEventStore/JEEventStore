package org.jeeventstore.notifier;

import org.jeeventstore.EventStoreCommitListener;
import org.jeeventstore.EventStoreCommitNotification;

/**
 *
 */
public class TestListener implements EventStoreCommitListener {

    public EventStoreCommitNotification notification;

    @Override
    public void receive(EventStoreCommitNotification notification) {
        System.out.println(this + ": received notification: " + notification);
        this.notification = notification;
    }
    
}
