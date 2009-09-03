# rsync creates files with the same permissions as the source file (considering umask)
chmod -R g+w htdocs
rsync -vu -e "ssh" htdocs/*.html  mbarchfe@shell.sf.net:/home/groups/r/ru/rubyeclipse/htdocs
rsync -vu -e "ssh" htdocs/*.php htdocs/.htaccess htdocs/*.css  mbarchfe@shell.sf.net:/home/groups/r/ru/rubyeclipse/htdocs
rsync -vu -e "ssh" htdocs/images/*.gif htdocs/images/*.png mbarchfe@shell.sf.net:/home/groups/r/ru/rubyeclipse/htdocs/images
rsync -vu -e "ssh" htdocs/images/screenshots/*.jpg mbarchfe@shell.sf.net:/home/groups/r/ru/rubyeclipse/htdocs/images/screenshots
