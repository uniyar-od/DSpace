CREATE TABLE IF NOT EXISTS cris_unpaywall 
(
	id int4 not null, 
	doi varchar(255), 
	itemId int4 unique,
	jsonRecord text,
	timestampCreated timestamp without time zone, 
	timestampLastModified timestamp without time zone,	 
	primary key (id)
);

create sequence CRIS_UNPAYWALL_SEQ;