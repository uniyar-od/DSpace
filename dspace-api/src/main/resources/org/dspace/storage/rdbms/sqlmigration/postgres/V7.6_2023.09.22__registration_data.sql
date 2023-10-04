--
-- The contents of this file are subject to the license and copyright
-- detailed in the LICENSE and NOTICE files at the root of the source
-- tree and available online at
--
-- http://www.dspace.org/license/
--

-----------------------------------------------------------------------------------
-- ALTER table registrationdata
-----------------------------------------------------------------------------------

SET @constraint_name = QUOTE_IDENT(
              SELECT DISTINCT constraint_name
              FROM information_schema.constraints
              WHERE table_schema = 'public'
              AND table_name = 'registrationdata'
              AND constraint_type = 'UNIQUE'
              AND column_list = 'email');

SET @command = 'ALTER TABLE public.registrationdata DROP CONSTRAINT public.' || @constraint_name;

SELECT @command;

EXECUTE IMMEDIATE @command;

ALTER TABLE registrationdata
ADD COLUMN registration_type VARCHAR(255);

ALTER TABLE registrationdata
ADD COLUMN net_id VARCHAR(64);

CREATE SEQUENCE  IF NOT EXISTS registrationdata_metadatavalue_seq START WITH 1 INCREMENT BY 1;

-----------------------------------------------------------------------------------
-- Creates table registrationdata_metadata
-----------------------------------------------------------------------------------

CREATE TABLE registrationdata_metadata (
  registrationdata_metadata_id INTEGER NOT NULL,
  registrationdata_id INTEGER,
  metadata_field_id INTEGER,
  text_value OID,
  CONSTRAINT pk_registrationdata_metadata PRIMARY KEY (registrationdata_metadata_id)
);

ALTER TABLE registrationdata_metadata
ADD CONSTRAINT FK_REGISTRATIONDATA_METADATA_ON_METADATA_FIELD
    FOREIGN KEY (metadata_field_id)
    REFERENCES metadatafieldregistry (metadata_field_id);

ALTER TABLE registrationdata_metadata
ADD CONSTRAINT FK_REGISTRATIONDATA_METADATA_ON_REGISTRATIONDATA
    FOREIGN KEY (registrationdata_id)
    REFERENCES registrationdata (registrationdata_id);
