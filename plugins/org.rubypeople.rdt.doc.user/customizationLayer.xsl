<?xml version="1.0"?> 
<xsl:stylesheet  
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"> 


<!-- another option of using a variable href would be the usage of
     an URIResolver. But thats probably more effort than replacing
     the token @@DOCBOOKROOT@@ with ant -->

<xsl:import href="./docbook/docbook-xsl/eclipse/eclipse.xsl"/> 

<xsl:param name="htmlhelp.title" >ABC</xsl:param>

<xsl:template name="user.header.navigation">
  <center style="font-weight: bold;">Ruby Development Tools Documentation - @@VERSION@@</center>
</xsl:template>



</xsl:stylesheet>