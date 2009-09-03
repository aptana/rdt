# rsync creates files with the same permissions as the source file (considering umask)
chmod -R g+w html
chmod -R g+w images
rsync -vu -e "ssh" html/*.html  mbarchfe@shell.sf.net:/home/groups/r/ru/rubyeclipse/htdocs/userdoc/html
rsync -vu -e "ssh" images/*.png mbarchfe@shell.sf.net:/home/groups/r/ru/rubyeclipse/htdocs/userdoc/images
