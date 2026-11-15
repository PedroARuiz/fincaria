CREATE TABLE farms (
    id uuid,
    user_id uuid NOT NULL,
    cadastral_reference varchar(20) NOT NULL,
    crop_type varchar(50) NOT NULL,
    CONSTRAINT farms_id_pk PRIMARY KEY (id),
    CONSTRAINT farms_user_id_fk FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT farms_cadastral_reference_length CHECK (length(cadastral_reference) = 20),
    CONSTRAINT farms_cadastral_reference_unique UNIQUE (cadastral_reference),
    CONSTRAINT farms_crop_type_not_blank CHECK (btrim(crop_type) <> '')
    );