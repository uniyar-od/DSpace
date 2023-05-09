--
-- The contents of this file are subject to the license and copyright
-- detailed in the LICENSE and NOTICE files at the root of the source
-- tree and available online at
--
-- http://www.dspace.org/license/
--

do $$
begin
	
insert into doi
SELECT 
	nextval('doi_seq'),
	identifier_doi,
	(select id from entity_type where label = 'Publication'),
	item_id,
	(
		CASE
			When response_code = '999' OR response_code = '201' THEN 3 ELSE 1
		END
	),
	(select uuid from item where item.item_id = olddoi.item_id)
from old_doi2item as olddoi;

exception when others then
 
    raise notice 'The transaction is in an uncommittable state. '
                     'Transaction was rolled back';
 
    raise notice 'Yo this is good! --> % %', SQLERRM, SQLSTATE;
end;
$$ language 'plpgsql';