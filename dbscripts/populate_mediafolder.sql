
--
-- Selected TOC Entries:
--
--
-- Data for TOC Entry ID 2 (OID 19073)
--
-- Name: media_folder Type: TABLE DATA Owner: postgres
--


-- Disable triggers
UPDATE "pg_class" SET "reltriggers" = 0 WHERE "relname" = 'media_folder';

INSERT INTO "media_folder" VALUES (7,'openposting','20020329','','openposting','media anonymously uploaded','2002-03-29 14:45:53+01',NULL);
-- Enable triggers
UPDATE pg_class SET reltriggers = (SELECT count(*) FROM pg_trigger where pg_class.oid = tgrelid) WHERE relname = 'media_folder';

--
-- TOC Entry ID 1 (OID 18921)
--
-- Name: media_folder_id_seq Type: SEQUENCE SET Owner: 
--

SELECT setval ('"media_folder_id_seq"', 1, 't');

