UPDATE "pg_class" SET "reltriggers" = 0 WHERE "relname" !~ '^pg_';
-- \connect - postgres
INSERT INTO "img_type" VALUES (0,'--');
INSERT INTO "img_type" VALUES (1,'Plakat');
INSERT INTO "img_type" VALUES (2,'Photo');
INSERT INTO "img_type" VALUES (3,'Grafik');
INSERT INTO "img_type" VALUES (4,'Kartenmaterial');
BEGIN TRANSACTION;
CREATE TEMP TABLE "tr" ("tmp_relname" name, "tmp_reltriggers" smallint);
INSERT INTO "tr" SELECT C."relname", count(T."oid") FROM "pg_class" C, "pg_trigger" T WHERE C."oid" = T."tgrelid" AND C."relname" !~ '^pg_' GROUP BY 1;
UPDATE "pg_class" SET "reltriggers" = TMP."tmp_reltriggers" FROM "tr" TMP WHERE "pg_class"."relname" = TMP."tmp_relname";
COMMIT TRANSACTION;
