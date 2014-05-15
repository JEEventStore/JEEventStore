/*
 * Copyright (c) 2013-2014 Red Rainbow IT Solutions GmbH, Germany
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
 * A mocked ChangeSet used in unit tests.
 */
public class TestChangeSet implements ChangeSet {

    private String bucketId;
    private String id;

    public TestChangeSet(String bucketId, String id) {
        this.bucketId = bucketId;
        this.id = id;
    }

    @Override
    public String bucketId() {
        return this.bucketId;
    }

    @Override
    public String streamId() {
        return this.id;
    }

    @Override
    public long streamVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String changeSetId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<Serializable> events() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}