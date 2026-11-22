-- new
CREATE TABLE holds (
    id          BIGSERIAL PRIMARY KEY,
    event_id    BIGINT NOT NULL REFERENCES events(id),
    status      VARCHAR(20) NOT NULL,          -- HELD, PAID, EXPIRED
    expires_at  TIMESTAMPTZ NOT NULL
);
