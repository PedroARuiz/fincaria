CREATE TABLE farm_crop_types (
    farm_id uuid NOT NULL,
    crop_type varchar(50) NOT NULL,
    CONSTRAINT farm_crop_types_pk PRIMARY KEY (farm_id, crop_type),
    CONSTRAINT farm_crop_types_farm_id_fk FOREIGN KEY (farm_id) REFERENCES farms (id)
);

INSERT INTO farm_crop_types (farm_id, crop_type)
SELECT id, crop_type FROM farms;

ALTER TABLE farms DROP COLUMN crop_type;
