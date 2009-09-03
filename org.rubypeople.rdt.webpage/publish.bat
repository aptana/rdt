REM pscp is part of putty
REM use putty to create a session "shell.sf.net"
pscp -load shell.sf.net htdocs/*.php htdocs/.htaccess htdocs/*.css shell.sf.net:/home/groups/r/ru/rubyeclipse/htdocs
pscp -load shell.sf.net htdocs/images/*.gif htdocs/images/*.png shell.sf.net:/home/groups/r/ru/rubyeclipse/htdocs/images
pscp -load shell.sf.net htdocs/images/screenshots/*.jpg shell.sf.net:/home/groups/r/ru/rubyeclipse/htdocs/images/screenshots
