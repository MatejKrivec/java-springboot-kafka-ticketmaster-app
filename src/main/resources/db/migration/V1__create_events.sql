CREATE TABLE events (
                        id           BIGINT PRIMARY KEY,
                        name         VARCHAR(200) NOT NULL,
                        tickets_left INT NOT NULL CHECK (tickets_left >= 0)
);

INSERT INTO events (id, name, tickets_left) VALUES (1, 'Oasis Live', 10);