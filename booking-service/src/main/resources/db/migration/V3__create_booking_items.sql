CREATE TABLE IF NOT EXISTS booking_items
(
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id),
    ticket_id BIGINT NOT NULL REFERENCES tickets(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT unique_ticket_per_booking UNIQUE(booking_id, ticket_id)
);