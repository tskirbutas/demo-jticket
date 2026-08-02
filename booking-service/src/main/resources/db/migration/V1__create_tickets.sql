CREATE TABLE IF NOT EXISTS tickets
(
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT,
    seat VARCHAR (255) NOT NULL,
    status VARCHAR(20) CHECK (status IN ('AVAILABLE', 'RESERVED', 'SOLD')),
    price NUMERIC(10,2),

    CONSTRAINT unique_event_seat UNIQUE(event_id, seat)
);