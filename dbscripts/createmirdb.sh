#!/bin/bash


# experimental bash script to create a database
#
# 

usage () {
  echo "usage: $0 dbname superuser username password"
}

if [ $# -ne 4 ]; then
  usage 
  exit;
fi;
  
SCRIPT_FILE="/tmp/createmir.$$.sql"

# database/user creation
echo "CREATE DATABASE $1 WITH ENCODING='UNICODE';" > ${SCRIPT_FILE}
echo "CREATE USER "$3" WITH PASSWORD '$4';" >> ${SCRIPT_FILE}
echo "UPDATE pg_database SET datdba=(select usesysid from pg_user where usename='$3') where datname='$1';" >> ${SCRIPT_FILE}

echo "\connect $1" >> ${SCRIPT_FILE}

# create/populate scripts:
echo "\i create_pg.sql" >> ${SCRIPT_FILE}
for i in help*.sql ; do echo "\i ${i}" >> ${SCRIPT_FILE} ; done
for i in populate*.sql ; do echo "\i ${i}" >> ${SCRIPT_FILE} ; done
echo "\i update_all_sequences.sql" >> ${SCRIPT_FILE}

# grant rights
echo "grant all on media_type      to $3;"   >> ${SCRIPT_FILE}
echo "grant all on img_format      to $3;"   >> ${SCRIPT_FILE}
echo "grant all on img_layout      to $3;"   >> ${SCRIPT_FILE}
echo "grant all on img_type        to $3;"   >> ${SCRIPT_FILE}
echo "grant all on img_color       to $3;"   >> ${SCRIPT_FILE}
echo "grant all on language        to $3;"   >> ${SCRIPT_FILE}
echo "grant all on rights          to $3;"   >> ${SCRIPT_FILE}
echo "grant all on feature         to $3;"   >> ${SCRIPT_FILE}
echo "grant all on article_type    to $3;"   >> ${SCRIPT_FILE}
echo "grant all on media           to $3;"   >> ${SCRIPT_FILE}
echo "grant all on breaking        to $3;"   >> ${SCRIPT_FILE}
echo "grant all on messages        to $3;"   >> ${SCRIPT_FILE}
echo "grant all on comment_status  to $3;"   >> ${SCRIPT_FILE}
echo "grant all on links_imcs      to $3;"   >> ${SCRIPT_FILE}
echo "grant all on other_media     to $3;"   >> ${SCRIPT_FILE}
echo "grant all on webdb_users     to $3;"   >> ${SCRIPT_FILE}
echo "grant all on content_x_topic to $3;"   >> ${SCRIPT_FILE}
echo "grant all on topic           to $3;"   >> ${SCRIPT_FILE}
echo "grant all on uploaded_media  to $3;"   >> ${SCRIPT_FILE}
echo "grant all on images          to $3;"   >> ${SCRIPT_FILE}
echo "grant all on content_x_media to $3;"   >> ${SCRIPT_FILE}
echo "grant all on audio           to $3;"   >> ${SCRIPT_FILE}
echo "grant all on video           to $3;"   >> ${SCRIPT_FILE}
echo "grant all on content         to $3;"   >> ${SCRIPT_FILE}
echo "grant all on comment         to $3;"   >> ${SCRIPT_FILE}
echo "grant all on media_folder    to $3;"   >> ${SCRIPT_FILE}
			  

echo "grant all on media_id_seq    to $3;"   >> ${SCRIPT_FILE}
echo "grant all on media_folder_id_seq to $3;"   >> ${SCRIPT_FILE}
echo "grant all on feature_id_seq  to $3;"   >> ${SCRIPT_FILE}
echo "grant all on topic_id_seq    to $3;"   >> ${SCRIPT_FILE}
echo "grant all on webdb_users_id_seq to $3;"   >> ${SCRIPT_FILE}
echo "grant all on comment_id_seq  to $3;"   >> ${SCRIPT_FILE}
echo "grant all on breaking_id_seq to $3;"   >> ${SCRIPT_FILE}
echo "grant all on messages_id_seq to $3;"   >> ${SCRIPT_FILE}
echo "grant all on media_type_id_seq to $3;"   >> ${SCRIPT_FILE}
echo "grant all on links_imcs_id_seq to $3;"   >> ${SCRIPT_FILE}
echo "grant all on comment_status_id_seq to $3;"   >> ${SCRIPT_FILE}
echo "grant all on article_type_id_seq to $3;"   >> ${SCRIPT_FILE}
echo "grant all on language_id_seq to $3;"   >> ${SCRIPT_FILE}


# execute the script
psql -U $2 -f ${SCRIPT_FILE} -d template1

rm ${SCRIPT_FILE}
