--
-- The contents of this file are subject to the license and copyright
-- detailed in the LICENSE and NOTICE files at the root of the source
-- tree and available online at
--
-- http://www.dspace.org/license/
--

create table imp_feed_queries (imp_feed_queries_id integer not null primary key, checksum varchar2(64), query clob, 
	execution_time timestamp, num_insert integer, num_delete integer, feeder varchar2(255));
alter table imp_feed_queries add constraint uk_imp_feed_queries_checksum unique (checksum);
CREATE SEQUENCE imp_feed_queries_seq;
create table imp_values_toignore (imp_values_toignore_id integer not null primary key, 
	metadata varchar2(255), textvalue varchar2(255), note varchar2(255), creation_time timestamp);
CREATE SEQUENCE imp_values_toignore_seq;