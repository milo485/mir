#!/bin/bash

# bash script to change the owner of a database
#
# 

usage () {
  echo "usage: $0 dbname newowner"
}

if [ $# -ne 2 ]; then
  usage 
  exit;
fi;
  
SCRIPT_FILE="/tmp/createmir.$$.sql"

echo "UPDATE pg_database SET datdba=(select usesysid from pg_user where usename='$2') where datname='$1';" > ${SCRIPT_FILE}

echo "\connect $1" >> ${SCRIPT_FILE}

# grant rights
echo "grant all on media_type      to $2;"   >> ${SCRIPT_FILE}
echo "grant all on img_format      to $2;"   >> ${SCRIPT_FILE}
echo "grant all on img_layout      to $2;"   >> ${SCRIPT_FILE}
echo "grant all on img_type        to $2;"   >> ${SCRIPT_FILE}
echo "grant all on img_color       to $2;"   >> ${SCRIPT_FILE}
echo "grant all on language        to $2;"   >> ${SCRIPT_FILE}
echo "grant all on rights          to $2;"   >> ${SCRIPT_FILE}
echo "grant all on feature         to $2;"   >> ${SCRIPT_FILE}
echo "grant all on article_type    to $2;"   >> ${SCRIPT_FILE}
echo "grant all on media           to $2;"   >> ${SCRIPT_FILE}
echo "grant all on breaking        to $2;"   >> ${SCRIPT_FILE}
echo "grant all on messages        to $2;"   >> ${SCRIPT_FILE}
echo "grant all on comment_status  to $2;"   >> ${SCRIPT_FILE}
echo "grant all on links_imcs      to $2;"   >> ${SCRIPT_FILE}
echo "grant all on other_media     to $2;"   >> ${SCRIPT_FILE}
echo "grant all on webdb_users     to $2;"   >> ${SCRIPT_FILE}
echo "grant all on content_x_topic to $2;"   >> ${SCRIPT_FILE}
echo "grant all on topic           to $2;"   >> ${SCRIPT_FILE}
echo "grant all on uploaded_media  to $2;"   >> ${SCRIPT_FILE}
echo "grant all on images          to $2;"   >> ${SCRIPT_FILE}
echo "grant all on content_x_media to $2;"   >> ${SCRIPT_FILE}
echo "grant all on comment_x_media to $2;"   >> ${SCRIPT_FILE}
echo "grant all on audio           to $2;"   >> ${SCRIPT_FILE}
echo "grant all on video           to $2;"   >> ${SCRIPT_FILE}
echo "grant all on content         to $2;"   >> ${SCRIPT_FILE}
echo "grant all on comment         to $2;"   >> ${SCRIPT_FILE}
echo "grant all on media_folder    to $2;"   >> ${SCRIPT_FILE}
			  

echo "grant all on media_id_seq    to $2;"   >> ${SCRIPT_FILE}
echo "grant all on media_folder_id_seq to $2;"   >> ${SCRIPT_FILE}
echo "grant all on feature_id_seq  to $2;"   >> ${SCRIPT_FILE}
echo "grant all on topic_id_seq    to $2;"   >> ${SCRIPT_FILE}
echo "grant all on webdb_users_id_seq to $2;"   >> ${SCRIPT_FILE}
echo "grant all on comment_id_seq  to $2;"   >> ${SCRIPT_FILE}
echo "grant all on breaking_id_seq to $2;"   >> ${SCRIPT_FILE}
echo "grant all on messages_id_seq to $2;"   >> ${SCRIPT_FILE}
echo "grant all on media_type_id_seq to $2;"   >> ${SCRIPT_FILE}
echo "grant all on links_imcs_id_seq to $2;"   >> ${SCRIPT_FILE}
echo "grant all on comment_status_id_seq to $2;"   >> ${SCRIPT_FILE}
echo "grant all on article_type_id_seq to $2;"   >> ${SCRIPT_FILE}
echo "grant all on language_id_seq to $2;"   >> ${SCRIPT_FILE}


# execute the script
psql -f ${SCRIPT_FILE} -d template1

rm ${SCRIPT_FILE}
