ALTER TABLE users
    ADD COLUMN password_hash varchar NOT NULL,
    ADD CONSTRAINT users_password_hash_not_blank CHECK (btrim(password_hash) <> '');
