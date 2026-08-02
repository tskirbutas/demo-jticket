TRUNCATE TABLE events RESTART IDENTITY CASCADE;

INSERT INTO events (id, name, description, start_date)
VALUES
    (
        1,
        'Coldplay Live',
        'Music concert',
        '2026-08-15 20:00:00'
    ),
    (
     2,
        'Rock Festival',
        'Three day festival',
        '2026-09-01 18:00:00'
    );
