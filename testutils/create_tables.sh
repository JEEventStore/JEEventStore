#!/bin/bash

mysql -e 'CREATE DATABASE jeeventstore_test;'
(echo 'USE jeeventstore_test;'; cat ./persistence-jpa/src/main/sql/mysql.sql) | mysql
mysql -e 'USE jeeventstore_test; TRUNCATE TABLE jeeventstore_test.event_store;'

psql -U postgres -c 'CREATE DATABASE jeeventstore_test;'
(echo '\c jeeventstore_test'; \
	cat ./persistence-jpa/src/main/sql/postgres.sql; \
	echo 'ALTER SEQUENCE event_store_id_seq RESTART;'; \
	echo 'TRUNCATE TABLE event_store;' \
	) | psql -U postgres
