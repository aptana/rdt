<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<?php

if ($page == "contact") {
  $title = "Contact" ;
}
else if ($page == "download") {
  $title = "Download" ;
}
else if ($page == "faq") {
  $title = "Frequently Asked Questions" ;
}
else if ($page == "features") {
  $title = "Features" ;
}
else if ($page == "screenshots") {
  $title = "Screenshots" ;
}
else {
  $page = "welcome" ;
  $title = "Welcome" ;
}



?>
	
<head>
  <link rel="icon" href="images/favicon.ico">
  <link rel="stylesheet" type="text/css" href="style.css">
  <title>RDT - Ruby Development Tools: <?= $title?></title>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>
	
  <body>   	
   <div id="container">
	 <!-- the left part is positioned absolutely. It occures first, because IE
		does not correctly handle mixed declarations of divs with relative and
		absolute positioning -->
     <div id="leftPart">
	
         <?php include 'menu.php';?>
     </div>		
     <div id="banner">
		 <table border="0" cellspacing="0" cellpadding="0" width="100%" bgcolor="#CC0000">
		    <tr>
		      <td width="126"><a href="http://rubyeclipse.sourceforge.net" target="_top"><img alt="RDT" border="0" src="images/menu_rdt.png"></a></td>
		      <td width="50"><img width="282" border="0" src="images/gradient.png" alt=""></td>
		      <td align="right" style="padding-right: 5px;">
		        <big><b><font face="Helvetica, Arial, sans-serif" color="#ffffff">
		        Ruby Development Tools
		        </font></b></big>
		      </td>
		    </tr>           
		  </table>		
	 </div>   
     <div id="mainPart">
		  <div id="mainPartHeader">
			<?= $title?>
		  </div>
		<?php  include "$page.php"; ?>
     </div>
    </div>
  </body>
</html>
