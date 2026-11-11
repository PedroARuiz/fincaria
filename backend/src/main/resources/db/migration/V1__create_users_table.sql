CREATE TABLE users (
    id uuid,
    name varchar NOT NULL,
    email varchar(254) NOT NULL,
    CONSTRAINT users_id_pk PRIMARY KEY (id),
    CONSTRAINT users_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT users_email_not_blank CHECK (btrim(email) <> ''),
    CONSTRAINT users_email_unique UNIQUE (email)
    );