-- update script 2003-04-09 by Zapata
-- * creates table comment_x_media, to support media in comments
-- * adds webdb_lastchange to comment
-- This script will the second time this script is run,
--   so running this script when it isn't needed can't do any harm.
--
-- IMPORTANT: after running this script, run the update_all_sequences script as well

BEGIN TRANSACTION;

-- task 1: comment_x_media

CREATE TABLE comment_x_media (
  comment_id integer,
  media_id integer
);

CREATE UNIQUE INDEX idx_comment_media on comment_x_media (comment_id, media_id);
CREATE UNIQUE INDEX idx_media_comment on comment_x_media (media_id, comment_id);

-- task 2: add webdb_lastchange to table comment
  
  ALTER TABLE "comment" RENAME TO "comment_old";
  DROP INDEX comment_pkey;
  
  CREATE TABLE "comment" (
    "id" integer DEFAULT nextval('comment_id_seq') NOT NULL,
    "title" character varying(80) NOT NULL,
    "creator" character varying(80) NOT NULL,
    "description" text NOT NULL,
    "main_url" character varying(255),
    "email" character varying(80),
    "address" character varying(80),
    "phone" character varying(20),
    "webdb_create" timestamp with time zone NOT NULL,
    "webdb_lastchange" timestamp with time zone,
    "is_published" boolean DEFAULT '1' NOT NULL,
    "to_language" integer DEFAULT '0' NOT NULL,
    "to_media" integer NOT NULL,
    "to_comment_status" smallint,
    "checksum" integer,
    "is_html" boolean DEFAULT '0' NOT NULL,
    Constraint "comment_pkey" Primary Key ("id")
  );
  
  INSERT INTO "comment" (
    "id", 
    "title", 
    "creator",
    "description",
    "main_url",
    "email",
    "address",
    "phone",
    "webdb_create",
    "webdb_lastchange",
    "is_published",
    "to_language",
    "to_media",
    "to_comment_status",
    "checksum",
    "is_html"
  )
  SELECT
    "id", 
    "title", 
    "creator",
    "description",
    "main_url",
    "email",
    "address",
    "phone",
    "webdb_create",
    "webdb_create",
    "is_published",
    "to_language",
    "to_media",
    "to_comment_status",
    "checksum",
    "is_html" 
  FROM "comment_old";

  UPDATE  pg_class
  SET
    relowner = (SELECT relowner FROM pg_class WHERE relname='comment_old'),
    relacl =   (SELECT relacl FROM pg_class WHERE relname='comment_old')
  WHERE 
    relname = 'comment';
  
  DROP TABLE "comment_old";

  CREATE  INDEX "comment_checksum_index" on "comment" using btree ( "checksum" "int4_ops" );
  CREATE  INDEX "idx_comment_to_media" on "comment" using btree ( "to_media" "int4_ops" );
  create index idx_comment_webdb_create on comment(webdb_create);
  CREATE  INDEX "idx_comment_tomedia_ispublished" on "comment" using btree ( "to_media" "int4_ops", "is_published" "bool_ops" );
  CREATE UNIQUE INDEX "idx_comment_id" on "comment" using btree ( "id" "int4_ops" );
  
-- that's it!
  
COMMIT TRANSACTION;
