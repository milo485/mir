<?xml version="1.0" encoding="ISO-8859-1" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output
                  method="xml"
                  encoding="iso-8859-1"
                  omit-xml-declaration="yes"
                  doctype-public="-//WAPFORUM//DTD WML 1.1//EN"
                  doctype-system="http://www.wapforum.org/DTD/wml_1.1.xml"
                  media-type="text/vnd.wap.wml"/>
<xsl:template match="/">

<wml>
<!--
<xsl:text>
<!DOCTYPE wml PUBLIC "-//WAPFORUM//DTD WML 1.1//EN" "http://www.wapforum.org/DTD/wml_1.1.xml">
</xsl:text>
-->

	<card title="indymedia" id="card1">
	        <p align="center">
	        <strong>CASTOR STOPPEN!</strong>
	        </p>
	        <p align="center">
	        de.indymedia.org
	        </p>
	        <p align="center">
	        <a href="#card2" title="news">NEWS</a>
	        </p>
	</card>


	<card id="card2" title="news">
		        <p align="center">
		        <strong>Breaking News</strong>
		        </p>
		<xsl:for-each select="//breakingitem">
		        <p align="center">
			<xsl:value-of select="date"/>
		        </p>
		        <p aligen="left">
			<xsl:value-of select="content"/>
		        </p>
		</xsl:for-each>
	</card>
</wml>
</xsl:template>
</xsl:stylesheet>

