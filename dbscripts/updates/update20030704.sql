-- update script 2003-07-04 by Zapata
-- * makes the password field longer in webdb_users
-- * adds a comments field to webdb_users;
-- This script will terminate with an error the second time it's run,
--   so running this script when it isn't needed can't do any harm.
--

BEGIN TRANSACTION;

ALTER TABLE "webdb_users" RENAME TO "webdb_users_old";
DROP INDEX "webdb_users_pkey";
DROP INDEX "idx_webdb_user_log_pas";
  
CREATE TABLE "webdb_users" (
	"id" integer DEFAULT nextval('webdb_users_id_seq'::text) NOT NULL,
	"login" character varying(16) NOT NULL,
	"password" character varying(255) NOT NULL,
	"is_admin" boolean DEFAULT '0' NOT NULL,
	"comment" text,
	Constraint "webdb_users_pkey" Primary Key ("id")
);

INSERT INTO "webdb_users" (
  "id", 
  "login", 
  "password",
  "is_admin"
)
SELECT
  "id", 
  "login", 
  "password",
  "is_admin"
FROM "webdb_users_old";

UPDATE  pg_class
SET
  relowner = (SELECT relowner FROM pg_class WHERE relname='webdb_users_old'),
  relacl =   (SELECT relacl FROM pg_class WHERE relname='webdb_users_old')
WHERE 
  relname = 'webdb_users';

DROP TABLE "webdb_users_old";

DROP INDEX "idx_webdb_user_log_pas_is_admin" on "webdb_users" using btree ( "login" "varchar_ops", "password" "varchar_ops", "is_admin" "bool_ops" );
  
-- that's it!
  
COMMIT TRANSACTION;
