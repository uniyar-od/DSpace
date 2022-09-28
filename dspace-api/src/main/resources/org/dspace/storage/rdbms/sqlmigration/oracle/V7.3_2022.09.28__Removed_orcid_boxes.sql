--
-- The contents of this file are subject to the license and copyright
-- detailed in the LICENSE and NOTICE files at the root of the source
-- tree and available online at
--
-- http://www.dspace.org/license/
--

-- DELETE ORCID boxes
DELETE FROM cris_layout_box box where box.type in ('ORCID_SYNC_SETTINGS', 'ORCID_AUTHORIZATIONS', 'ORCID_SYNC_QUEUE');

-- DELETE empty cells
DELETE FROM cris_layout_cell cell 
where NOT EXISTS (SELECT 1 from cris_layout_box box where box.cell = cell.id);

-- DELETE empty rows
DELETE FROM cris_layout_row l_row 
where NOT EXISTS (SELECT 1 from cris_layout_cell cell where cell.row = l_row.id);

-- DELETE empty tabs
DELETE FROM cris_layout_tab tab
where NOT EXISTS (SELECT 1 from cris_layout_row l_row where l_row.tab = tab.id);