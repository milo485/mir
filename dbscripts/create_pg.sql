--
-- media_folder
--

CREATE SEQUENCE "media_folder_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;

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
-- media_type
--

CREATE SEQUENCE "media_type_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;

CREATE TABLE "media_type" (
	"id" integer DEFAULT nextval('media_type_id_seq'::text) NOT NULL,
	"name" character varying(80) NOT NULL,
	"mime_type" character varying(40) NOT NULL,
	"classname" character varying(80) NOT NULL,
	"tablename" character varying(80) NOT NULL,
	"dcname" character varying(20)
);

--
-- language
--

CREATE SEQUENCE "language_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1 cache 1 ;

CREATE TABLE "language" (
	"id" integer DEFAULT nextval('language_id_seq') NOT NULL,
  "name" character varying(40) NOT NULL,
  "code" character varying(2) NOT NULL,
  Constraint "language_pkey" Primary Key ("id")
);

--
-- comment_status
--

CREATE SEQUENCE "comment_status_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1 cache 1 ;

CREATE TABLE "comment_status" (
  "id" integer DEFAULT nextval('comment_status_id_seq') NOT NULL,
  "name" character varying(40) NOT NULL,
  CONSTRAINT "comment_status_pkey" PRIMARY KEY ("id")
);



--
-- rights
--

CREATE TABLE "rights" (
	"id" integer NOT NULL,
	"name" character varying(80) NOT NULL,
	"description" text,
	Constraint "rights_pkey" Primary Key ("id")
);

--
-- webdb_users
--

CREATE SEQUENCE "webdb_users_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;

CREATE TABLE "webdb_users" (
	"id" integer DEFAULT nextval('webdb_users_id_seq'::text) NOT NULL,
	"login" character varying(16) NOT NULL,
	"password" character varying(16) NOT NULL,
	"is_admin" boolean DEFAULT '0' NOT NULL,
	Constraint "webdb_users_pkey" Primary Key ("id")
);

CREATE  INDEX "idx_webdb_user_log_pas_is_admin" on "webdb_users" using btree ( "login" "varchar_ops", "password" "varchar_ops", "is_admin" "bool_ops" );

CREATE  INDEX "idx_webdb_user_log_pas" on "webdb_users" using btree ( "login" "varchar_ops", "password" "varchar_ops" );

--
-- article type
--

CREATE SEQUENCE "article_type_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1 cache 1 ;

CREATE TABLE "article_type" (
	"id" integer DEFAULT nextval('article_type_id_seq') NOT NULL,
	"name" character varying(40) NOT NULL,
	CONSTRAINT "article_type_pkey" PRIMARY KEY ("id")
);

--
-- topic
--

CREATE SEQUENCE "topic_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;

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

CREATE        INDEX "idx_topic_title" on "topic"           using btree ( "title" "varchar_ops" );
CREATE UNIQUE INDEX "idx_topic_id"    on "topic"           using btree ( "id" "int4_ops" );


-- 
-- content_x_topics
--

CREATE TABLE "content_x_topic" (
	"content_id" integer NOT NULL,
	"topic_id" integer NOT NULL
);

CREATE UNIQUE INDEX "idx_content"     on "content_x_topic" using btree ( "content_id" "int4_ops", "topic_id" "int4_ops" );
CREATE UNIQUE INDEX "idx_topic"       on "content_x_topic" using btree ( "topic_id" "int4_ops", "content_id" "int4_ops" );

--
-- comment
--

CREATE SEQUENCE "comment_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;
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
  "webdb_lastchange" timestamp with time zone,
  "is_published" boolean DEFAULT '1' NOT NULL,
  "to_language" integer DEFAULT '0' NOT NULL,
  "to_media" integer NOT NULL,
  "to_comment_status" smallint,
  "checksum" integer,
  "is_html" boolean DEFAULT '0' NOT NULL,
  Constraint "comment_pkey" Primary Key ("id")
);

CREATE        INDEX "comment_checksum_index" on "comment" using btree ( "checksum" "int4_ops" );
CREATE        INDEX "idx_comment_to_media" on "comment" using btree ( "to_media" "int4_ops" );
CREATE        INDEX idx_comment_webdb_create on comment(webdb_create);
CREATE        INDEX "idx_comment_tomedia_ispublished" on "comment" using btree ( "to_media" "int4_ops", "is_published" "bool_ops" );
CREATE UNIQUE INDEX "idx_comment_id" on "comment" using btree ( "id" "int4_ops" );



      CREATE TABLE "comment_x_media" (
         "comment_id" integer,
         "media_id" integer
      );
      
      CREATE UNIQUE INDEX idx_comment_media on comment_x_media (comment_id, media_id);
      CREATE UNIQUE INDEX idx_media_comment on comment_x_media (media_id, comment_id);


--
-- media
--

CREATE SEQUENCE "media_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;
CREATE TABLE "media" (
	"id" integer DEFAULT nextval('media_id_seq'::text) NOT NULL,
	"title" character varying(255),
	"subtitle" character varying(255),
	"edittitle" character varying(255),
	"date" character(8) NOT NULL,
	"creator" character varying(80),
	"creator_main_url" character varying(255),
	"creator_email" character varying(80),
	"creator_address" character varying(80),
	"creator_phone" character varying(20),
	"description" text,
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
-- uploaded_media
--

CREATE TABLE "uploaded_media" (
	"icon_is_produced" boolean DEFAULT '0' NOT NULL,
	"icon_path" character varying(255),
	"size" integer
)
INHERITS ("media");

CREATE UNIQUE INDEX "idx_uploaded_media_id" on "uploaded_media" using btree ( "id" "int4_ops" );
CREATE UNIQUE INDEX "idx_uploaded_media_is_published" on "uploaded_media" using btree ( "id" "int4_ops", "is_published" "bool_ops" );

--
-- images
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

CREATE        INDEX "idx_images_is_published__icon_i" on "images" using btree ( "is_published" "bool_ops", "icon_is_produced" "bool_ops" );
CREATE UNIQUE INDEX "idx_images_id" on "images" using btree ( "id" "int4_ops" );

        --
        -- img_format
        --
        
        CREATE TABLE "img_format" (
          "id" smallint NOT NULL,
          "name" character varying(20) NOT NULL,
          "extension" character varying(10) NOT NULL,
          "mimetype" character varying(40) NOT NULL,
          "commment" character varying(255)
        );
        
        --
        -- img_layout
        --
        
        CREATE TABLE "img_layout" (
          "id" smallint NOT NULL,
          "name" character varying(20) NOT NULL
        );
        
        --
        -- img_type
        --
        
        CREATE TABLE "img_type" (
          "id" smallint NOT NULL,
          "name" character varying(30) NOT NULL
        );
        
        --
        -- img_color
        --
        
        CREATE TABLE "img_color" (
          "id" smallint NOT NULL,
          "name" character varying(30) NOT NULL
        );

        
--
-- audio
--

CREATE TABLE "audio" (
  "kbits" smallint
)
INHERITS ("uploaded_media");

CREATE        INDEX "idx_audio_is_published_produced" on "audio" using btree ( "is_published" "bool_ops", "is_produced" "bool_ops" );
CREATE UNIQUE INDEX "idx_audio_id" on "audio" using btree ( "id" "int4_ops" );

--
-- video
--

CREATE TABLE "video" (
)
INHERITS ("uploaded_media");

CREATE        INDEX "idx_video_is_published_produced" on "video" using btree ( "is_published" "bool_ops", "is_produced" "bool_ops" );
CREATE UNIQUE INDEX "idx_video_id" on "video" using btree ( "id" "int4_ops" );

--
-- other_media
--

CREATE TABLE "other_media" (
)
INHERITS ("uploaded_media");

--
-- content
--

CREATE TABLE "content" (
	"content_data" text,
	"is_html" boolean DEFAULT '0' NOT NULL,
	"to_article_type" smallint DEFAULT '0' NOT NULL,
	"to_content" integer,
	"checksum" integer
)
INHERITS ("media");

CREATE        INDEX "content_checksum_index" on "content" using btree ( "checksum" "int4_ops" );
CREATE        INDEX "idx_content_to_article_type" on "content" using btree ( "to_article_type" "int2_ops" );
CREATE        INDEX "idx_content_is_produced" on "content" using btree ( "is_produced" "bool_ops" );
CREATE        INDEX "idx_content_is_published__to_ar" on "content" using btree ( "is_published" "bool_ops", "to_article_type" "int2_ops" );
CREATE        INDEX "idx_content_is_published__id" on "content" using btree ( "is_published" "bool_ops", "id" "int4_ops" );
CREATE        INDEX "idx_content_is_pub__to_art__to_" on "content" using btree ( "is_published" "bool_ops", "to_article_type" "int2_ops", "id" "int4_ops" );
CREATE UNIQUE INDEX "idx_content_id" on "content" using btree ( "id" "int4_ops" );
CREATE        INDEX "idx_content_is_published" on "content" using btree ( "is_published" "bool_ops" );
CREATE        INDEX idx_content_webdb_create on content(webdb_create);


    -- content_x_media

    CREATE TABLE "content_x_media" (
      "content_id" integer,
      "media_id" integer
    );

    CREATE UNIQUE INDEX "idx_content_media" on "content_x_media" using btree ( "content_id" "int4_ops", "media_id" "int4_ops" );
    CREATE UNIQUE INDEX "idx_media_content" on "content_x_media" using btree ( "media_id" "int4_ops", "content_id" "int4_ops" );

--
-- breaking
--

CREATE SEQUENCE "breaking_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;

CREATE TABLE "breaking" (
  "id" integer DEFAULT nextval('breaking_id_seq'::text) NOT NULL,
  "text" character varying(255) NOT NULL,
  "webdb_create" timestamp with time zone NOT NULL
);

--
-- messages
--

CREATE SEQUENCE "messages_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;

CREATE TABLE "messages" (
  "id" integer DEFAULT nextval('messages_id_seq'::text) NOT NULL,
  "title" character varying(30),
  "description" character varying(255) NOT NULL,
  "creator" character varying(30) NOT NULL,
  "webdb_create" timestamp with time zone NOT NULL
);

