<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
                      xmlns:fo="http://www.w3.org/1999/XSL/Format"
version="1.0" >

<!-- This stylesheet replaces the <BR /> tags that Mir 
uses to break paragraphs with empty blocks, 
which do the same thing in XSL:FO -->

<xsl:template match="br">
  <fo:block />  
</xsl:template>

<xsl:template match="@*|*|processing-instruction()|comment()">
  <xsl:copy>
    <xsl:apply-templates select="*|@*|text()|processing-instruction()|comment()" />
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>