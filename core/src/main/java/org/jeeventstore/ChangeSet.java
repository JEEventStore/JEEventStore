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

import java.io.Serializable;
import java.util.Iterator;

/**
 * Represents a series of events that form a single, consistent unit within an
 * event stream. 
 * The events within a ChangeSet are stored in an atomic operation: The events
 * in a ChangeSet can either be applied to the event stream as a whole, or
 * none of the events are applied.
 */
public interface ChangeSet extends Serializable {

    /**
     * Identifies the bucket to which the event stream belongs.
     * 
     * @return  the identifier
     */
    String bucketId();

    /**
     * Identifies the event stream to which this belongs.
     * 
     * @return  the identifier
     */
    String streamId();

    /**
     * Gets the version of the event stream to which this applies.
     * 
     * @return  the version
     */
    long streamVersion();

    /**
     * Gets the unique identifier for this ChangeSet within the stream.
     * 
     * @return the unique identifer
     */
    String changeSetId();
    
    /**
     * Gets an iterator to the events contained within this ChangeSet.
     * 
     * @return  the iterator
     */
    Iterator<Serializable> events();
    
}
