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

    // must use columnDefinition = TEXT, because @Lob will cause Hibernate to
    // create a somewhat broken binary (=ascii-only) column (OID)
    // in Postgres, which leads to broken UTF8 encodings
    @Column(name = "body") // , columnDefinition = "TEXT")
    @NotNull
    private String body;

    public EventStoreEntry(
            String bucketId,
            String streamId,
            long streamVersion,
            long persistedAt,
            String changeSetId,
            String body) {

        //Validate.notEmpty(bucketId, "bucketId must not be empty");
        //Validate.notEmpty(streamId, "streamId must not be empty");
        //Validate.notEmpty(changeSetId, "changeSetId must not be empty");
        //Validate.notEmpty(body, "body must not be empty");

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