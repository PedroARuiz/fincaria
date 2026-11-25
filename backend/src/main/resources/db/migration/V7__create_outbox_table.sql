CREATE TABLE outbox (
    id uuid NOT NULL,
    aggregate_type varchar(50) NOT NULL,
    aggregate_id varchar(64) NOT NULL,
    event_type varchar(100) NOT NULL,
    payload jsonb NOT NULL,
    occurred_at timestamptz NOT NULL,
    published_at timestamptz,
    CONSTRAINT outbox_pk PRIMARY KEY (id),
    CONSTRAINT outbox_aggregate_type_not_blank CHECK (btrim(aggregate_type) <> ''),
    CONSTRAINT outbox_event_type_not_blank CHECK (btrim(event_type) <> '')
);

-- The relay polls for unpublished events ordered by when they occurred. A
-- partial index over only the pending rows keeps it small: once an event is
-- published its `published_at` is set and the row drops out of the index, so
-- the index only ever holds the backlog still to publish, not the full
-- history of every event ever emitted.
CREATE INDEX outbox_pending_idx ON outbox (occurred_at) WHERE published_at IS NULL;
