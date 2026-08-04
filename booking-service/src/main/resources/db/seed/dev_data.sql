TRUNCATE TABLE bookings, tickets, booking_items RESTART IDENTITY CASCADE;

INSERT INTO bookings (id, buyer_email, status, expires_at)
VALUES
    (1, 'buyer5@demo.com','PAYMENT_SUCCEEDED', null),
    (2, 'buyer6@demo.com','IN_PROGRESS', (now() + interval '30 minutes'));

INSERT INTO tickets (id, event_id, seat, status, price)
VALUES
    (1, 1, 'A1', 'SOLD',    50.00),
    (2, 1, 'A2', 'SOLD',    50.00),
    (3, 1, 'A3', 'AVAILABLE', 50.00),
    (4, 1, 'A4', 'AVAILABLE', 50.00),
    (5, 1, 'A5', 'AVAILABLE', 50.00),
    (6, 1, 'A6', 'AVAILABLE', 50.00),
    (7, 1, 'A7', 'RESERVED', 50.00),
    (8, 1, 'A8', 'AVAILABLE', 50.00),
    (9, 2, 'A1', 'SOLD',    50.00),
    (10, 2, 'A2', 'AVAILABLE',    55.00);

INSERT INTO booking_items (booking_id, ticket_id)
VALUES
    (1, 1),
    (1, 2),
    (2, 7);