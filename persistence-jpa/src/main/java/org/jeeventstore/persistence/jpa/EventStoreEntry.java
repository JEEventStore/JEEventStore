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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

/**
 * JPA EventStore Entry.
 * @author alex
 */
@Entity
@Table(name = "event_store", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"bucket_id", "stream_id", "stream_version"}),
    @UniqueConstraint(columnNames = {"change_set_id"})
})
public class EventStoreEntry implements Serializable {

    /**
     * ID of the entry.  Can be used to enumerate ALL Events.
     */
    @SequenceGenerator(name="event_store_id_seq", sequenceName="event_store_id_seq", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="event_store_id_seq")
    @Column(name = "id", updatable = false)
    @Id
    private Long id;

    @Column(name = "bucket_id")
    @NotNull
    private String bucketId;

    @Column(name = "stream_id")
    @NotNull
    private String streamId;

    @Column(name = "stream_version")
    private long streamVersion;

    @Column(name="persisted_at")
    private long persistedAt;

    @Column(name = "change_set_id")
    @NotNull
    private String changeSetId;

    /*
     * We cannot use @Lob, because it will cause Hibernate to map the field
     * in ascii-only mode when used with PostreSQL, which leads to broken
     * UTF8 Strings.  Both parties refuse to fix this isse, see
     * https://groups.google.com/forum/#!topic/pgsql.interfaces.jdbc/g4XXAL-a5tE
     * http://in.relation.to/15492.lace
     * https://hibernate.atlassian.net/browse/HHH-6127
     *
     * We therefore fix the length at 32,672 , which is the maximum VARCHAR
     * size in Apache Derby, the database used for unit tests.  In production
     * mode you are expected to manually create the table with the correct
     * length and field types.  See src/main/sql/*.sql for table definitions.
     */
    @Column(name = "body", length = 32672)
    private String body;

    public EventStoreEntry(
            String bucketId,
            String streamId,
            long streamVersion,
            long persistedAt,
            String changeSetId,
            String body) {

        if (bucketId == null || bucketId.isEmpty())
            throw new IllegalArgumentException("bucketId must not be empty");
        if (streamId == null || streamId.isEmpty())
            throw new IllegalArgumentException("streamId must not be empty");
        if (changeSetId == null || changeSetId.isEmpty())
            throw new IllegalArgumentException("changeSetId must not be empty");
        if (body == null || body.isEmpty())
            throw new IllegalArgumentException("body must not be empty");

        this.bucketId = bucketId;
        this.streamId = streamId;
        this.streamVersion = streamVersion;
        this.persistedAt = persistedAt;
        this.changeSetId = changeSetId;
        this.body = body;
    }

    public Long id() {
        return id;
    }

    public String bucketId() {
        return bucketId;
    }

    public String streamId() {
        return streamId;
    }

    public long streamVersion() {
        return streamVersion;
    }

    public long persistedAt() {
        return persistedAt;
    }

    public String changeSetId() {
        return changeSetId;
    }

    public String body() {
        return body;
    }

    /**
     * Required for JPA, do not use
     * @deprecated 
     */
    @Deprecated
    protected EventStoreEntry() { }
    
}