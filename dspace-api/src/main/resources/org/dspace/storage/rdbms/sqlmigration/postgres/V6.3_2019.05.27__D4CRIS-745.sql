--
-- The contents of this file are subject to the license and copyright
-- detailed in the LICENSE and NOTICE files at the root of the source
-- tree and available online at
--
-- http://www.dspace.org/license/
--

do $$
begin

--migrate imp_record
ALTER TABLE imp_record RENAME COLUMN imp_eperson_id to imp_eperson_legacy_id;
ALTER TABLE imp_record RENAME COLUMN imp_collection_id to imp_collection_legacy_id;
ALTER TABLE imp_record ADD COLUMN imp_eperson_id varchar(256);
ALTER TABLE imp_record ADD COLUMN imp_collection_id varchar(256);
UPDATE imp_record SET imp_eperson_id = (SELECT eperson.uuid FROM eperson WHERE imp_record.imp_eperson_legacy_id = eperson.eperson_id);
UPDATE imp_record SET imp_collection_id = (SELECT collection.uuid FROM collection WHERE imp_record.imp_collection_legacy_id = collection.collection_id);

ALTER TABLE imp_record DROP COLUMN imp_eperson_legacy_id;
ALTER TABLE imp_record DROP COLUMN imp_collection_legacy_id;

-- migrate imp_record_to_item
ALTER TABLE imp_record_to_item RENAME COLUMN imp_item_id to imp_item_legacy_id;
ALTER TABLE imp_record_to_item ADD COLUMN imp_item_id varchar(256);
UPDATE imp_record_to_item SET imp_item_id = (SELECT item.uuid FROM item WHERE imp_record_to_item.imp_item_legacy_id = item.item_id);
ALTER TABLE imp_record_to_item DROP COLUMN imp_item_legacy_id;

exception when others then
 
    raise notice 'The transaction is in an uncommittable state. '
                     'Transaction was rolled back';
 
    raise notice 'Yo this is good! --> % %', SQLERRM, SQLSTATE;
end;
$$ language 'plpgsql';