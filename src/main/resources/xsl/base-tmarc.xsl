<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:tmarc="http://www.indexdata.com/turbomarc"
  xmlns:pz="http://www.indexdata.com/pazpar2/1.0">

  <xsl:output indent="yes" method="xml" version="1.0" encoding="UTF-8" />

  <xsl:template match="tmarc:collection">
    <collection>
      <xsl:apply-templates />
    </collection>
  </xsl:template>
  
  <xsl:template match="tmarc:r">
  
    <pz:record>
      <!-- rules inserted here -->
    </pz:record>

  </xsl:template>
  
  <xsl:template match="text()" />

</xsl:stylesheet>
