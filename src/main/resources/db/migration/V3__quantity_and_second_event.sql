-- new
-- How many tickets a hold contains (existing holds count as 1)
ALTER TABLE holds ADD COLUMN quantity INT NOT NULL DEFAULT 1 CHECK (quantity > 0);

-- A second event, so we can test several events
INSERT INTO events (id, name, tickets_left) VALUES (2, 'Jazz Night', 20);
