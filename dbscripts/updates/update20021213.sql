-- update script 2002-12-12 by Zapata
-- * introduces sequences and primary keys for comment_status, article_type and language
-- * adds is_html to comment
-- The first operation will fail the second time this script is run,
--   so running this script when it isn't needed can't do any harm.
--
-- IMPORTANT: after running this script, run the update_all_sequences script as well

BEGIN TRANSACTION;

-- task 1: add sequenced and unique id's to comment_status 

  CREATE SEQUENCE "comment_status_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1 cache 1;

  ALTER TABLE "comment_status" RENAME TO comment_status_old;

  CREATE TABLE "comment_status" (
  	"id" integer DEFAULT nextval('comment_status_id_seq') NOT NULL,
  	"name" character varying(40) NOT NULL,
  	CONSTRAINT "comment_status_pkey" PRIMARY KEY ("id")
  );

  INSERT INTO "comment_status" ("id", "name")
  SELECT "id", "name"
  FROM "comment_status_old";

  UPDATE pg_class
  SET
    relowner = (SELECT relowner FROM pg_class WHERE relname='comment_status_old'),
    relacl =   (SELECT relacl FROM pg_class WHERE relname='comment_status_old')
  WHERE 
    relname = 'comment_status' or relname='comment_status_id_seq';

  DROP TABLE "comment_status_old";


  
-- task 2: add sequenced and unique id's to article_type

  CREATE SEQUENCE "article_type_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1 cache 1 ;

  ALTER TABLE "article_type" RENAME TO "article_type_old";

  CREATE TABLE "article_type" (
  	"id" integer DEFAULT nextval('article_type_id_seq') NOT NULL,
  	"name" character varying(40) NOT NULL,
  	CONSTRAINT "article_type_pkey" PRIMARY KEY ("id")
  );

  INSERT INTO "article_type" ("id", "name")
  SELECT "id", "name"
  FROM "article_type_old";

  UPDATE pg_class
  SET
    relowner = (SELECT relowner FROM pg_class WHERE relname='article_type_old'),
    relacl =   (SELECT relacl FROM pg_class WHERE relname='article_type_old')
  WHERE 
    relname = 'article_type' or relname= 'article_type_id_seq';
  
  DROP TABLE "article_type_old";
  

-- task 3: add sequenced and unique id's to language

  CREATE SEQUENCE "language_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1 cache 1 ;

  ALTER TABLE "language" RENAME TO "language_old";
  DROP INDEX language_pkey;
  
  CREATE TABLE "language" (
    "id" integer DEFAULT nextval('language_id_seq') NOT NULL,
    "name" character varying(40) NOT NULL,
    "code" character varying(2) NOT NULL,
    Constraint "language_pkey" Primary Key ("id")
  );

  INSERT INTO "language" ("id", "name", "code")
  SELECT "id", "name", "code"
  FROM "language_old";

  UPDATE  pg_class
  SET
    relowner = (SELECT relowner FROM pg_class WHERE relname='language_old'),
    relacl =   (SELECT relacl FROM pg_class WHERE relname='language_old')
  WHERE 
    relname = 'language' or relname='language_id_seq';
  
  DROP TABLE "language_old";

  
-- task 4: add is_html to table comment
  
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
    "is_published",
    "to_language",
    "to_media",
    "to_comment_status",
    "checksum",
    '0'
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
