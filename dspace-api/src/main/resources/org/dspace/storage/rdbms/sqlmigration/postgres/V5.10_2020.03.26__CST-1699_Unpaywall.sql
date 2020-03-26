create sequence cris_unpaywall_seq;
ALTER TABLE public.cris_unpaywall
RENAME COLUMN "timestampCreated" TO "timestampcreated";
ALTER TABLE public.cris_unpaywall
RENAME COLUMN "timestampLastModified" TO "timestamplastmodified";
ALTER TABLE public.cris_unpaywall
    ADD COLUMN record text;