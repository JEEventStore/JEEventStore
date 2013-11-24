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

package org.jeeventstore.persistence.jpa;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

/**
 * Static meta model for {@link EventStoreEntry} to be used with the JPA
 * criteria API.
 */
@StaticMetamodel(EventStoreEntry.class)
public class EventStoreEntry_ {

    public static volatile SingularAttribute<EventStoreEntry, Long> id;
    public static volatile SingularAttribute<EventStoreEntry, String> bucketId;
    public static volatile SingularAttribute<EventStoreEntry, String> streamId;
    public static volatile SingularAttribute<EventStoreEntry, Long> streamVersion;
    public static volatile SingularAttribute<EventStoreEntry, Long> persistedAt;
    public static volatile SingularAttribute<EventStoreEntry, String> changeSetId;
    public static volatile SingularAttribute<EventStoreEntry, String> body;

}
