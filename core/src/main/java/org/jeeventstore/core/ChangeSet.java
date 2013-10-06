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
