#!/bin/sh
# startxml.sh -- indy.offline.xmlparser

# Identify the custom class path components we need
JAVA_HOME=/usr/lib/j2sdk1.3
APP_HOME=/var/work/admin/Indy
XML_HOME=/home/transfer/offline/
LIB_HOME=${APP_HOME}/WEB-INF/lib
CLASSPATH=${APP_HOME}/WEB-INF/classes
CP=$LIB_HOME/gnu-regexp-1.0.8.jar
CP=$CP:$LIB_HOME/freemarker.jar
CP=$CP:$LIB_HOME/mm.mysql
CP=$CP:$LIB_HOME/dbconbroker.jar
CP=$CP:$LIB_HOME/saxon.jar
CP=$CP:$LIB_HOME/postgresql-jdbc.jar

# Execute
/usr/lib/j2sdk1.3/bin/java -classpath $CP:$CLASSPATH \
	org.indy.input.XmlInputParser $XML_HOME > /dev/null 2>&1
