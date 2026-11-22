CREATE TABLE crop_types (
    code varchar(50) NOT NULL,
    label varchar(255) NOT NULL,
    description text,
    display_order integer NOT NULL,
    CONSTRAINT crop_types_code_pk PRIMARY KEY (code),
    CONSTRAINT crop_types_label_not_blank CHECK (btrim(label) <> ''),
    CONSTRAINT crop_types_display_order_unique UNIQUE (display_order)
);

-- Seed the catalog with the crop types currently recognized by the CropType
-- domain enum (the source of truth for validity). New crop types are added by
-- extending the enum and inserting the matching row here together.
INSERT INTO crop_types (code, label, description, display_order)
VALUES ('OLIVE', 'Olive grove', 'Olive tree cultivation for table olives and olive oil production.', 1);

-- Enforce referential integrity: a farm can only reference crop types that
-- exist in the catalog. Safe to add now because existing rows only hold enum
-- values already present in the catalog.
ALTER TABLE farm_crop_types
    ADD CONSTRAINT farm_crop_types_crop_type_fk
        FOREIGN KEY (crop_type) REFERENCES crop_types (code);
