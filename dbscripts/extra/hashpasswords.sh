#!/bin/bash

# DON'T RUN THIS SCRIPT UNLESS YOU KNOW WHAT YOU ARE DOING
#
# bash script to md5-hash all passwords: RUN ONLY ONCE!
# md5sum needs to be installed
# 
# this script is not enough to use md5-hashed passwords: an
# appropriate localizer needs to be set up as well.


usage () {
  echo "usage: $0 dbname"
}

if [ $# -ne 1 ]; then
  usage 
  exit;
fi;


# to get a backslash in the first pass sql script
s1="\\"
# to get a backslash in the second pass sql script
s2="\\\\"
# to get an escaped backslash in the second pass sql script:
s3="\\\\\\\\"

 
SCRIPT_FILE="/tmp/hashpasswords.$$.sql"

echo psql $1 -P format=unaligned -t -X -F "" -c \
  "select '${s2}set hashedpassword ${s1}'${s2}${s1}'${s1}' \`echo -n \"'||password||'\" | md5sum \`   \
            \"${TEXT_SOURCE_PATH}\$(basename \"' || trim($2) || '\")\"\` ${s1}'${s2}${s1}'\\'\n',  \
                'update webdb_users set password=:hashedpassword where id = '||id from webdb_users" > $SCRIPT_FILE


# execute the script
#psql -f ${SCRIPT_FILE} -d template1
cat SCRIPT_FILE

rm ${SCRIPT_FILE}
