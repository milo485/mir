\connect - postgres

CREATE SEQUENCE "media_type_id_seq" start 1 increment 1 maxvalue 2147483647 minvalue 1  cache 1 ;

DROP TABLE "media_type";

CREATE TABLE "media_type" (
 	"id" integer DEFAULT nextval('media_type_id_seq'::text) NOT NULL,
	"name" character varying(80) NOT NULL,
	"mime_type" character varying(40) NOT NULL,
	"classname" character varying(80) NOT NULL,
	"tablename" character varying(80) NOT NULL,
	"dcname" character varying(20)
);


CREATE TABLE "uploaded_media" (
 
) INHERITS ("media");

