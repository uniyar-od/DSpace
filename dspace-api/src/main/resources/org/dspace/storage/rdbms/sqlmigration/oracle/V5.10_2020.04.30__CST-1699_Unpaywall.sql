CREATE TABLE cris_unpaywall
(
  id number(10,0) not null,  
  doi varchar2(255 char),
  itemId number(10,0) not null unique,
  jsonRecord clob,
  timestampCreated TIMESTAMP(6),
  timestampLastModified TIMESTAMP(6),  
  PRIMARY KEY (id)
);

CREATE sequence CRIS_UNPAYWALL_SEQ;
