--
-- Selected TOC Entries:
--
--
-- TOC Entry ID 2 (OID 19796)
--
-- Name: media_id_seq Type: SEQUENCE Owner: postgres
--

CREATE SEQUENCE "media_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;

--
-- TOC Entry ID 3 (OID 19815)
--
-- Name: media_folder_id_seq Type: SEQUENCE Owner: postgres
--

CREATE SEQUENCE "media_folder_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;

--
-- TOC Entry ID 4 (OID 19834)
--
-- Name: feature_id_seq Type: SEQUENCE Owner: postgres
--

CREATE SEQUENCE "feature_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;

--
-- TOC Entry ID 5 (OID 19853)
--
-- Name: topic_id_seq Type: SEQUENCE Owner: postgres
--

CREATE SEQUENCE "topic_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;

--
-- TOC Entry ID 6 (OID 19872)
--
-- Name: webdb_users_id_seq Type: SEQUENCE Owner: postgres
--

CREATE SEQUENCE "webdb_users_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;

--
-- TOC Entry ID 7 (OID 19891)
--
-- Name: comment_id_seq Type: SEQUENCE Owner: postgres
--

CREATE SEQUENCE "comment_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;

--
-- TOC Entry ID 8 (OID 19910)
--
-- Name: breaking_id_seq Type: SEQUENCE Owner: postgres
--

CREATE SEQUENCE "breaking_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;

--
-- TOC Entry ID 9 (OID 19929)
--
-- Name: messages_id_seq Type: SEQUENCE Owner: postgres
--

CREATE SEQUENCE "messages_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;

--
-- TOC Entry ID 10 (OID 19948)
--
-- Name: media_type_id_seq Type: SEQUENCE Owner: postgres
--

CREATE SEQUENCE "media_type_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;

--
-- TOC Entry ID 12 (OID 19967)
--
-- Name: media_folder Type: TABLE Owner: postgres
--

CREATE TABLE "media_folder" (
	"id" integer DEFAULT nextval('media_folder_id_seq'::text) NOT NULL,
	"name" character varying(255) NOT NULL,
	"date" character(8) NOT NULL,
	"place" character varying(80),
	"keywords" text,
	"comment" text,
	"webdb_create" timestamp with time zone NOT NULL,
	"webdb_lastchange" timestamp with time zone
);

--
-- TOC Entry ID 13 (OID 20000)
--
-- Name: media_type Type: TABLE Owner: postgres
--

CREATE TABLE "media_type" (
	"id" integer DEFAULT nextval('media_type_id_seq'::text) NOT NULL,
	"name" character varying(80) NOT NULL,
	"mime_type" character varying(40) NOT NULL,
	"classname" character varying(80) NOT NULL,
	"tablename" character varying(80) NOT NULL,
	"dcname" character varying(20)
);

--
-- TOC Entry ID 14 (OID 20016)
--
-- Name: img_format Type: TABLE Owner: postgres
--

CREATE TABLE "img_format" (
	"id" smallint NOT NULL,
	"name" character varying(20) NOT NULL,
	"extension" character varying(10) NOT NULL,
	"mimetype" character varying(40) NOT NULL,
	"commment" character varying(255)
);

--
-- TOC Entry ID 15 (OID 20030)
--
-- Name: img_layout Type: TABLE Owner: postgres
--

CREATE TABLE "img_layout" (
	"id" smallint NOT NULL,
	"name" character varying(20) NOT NULL
);

--
-- TOC Entry ID 16 (OID 20041)
--
-- Name: img_type Type: TABLE Owner: postgres
--

CREATE TABLE "img_type" (
	"id" smallint NOT NULL,
	"name" character varying(30) NOT NULL
);

--
-- TOC Entry ID 17 (OID 20052)
--
-- Name: img_color Type: TABLE Owner: postgres
--

CREATE TABLE "img_color" (
	"id" smallint NOT NULL,
	"name" character varying(30) NOT NULL
);

--
-- TOC Entry ID 18 (OID 20063)
--
-- Name: language Type: TABLE Owner: postgres
--

CREATE TABLE "language" (
	"id" integer NOT NULL,
	"name" character varying(40) NOT NULL,
	"code" character varying(2) NOT NULL,
	Constraint "language_pkey" Primary Key ("id")
);

--
-- TOC Entry ID 19 (OID 20078)
--
-- Name: rights Type: TABLE Owner: postgres
--

CREATE TABLE "rights" (
	"id" integer NOT NULL,
	"name" character varying(80) NOT NULL,
	"description" text,
	Constraint "rights_pkey" Primary Key ("id")
);

--
-- TOC Entry ID 20 (OID 20108)
--
-- Name: feature Type: TABLE Owner: postgres
--

CREATE TABLE "feature" (
	"id" integer DEFAULT nextval('feature_id_seq'::text) NOT NULL,
	"title" character varying(80) NOT NULL,
	"description" text,
	"filename" character varying(20) NOT NULL,
	"main_url" character varying(255),
	"is_published" boolean DEFAULT '0' NOT NULL,
	Constraint "feature_pkey" Primary Key ("id")
);

--
-- TOC Entry ID 21 (OID 20143)
--
-- Name: webdb_users Type: TABLE Owner: postgres
--

CREATE TABLE "webdb_users" (
	"id" integer DEFAULT nextval('webdb_users_id_seq'::text) NOT NULL,
	"login" character varying(16) NOT NULL,
	"password" character varying(16) NOT NULL,
	"is_admin" boolean DEFAULT '0' NOT NULL,
	Constraint "webdb_users_pkey" Primary Key ("id")
);

--
-- TOC Entry ID 22 (OID 20161)
--
-- Name: content_x_topic Type: TABLE Owner: postgres
--

CREATE TABLE "content_x_topic" (
	"content_id" integer NOT NULL,
	"topic_id" integer NOT NULL
);

--
-- TOC Entry ID 23 (OID 20172)
--
-- Name: article_type Type: TABLE Owner: postgres
--

CREATE TABLE "article_type" (
	"id" integer NOT NULL,
	"name" character varying(20) NOT NULL
);

--
-- TOC Entry ID 24 (OID 20183)
--
-- Name: topic Type: TABLE Owner: postgres
--

CREATE TABLE "topic" (
	"id" integer DEFAULT nextval('topic_id_seq'::text) NOT NULL,
	"parent_id" integer DEFAULT '0' NOT NULL,
	"title" character varying(80) NOT NULL,
	"description" text,
	"filename" character varying(20) NOT NULL,
	"main_url" character varying(255),
	"archiv_url" character varying(255),
	Constraint "topic_pkey" Primary Key ("id")
);

--
-- TOC Entry ID 25 (OID 20219)
--
-- Name: comment Type: TABLE Owner: postgres
--

CREATE TABLE "comment" (
	"id" integer DEFAULT nextval('comment_id_seq'::text) NOT NULL,
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
	Constraint "comment_pkey" Primary Key ("id")
);

--
-- TOC Entry ID 26 (OID 20266)
--
-- Name: media Type: TABLE Owner: postgres
--

CREATE TABLE "media" (
	"id" integer DEFAULT nextval('media_id_seq'::text) NOT NULL,
	"title" character varying(80) NOT NULL,
	"subtitle" character varying(30),
	"edittitle" character varying(30),
	"date" character(8) NOT NULL,
	"creator" character varying(80),
	"creator_main_url" character varying(255),
	"creator_email" character varying(80),
	"creator_address" character varying(80),
	"creator_phone" character varying(20),
	"description" text,
	"keywords" text,
	"comment" text,
	"source" character varying(255),
	"publish_date" timestamp with time zone,
	"publish_server" character varying(255),
	"publish_path" character varying(255),
	"is_published" boolean DEFAULT '0' NOT NULL,
	"is_produced" boolean DEFAULT '0' NOT NULL,
	"to_feature" integer DEFAULT '0' NOT NULL,
	"to_media_folder" integer DEFAULT '0' NOT NULL,
	"to_media_type" smallint DEFAULT '0' NOT NULL,
	"to_publisher" integer NOT NULL,
	"to_language" integer DEFAULT '0',
	"to_rights" integer DEFAULT '0',
	"webdb_create" timestamp with time zone NOT NULL,
	"webdb_lastchange" timestamp with time zone,
	"to_media" integer
);

--
-- TOC Entry ID 27 (OID 20326)
--
-- Name: uploaded_media Type: TABLE Owner: postgres
--

CREATE TABLE "uploaded_media" (
	"icon_is_produced" boolean DEFAULT '0' NOT NULL,
	"icon_path" character varying(255),
	"size" integer
)
INHERITS ("media");

--
-- TOC Entry ID 28 (OID 20392)
--
-- Name: images Type: TABLE Owner: postgres
--

CREATE TABLE "images" (
	"image_data" oid,
	"icon_data" oid,
	"year" character varying(40),
	"img_width" smallint,
	"img_height" smallint,
	"to_img_format" smallint DEFAULT '0' NOT NULL,
	"to_img_layout" smallint DEFAULT '0' NOT NULL,
	"to_img_type" smallint DEFAULT '0' NOT NULL,
	"to_img_color" smallint DEFAULT '0' NOT NULL,
	"icon_width" smallint,
	"icon_height" smallint
)
INHERITS ("uploaded_media");

--
-- TOC Entry ID 29 (OID 20474)
--
-- Name: content Type: TABLE Owner: postgres
--

CREATE TABLE "content" (
	"content_data" text,
	"link_url" character varying(255),
	"is_html" boolean DEFAULT '0' NOT NULL,
	"is_stored" boolean DEFAULT '0' NOT NULL,
	"to_article_type" smallint DEFAULT '0' NOT NULL,
	"to_content" integer,
	"checksum" integer
)
INHERITS ("media");

--
-- TOC Entry ID 30 (OID 20549)
--
-- Name: breaking Type: TABLE Owner: postgres
--

CREATE TABLE "breaking" (
	"id" integer DEFAULT nextval('breaking_id_seq'::text) NOT NULL,
	"text" character varying(255) NOT NULL,
	"webdb_create" timestamp with time zone NOT NULL
);

--
-- TOC Entry ID 31 (OID 20562)
--
-- Name: messages Type: TABLE Owner: postgres
--

CREATE TABLE "messages" (
	"id" integer DEFAULT nextval('messages_id_seq'::text) NOT NULL,
	"title" character varying(30),
	"description" character varying(255) NOT NULL,
	"creator" character varying(30) NOT NULL,
	"webdb_create" timestamp with time zone NOT NULL
);

--
-- TOC Entry ID 32 (OID 20577)
--
-- Name: comment_status Type: TABLE Owner: postgres
--

CREATE TABLE "comment_status" (
	"id" smallint NOT NULL,
	"name" character varying(40) NOT NULL
);

--
-- TOC Entry ID 33 (OID 20588)
--
-- Name: content_x_media Type: TABLE Owner: postgres
--

CREATE TABLE "content_x_media" (
	"content_id" integer,
	"media_id" integer
);

--
-- TOC Entry ID 11 (OID 20599)
--
-- Name: links_imcs_id_seq Type: SEQUENCE Owner: postgres
--

CREATE SEQUENCE "links_imcs_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;

--
-- TOC Entry ID 34 (OID 20618)
--
-- Name: links_imcs Type: TABLE Owner: postgres
--

CREATE TABLE "links_imcs" (
	"id" integer DEFAULT nextval('links_imcs_id_seq'::text) NOT NULL,
	"to_parent_id" integer,
	"title" character varying(80) NOT NULL,
	"url" character varying(255) NOT NULL,
	"sortpriority" integer DEFAULT '1',
	"to_language" integer DEFAULT '0' NOT NULL,
	Constraint "links_imcs_pkey" Primary Key ("id")
);

--
-- TOC Entry ID 35 (OID 37215)
--
-- Name: audio Type: TABLE Owner: postgres
--

CREATE TABLE "audio" (
	"kbits" smallint
)
INHERITS ("uploaded_media");

--
-- TOC Entry ID 36 (OID 37284)
--
-- Name: video Type: TABLE Owner: postgres
--

CREATE TABLE "video" (
)
INHERITS ("uploaded_media");

--
-- TOC Entry ID 37 (OID 45396)
--
-- Name: other_media Type: TABLE Owner: postgres
--

CREATE TABLE "other_media" (
	
)
INHERITS ("uploaded_media");

--
-- TOC Entry ID 40 (OID 20143)
--
-- Name: "idx_webdb_user_log_pas_is_admin" Type: INDEX Owner: postgres
--

CREATE  INDEX "idx_webdb_user_log_pas_is_admin" on "webdb_users" using btree ( "login" "varchar_ops", "password" "varchar_ops", "is_admin" "bool_ops" );

--
-- TOC Entry ID 41 (OID 20143)
--
-- Name: "idx_webdb_user_log_pas" Type: INDEX Owner: postgres
--

CREATE  INDEX "idx_webdb_user_log_pas" on "webdb_users" using btree ( "login" "varchar_ops", "password" "varchar_ops" );

--
-- TOC Entry ID 42 (OID 20161)
--
-- Name: "idx_content" Type: INDEX Owner: postgres
--

CREATE UNIQUE INDEX "idx_content" on "content_x_topic" using btree ( "content_id" "int4_ops", "topic_id" "int4_ops" );

--
-- TOC Entry ID 43 (OID 20161)
--
-- Name: "idx_topic" Type: INDEX Owner: postgres
--

CREATE UNIQUE INDEX "idx_topic" on "content_x_topic" using btree ( "topic_id" "int4_ops", "content_id" "int4_ops" );

--
-- TOC Entry ID 44 (OID 20183)
--
-- Name: "idx_topic_title" Type: INDEX Owner: postgres
--

CREATE  INDEX "idx_topic_title" on "topic" using btree ( "title" "varchar_ops" );

--
-- TOC Entry ID 45 (OID 20183)
--
-- Name: "idx_topic_id" Type: INDEX Owner: postgres
--

CREATE UNIQUE INDEX "idx_topic_id" on "topic" using btree ( "id" "int4_ops" );

--
-- TOC Entry ID 38 (OID 20219)
--
-- Name: "comment_checksum_index" Type: INDEX Owner: postgres
--

CREATE  INDEX "comment_checksum_index" on "comment" using btree ( "checksum" "int4_ops" );

--
-- TOC Entry ID 46 (OID 20219)
--
-- Name: "idx_comment_to_media" Type: INDEX Owner: postgres
--

CREATE  INDEX "idx_comment_to_media" on "comment" using btree ( "to_media" "int4_ops" );


create index idx_comment_webdb_create on comment(webdb_create);



--

--
-- TOC Entry ID 59 (OID 20326)
--
-- Name: "idx_uploaded_media_id" Type: INDEX Owner: postgres
--

CREATE UNIQUE INDEX "idx_uploaded_media_id" on "uploaded_media" using btree ( "id" "int4_ops" );

--
-- TOC Entry ID 60 (OID 20326)
--
-- Name: "idx_uploaded_media_is_published" Type: INDEX Owner: postgres
--

CREATE UNIQUE INDEX "idx_uploaded_media_is_published" on "uploaded_media" using btree ( "id" "int4_ops", "is_published" "bool_ops" );

--
-- TOC Entry ID 47 (OID 20392)
--
-- Name: "idx_images_is_published__icon_i" Type: INDEX Owner: postgres
--

CREATE  INDEX "idx_images_is_published__icon_i" on "images" using btree ( "is_published" "bool_ops", "icon_is_produced" "bool_ops" );

--
-- TOC Entry ID 48 (OID 20392)
--
-- Name: "idx_images_id" Type: INDEX Owner: postgres
--

CREATE UNIQUE INDEX "idx_images_id" on "images" using btree ( "id" "int4_ops" );

--
-- TOC Entry ID 39 (OID 20474)
--
-- Name: "content_checksum_index" Type: INDEX Owner: postgres
--

CREATE  INDEX "content_checksum_index" on "content" using btree ( "checksum" "int4_ops" );

--
-- TOC Entry ID 49 (OID 20474)
--
-- Name: "idx_content_to_article_type" Type: INDEX Owner: postgres
--

CREATE  INDEX "idx_content_to_article_type" on "content" using btree ( "to_article_type" "int2_ops" );

--
-- TOC Entry ID 50 (OID 20474)
--
-- Name: "idx_content_is_produced" Type: INDEX Owner: postgres
--

CREATE  INDEX "idx_content_is_produced" on "content" using btree ( "is_produced" "bool_ops" );

--
-- TOC Entry ID 51 (OID 20474)
--
-- Name: "idx_content_is_published__to_ar" Type: INDEX Owner: postgres
--

CREATE  INDEX "idx_content_is_published__to_ar" on "content" using btree ( "is_published" "bool_ops", "to_article_type" "int2_ops" );

--
-- TOC Entry ID 52 (OID 20474)
--
-- Name: "idx_content_is_stored" Type: INDEX Owner: postgres
--

CREATE  INDEX "idx_content_is_stored" on "content" using btree ( "is_stored" "bool_ops" );

--
-- TOC Entry ID 53 (OID 20474)
--
-- Name: "idx_content_is_published__id" Type: INDEX Owner: postgres
--

CREATE  INDEX "idx_content_is_published__id" on "content" using btree ( "is_published" "bool_ops", "id" "int4_ops" );

--
-- TOC Entry ID 54 (OID 20474)
--
-- Name: "idx_content_is_pub__to_art__to_" Type: INDEX Owner: postgres
--

CREATE  INDEX "idx_content_is_pub__to_art__to_" on "content" using btree ( "is_published" "bool_ops", "to_article_type" "int2_ops", "id" "int4_ops" );

--
-- TOC Entry ID 55 (OID 20474)
--
-- Name: "idx_content_id" Type: INDEX Owner: postgres
--

CREATE UNIQUE INDEX "idx_content_id" on "content" using btree ( "id" "int4_ops" );

--
-- TOC Entry ID 56 (OID 20588)
--
-- Name: "idx_content_media" Type: INDEX Owner: postgres
--

CREATE UNIQUE INDEX "idx_content_media" on "content_x_media" using btree ( "content_id" "int4_ops", "media_id" "int4_ops" );

--
-- TOC Entry ID 57 (OID 20588)
--
-- Name: "idx_media_content" Type: INDEX Owner: postgres
--

CREATE UNIQUE INDEX "idx_media_content" on "content_x_media" using btree ( "media_id" "int4_ops", "content_id" "int4_ops" );

--
-- TOC Entry ID 62 (OID 37215)
--
-- Name: "idx_audio_is_published_produced" Type: INDEX Owner: postgres
--

CREATE  INDEX "idx_audio_is_published_produced" on "audio" using btree ( "is_published" "bool_ops", "is_produced" "bool_ops" );

--
-- TOC Entry ID 64 (OID 37215)
--
-- Name: "idx_audio_id" Type: INDEX Owner: postgres
--

CREATE UNIQUE INDEX "idx_audio_id" on "audio" using btree ( "id" "int4_ops" );

--
-- TOC Entry ID 65 (OID 37215)
--
-- Name: "idx_video_id" Type: INDEX Owner: postgres
--

CREATE UNIQUE INDEX "idx_video_id" on "audio" using btree ( "id" "int4_ops" );

--
-- TOC Entry ID 63 (OID 37284)
--
-- Name: "idx_video_is_published_produced" Type: INDEX Owner: postgres
--

CREATE  INDEX "idx_video_is_published_produced" on "video" using btree ( "is_published" "bool_ops", "is_produced" "bool_ops" );


--
-- TOC Entry ID 37 (OID 520246)
--
-- Name: "idx_content_is_published" Type: INDEX Owner: postgres
--

CREATE  INDEX "idx_content_is_published" on "content" using btree ( "is_published" "bool_ops" );

--
-- TOC Entry ID 47 (OID 465036)
--
-- Name: "idx_comment_tomedia_ispublished" Type: INDEX Owner: postgres
--

CREATE  INDEX "idx_comment_tomedia_ispublished" on "comment" using btree ( "to_media" "int4_ops", "is_published" "bool_ops" );

CREATE UNIQUE INDEX "idx_comment_id" on "comment" using btree ( "id" "int4_ops" );

