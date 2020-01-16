CREATE TABLE public.cris_unpaywall
(
  doi text,
  "timestampCreated" timestamp without time zone,
  "timestampLastModified" timestamp without time zone,
  unpaywall_id integer NOT NULL,
  CONSTRAINT unpaywall_pk PRIMARY KEY (unpaywall_id)
)