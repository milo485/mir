--
-- Selected TOC Entries:
--
--
-- Data for TOC Entry ID 2 (OID 46975)
--
-- Name: media_type Type: TABLE DATA Owner: postgres
--


-- \connect - de_indy

-- Disable triggers
UPDATE "pg_class" SET "reltriggers" = 0 WHERE "relname" = 'media_type';

INSERT INTO "media_type" VALUES (4,'mp3','audio/mp3','Mp3','Audio',NULL);
INSERT INTO "media_type" VALUES (13,'mp3','audio/x-mp3','Mp3','Audio',NULL);
INSERT INTO "media_type" VALUES (14,'mp3','audio/x-mpeg','Mp3','Audio',NULL);
INSERT INTO "media_type" VALUES (10,'ra','audio/vnd.rn-realaudio','RealAudio','Audio',NULL);
INSERT INTO "media_type" VALUES (12,'ra','audio/x-pn-realaudio','RealAudio','Audio',NULL);
INSERT INTO "media_type" VALUES (11,'rm','video/vnd.rn-realvideo','RealVideo','Video',NULL);
INSERT INTO "media_type" VALUES (8,'mov','video/quicktime','Video','Video',NULL);
INSERT INTO "media_type" VALUES (7,'mpg','video/mpeg','Video','Video',NULL);
INSERT INTO "media_type" VALUES (9,'avi','video/x-msvideo','Video','Video',NULL);
INSERT INTO "media_type" VALUES (6,'pdf','application/pdf','Generic','Other',NULL);
INSERT INTO "media_type" VALUES (15,'png','image/png','ImagesPng','Images',NULL);
INSERT INTO "media_type" VALUES (3,'jpg','- deprecated -','Images','Images',NULL);
INSERT INTO "media_type" VALUES (5,'jpg','image/*','ImagesJpeg','Images',NULL);
INSERT INTO "media_type" VALUES (16,'asf','video/x-ms-asf','Video','Video',NULL);
INSERT INTO "media_type" VALUES (17,'rm','application/vnd.rn-realmedia','RealVideo','Video',NULL);
INSERT INTO "media_type" VALUES (18,'mp3','audio/mpeg','Mp3','Audio',NULL);
INSERT INTO "media_type" VALUES (19,'png','image/gif','ImagesPng','Images',NULL);
INSERT INTO "media_type" VALUES (20,'avi','video/avi','Video','Video',NULL);
-- Enable triggers
UPDATE pg_class SET reltriggers = (SELECT count(*) FROM pg_trigger where pg_class.oid = tgrelid) WHERE relname = 'media_type';

--
-- TOC Entry ID 1 (OID 46923)
--
-- Name: media_type_id_seq Type: SEQUENCE SET Owner: 
--

SELECT setval ('"media_type_id_seq"', 1, 't');

