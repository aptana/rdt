Webpage HOWTO:

1) Every RDT developer can change and publish the web page to
   /home/groups/r/ru/rubyeclipse/htdocs/
2) Whenever you publish a web page, make sure that the page is also checked in to CVS
3) For publishing you can use the publish scripts:
3.a) publish.sh 
Requires rsync and changing the sourceforge user name before using (an environment variable
should be used instead)
3.b) publish.bat
Requires putty and pscp. Before using the bat file you must start putty and save a session 
named "shell.sf.net". The session should connect to shell.sf.net with your user name. You 
can also add your private key for login without a password
4) If you do not use the scripts for publishing the pages check the rights on files and 
directories: they must be writable for group members. 
5) If you create a new directory, don't forget to set the s-bit, too. New files in this 
directory will then get the same rights as set on the directory.
The image directory and index.php file as example:
drwxrwsr-x   3 mbarchfe    rubyeclipse    4096 Dec 21  2004 images
-rw-rw-r--   1 mbarchfe    rubyeclipse    1733 Dec 21  2004 index.php
