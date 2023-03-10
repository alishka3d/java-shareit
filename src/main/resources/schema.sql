DROP TABLE IF EXISTS
    users,
    requests,
    items,
    bookings,
    comments;

CREATE TABLE IF NOT EXISTS users
(
    user_id   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_name VARCHAR(50) NOT NULL,
    email     VARCHAR(50) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS requests
(
    requests_id  INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    description  VARCHAR(255) NOT NULL,
    requestor_id INTEGER  REFERENCES users (user_id) NOT NULL
    );

CREATE TABLE IF NOT EXISTS items
(
    item_id     INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    item_name   VARCHAR(128) NOT NULL,
    description VARCHAR(256) NOT NULL,
    available   BOOLEAN NOT NULL,
    owner_id    INTEGER REFERENCES users (user_id) NOT NULL,
    request_id  INTEGER REFERENCES requests (requests_id)
    );

CREATE TABLE IF NOT EXISTS bookings
(
    booking_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    item_id    INTEGER REFERENCES items (item_id),
    booker_id  INTEGER REFERENCES users (user_id),
    status     VARCHAR(50) NOT NULL
    );

CREATE TABLE IF NOT EXISTS comments
(
    comment_id   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    comment_text VARCHAR(256) NOT NULL,
    item_id      INTEGER REFERENCES items (item_id),
    author_id    INTEGER REFERENCES users (user_id),
    created      TIMESTAMP WITHOUT TIME ZONE NOT NULL
    );