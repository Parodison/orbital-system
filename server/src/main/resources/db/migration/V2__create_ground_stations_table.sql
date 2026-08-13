CREATE TABLE ground_stations (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    added_at TIMESTAMP NOT NULL,
    status JSONB NOT NULL,

    CONSTRAINT pk_ground_stations PRIMARY KEY (id)
);
