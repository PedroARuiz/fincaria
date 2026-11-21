ALTER TABLE farms
    ADD COLUMN verification_status varchar(20) NOT NULL DEFAULT 'UNVERIFIED',
    ADD COLUMN cadastral_municipality varchar(255),
    ADD COLUMN cadastral_province varchar(255),
    ADD COLUMN cadastral_area_hectares double precision;

ALTER TABLE farms
    ADD CONSTRAINT farms_verification_status_check
        CHECK (verification_status IN ('VERIFIED', 'UNVERIFIED')),
    ADD CONSTRAINT farms_cadastral_details_consistency
        CHECK (
            (verification_status = 'VERIFIED'
                AND cadastral_municipality IS NOT NULL
                AND cadastral_province IS NOT NULL
                AND cadastral_area_hectares IS NOT NULL)
            OR (verification_status = 'UNVERIFIED'
                AND cadastral_municipality IS NULL
                AND cadastral_province IS NULL
                AND cadastral_area_hectares IS NULL)
        );
