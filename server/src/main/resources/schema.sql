CREATE TABLE IF NOT EXISTS users
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR NOT NULL,
    email VARCHAR UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS requests
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    description VARCHAR NOT NULL,
    requestor_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    created TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS items
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR NOT NULL,
    description VARCHAR NOT NULL,
    available BOOLEAN NOT NULL,
    request_id BIGINT REFERENCES requests(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS bookings
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    item_id BIGINT REFERENCES items(id) ON DELETE CASCADE,
    booker_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR NOT NULL,
    start_time timestamp NOT NULL,
    end_time timestamp NOT NULL
);

CREATE TABLE IF NOT EXISTS comments
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    text VARCHAR NOT NULL,
    item_id BIGINT REFERENCES items(id) ON DELETE CASCADE,
    author_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    created TIMESTAMP NOT NULL
);