create sequence cris_unpaywall_seq;
ALTER TABLE public.cris_unpaywall
RENAME COLUMN "timestampCreated" TO "timestampcreated";
ALTER TABLE public.cris_unpaywall
RENAME COLUMN "timestampLastModified" TO "timestamplastmodified";
ALTER TABLE public.cris_unpaywall
    ADD COLUMN record text;
ALTER TABLE public.cris_unpaywall RENAME unpaywall_id  TO id;
ALTER TABLE public.cris_unpaywall
   ADD COLUMN resource_id integer NOT NULL;
ALTER TABLE public.cris_unpaywall
  ADD CONSTRAINT "UNIQUE" UNIQUE (resource_id);
