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

package org.jeeventstore.store;

import java.io.Serializable;
import java.util.Iterator;
import org.jeeventstore.ChangeSet;

/**
 * An iterator that iterates over all events for multiple {@link ChangeSet}s
 * in a proper sequence.
 */
public class EventsIterator<T extends ChangeSet> implements Iterator<Serializable> {

    private Iterator<T> cit;
    private Iterator<Serializable> it = null;

    public EventsIterator(Iterator<T> cit) {
        this.cit = cit;
        advance();
    }
    
    private void advance() {
        if (it != null && it.hasNext())
            return;
        // now either it == null or it has no next
        do {
            // already reached the last change set?
            if (!cit.hasNext()) {
                it = null;
                return;
            }
            // no, another changeset is available.  Grab it.
            ChangeSet nextcs = cit.next();
            it = nextcs.events();
        } while (!it.hasNext()); // protect against ChangeSets w/o events
    }

    @Override
    public boolean hasNext() {
        return it != null;
    }

    @Override
    public Serializable next() {
        Serializable n = it.next();
        advance();
        return n;
    }

    /**
     * {@link #remove()} is not supported.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported.");
    }
    
}
