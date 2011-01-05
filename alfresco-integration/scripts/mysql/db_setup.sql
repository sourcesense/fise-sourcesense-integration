create database iks default character set utf8 collate utf8_bin;
grant all on iks.* to 'iks'@'localhost' identified by 'iks' with grant option;
grant all on iks.* to 'iks'@'localhost.localdomain' identified by 'iks' with grant option;