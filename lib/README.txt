This folder contains all necessary java libraries to compile
MIR. Have a look at the /doc folder for java libraries that
have to be present on your system in order to make MIR run.

The libraries should be stored in this folder containing
their version in the filename. The filename should look like:

  name-of-library-x.y.z.jar

A description and url should be added here. Please keep track
of the documentation! If there are things to be done regarding
a certain library, this is stated in a todo field

Make sure to check in the libs with the binary flag. If you
forgot to do so you can fire up "cvs admin -kb [name_of_lib]" 

Now the descriptions in alphabetical order:


avalon-framework-4.0
------------------------------------------------
version    : 4.0
url        : http://avalon.apache.org/framework
description: The Avalon Framework consists of 
             interfaces that define relationships 
             between commonly used application 
             components, best-of-practice pattern 
             enforcements, and several lightweight 
             convenience implementations of the 
             generic components.
todo       : update to 4.1.4



batik
------------------------------------------------
version    : ??
url        : http://xml.apache.org/batik/
description: batik is for images in the Scalable Vector 
             Graphics (SVG) 
todo       : check if necessary / update to version 1.1.1
             MIR compiles without it. Is it (still) 
             necessary?


commons-beanutils
------------------------------------------------
version    : 1.6.1
url        : http://jakarta.apache.org/commons/
description: Commons-BeanUtils provides 
			 easy-to-use wrappers around the Java 
			 reflection and introspection APIs. 
     
     
commons-collections
------------------------------------------------
version    : 2.1
url        : http://jakarta.apache.org/commons/
description: needed for parsing the configuration 
             properties file and provides more useful collections
             like a LRUCache


commons-logging
------------------------------------------------
version    : 1.0.3
url        : http://jakarta.apache.org/commons/
description: Commons-Logging is a wrapper around 
             a variety of logging API 
             implementations. 


commons-net
------------------------------------------------
version    : 1.0
url        : http://jakarta.apache.org/commons/
description: needed for the mail feature


fop
------------------------------------------------
version    : ??
url        : http://xml.apache.org/fop/
description:
todo       : The latest stable version is FOP-0.20.5
             check if necessary


freemarker.jar / freemarker-utility.jar
------------------------------------------------
version    : 1.6.2 (probably)
url        : http://fm-classic.sourceforge.net
description: template engine
todo       : update to version 1.7.5 
             template changes involved on update


gnu-regexp
------------------------------------------------
version    : 1.1.4
url        : http://www.cacas.org/java/gnu/regexp/
description: GNU regular expression library
todo 	   : check if to be replaced with jakarta
             commons regexp


iText
------------------------------------------------
version    : ??
url        : http://www.lowagie.com/iText/
description: PDF
todo 	   : check if necessary


jimi
------------------------------------------------
version    : ??
url        : http://java.sun.com/products/jimi/
description: Jimi is a class library for managing 
             images
todo       : check for update / check if necessary


log4j
------------------------------------------------
version    : 1.2.8
url        : http://jakarta.apache.org/log4j/
description: Jakarta Log4J standard library for
             the logging layer of MIR.


logkit
------------------------------------------------
version    : 1.0
url        : http://apache.serveftp.org/apache-site/dist/avalon/logkit/latest/
description: LogKit is an easy to use logging 
             toolkit. 
depends on : avalon
todo       : update to version 1.2 ??


lucene
------------------------------------------------
version    : 1.2
url        : http://jakarta.apache.org/lucene/
description: Jakarta Lucene is a high-performance, 
             full-featured text search engine written 
             entirely in Java.


multex
------------------------------------------------
version    : 3
url        : http://www.tfh-berlin.de/~knabe/java/multex
description: nested exceptions
todo       : rename to multex-3.jar


poolman
------------------------------------------------
version    : ??
url        : https://sourceforge.net/projects/poolman
url        : http://www.codestudio.com/
description: PoolMan is no longer available or supported 
             Connection and object pooling mechanisms: 
             they can now be found in application servers [...] 
             Tomcat and the Jakarta Project, and other 
             J2EE products and servers.
todo       : find replacement


postgresql
------------------------------------------------
version    : 7.2 (jdbc2-version)
url        : http://jdbc.postgresql.org
description: JDBC driver for postgresql database


strutsmesg
------------------------------------------------
version    : ??
url        : 
description: Extracted from struts 1.0
todo       : update


