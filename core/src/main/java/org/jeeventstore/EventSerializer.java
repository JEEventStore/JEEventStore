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
import java.util.List;

/**
 * Provides the ability to serialize and deserialize events.
 * 
 * @author Alexander Langer
 */
public interface EventSerializer {
    
    /**
     * Serializes a list of events and returns a serialized representation.
     * 
     * @param events The list of events to be serialized.
     * @return A {@link String} representation of the serialized events.
     */
    String serialize(List<? extends Serializable> events);

    /**
     * Deserializes the String provided and reconstructs the corresponding 
     * list of events.
     * @param body The {@link String} from which the list of events will be reconstructed.
     * @return The reconstructed list.
     */
    List<? extends Serializable> deserialize(String body);

}