UPDATE "pg_class" SET "reltriggers" = 0 WHERE "relname" !~ '^pg_';
-- \connect - postgres
INSERT INTO "rights" VALUES (0,'keine angabe',NULL);
INSERT INTO "rights" VALUES (1,'indymedia','rechte liegen bei indymedia');
INSERT INTO "rights" VALUES (2,'rechte bei autor/innen',NULL);
INSERT INTO "rights" VALUES (3,'frei ','keiner hat rechte dran');
BEGIN TRANSACTION;
CREATE TEMP TABLE "tr" ("tmp_relname" name, "tmp_reltriggers" smallint);
INSERT INTO "tr" SELECT C."relname", count(T."oid") FROM "pg_class" C, "pg_trigger" T WHERE C."oid" = T."tgrelid" AND C."relname" !~ '^pg_' GROUP BY 1;
UPDATE "pg_class" SET "reltriggers" = TMP."tmp_reltriggers" FROM "tr" TMP WHERE "pg_class"."relname" = TMP."tmp_relname";
COMMIT TRANSACTION;
