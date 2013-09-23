package org.jeeventstore.core;

import java.io.Serializable;
import java.util.Iterator;
import java.util.UUID;

/**
 * A ChangeSet represents a series of events that form a single, consistent
 * unit within the given event stream. The events within a ChangeSet are
 * stored in an atomic operation: The events in a ChangeSet can either be
 * applied to the event stream as a whole, or none of the events are applied.
 * 
 * @author Alexander Langer
 */
public interface ChangeSet extends Serializable {

    /**
     * Identifies the bucket to which the ChangeSet and the event stream belong.
     * @return 
     */
    String bucketId();

    /**
     * Identifies the event stream to which the ChangeSet belongs.
     */
    String streamId();

    /**
     * The version of the event stream to which this ChangeSet applies.
     */
    long streamVersion();

    /**
     * A unique identifier for this ChangeSet within the stream.
     */
    UUID changeSetId();
    
    /**
     * Gets an iterator to the events contained within this ChangeSet.
     */
    Iterator<Serializable> events();
    
}
