
CREATE SEQUENCE event_store_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE event_store (
  id bigint NOT NULL,
  bucket_id character varying(255),
  stream_id character varying(255),
  stream_version bigint,
  change_set_id character varying(255),
  persisted_at bigint,
  body text,
  CONSTRAINT event_store_pkey PRIMARY KEY (id),
  CONSTRAINT unq_event_store_optimistic_lock UNIQUE (bucket_id, stream_id, stream_version),
  CONSTRAINT unq_event_store_change_set UNIQUE (change_set_id)
)
WITH (
  OIDS=FALSE
);

-- Set the owner to the correct user
-- ALTER TABLE event_store_id_seq OWNER TO someusername;
-- ALTER TABLE event_store OWNER TO someusername;
