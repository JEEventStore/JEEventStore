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
