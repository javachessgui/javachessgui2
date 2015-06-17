copy gitconfig_javachessgui2.txt .gitconfig
cd documents
cd netbeansprojects
cd javachessgui2
del config.ser
del engine_list.txt
del /AH *.db
del dist\*.ser
del dist\*.txt
del /AH dist\*.db
copy board_setup.ser dist
git init
git add -A .
git commit -m %1
git remote set-url origin https://github.com/javachessgui/javachessgui2
git push origin master