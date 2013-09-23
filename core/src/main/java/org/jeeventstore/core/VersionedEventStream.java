package org.jeeventstore.core;

/**
 * A VersionedEventStream belongs to a bucket, has a unique
 * identifier within the bucket and a version number.
 * 
 * @author Alexander Langer
 */
public interface VersionedEventStream {

    /**
     * Identifies the bucket to which the EventStream belongs.
     * @return 
     */
    String bucketId();

    /**
     * Identifies the EventStream within the bucket.
     */
    String streamId();

    /**
     * The version of the EventStream.
     */
    long version();

}
