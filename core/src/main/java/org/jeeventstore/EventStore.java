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

package org.jeeventstore;

/**
 * The EventStore orchestrates the creation of event streams.
 * This is the main entry point to clients of the event store.
 */
public interface EventStore {

    /**
     * Tests whether the stream identified by {@code streamId} exists in the
     * bucket identified by {@code bucketId}.
     * 
     * @param bucketId  the identifier of the bucket to which the stream belongs
     * @param streamId  the identifier of the stream that is tested for existence
     * @return  whether the specified stream exists
     */
    boolean existsStream(String bucketId, String streamId);

    /**
     * Opens the latest version of the stream identified by {@code streamId} in the
     * bucket identified by {@code bucketId} for reading.
     * 
     * @param bucketId  the identifier of the bucket to which the stream belongs
     * @param streamId  the identifier of the stream that is opened for reading
     * @return  the requested event stream as readable event stream
     * @throws StreamNotFoundException  if a stream with the given identifier cannot be found in the bucket
     */
    ReadableEventStream openStreamForReading(String bucketId, String streamId)
            throws StreamNotFoundException;

    /**
     * Opens the stream identified by {@code streamId} in the
     * bucket identified by {@code bucketId} for reading, but only with
     * a maximum version of {@code maxVersion}.
     * Can be used to load previous versions of the event stream for reading.
     * If {@code maxVersion} is larger than the latest stream version,
     * the latest available version of the stream will be loaded.
     * 
     * @param bucketId  the identifier of the bucket to which the stream belongs
     * @param streamId  the identifier of the stream that is opened for reading
     * @param maxVersion  the requested version or {@code Long.MAX_VALUE} to load the latest version
     * @return  the requested event stream as readable event stream
     * @throws StreamNotFoundException  if a stream with the given identifier cannot be found in the bucket
     */
    ReadableEventStream openStreamForReading(String bucketId, String streamId, long maxVersion)
            throws StreamNotFoundException;

    /**
     * Creates a new stream with identifier {@code streamId} in the bucket
     * identified by {@code bucketId}.
     * 
     * 
     * @param bucketId  the identifier of the bucket to which the stream belongs
     * @param streamId  the identifier of the stream that is created
     * @return  the requested event stream as writable event stream
     */
    WritableEventStream createStream(String bucketId, String streamId);

    /**
     * Open the version {@code version} of the stream identified by {@code streamId}
     * in the bucket identified by {@code bucketId} for writing.
     * 
     * @param bucketId  the identifier of the bucket to which the stream belongs
     * @param streamId  the identifier of the stream that is opened for reading
     * @param version   the requested version of the stream
     * @return  the requested event stream as writable event stream
     */
    WritableEventStream openStreamForWriting(String bucketId, String streamId, long version);

}