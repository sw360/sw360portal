#! /bin/bash

sleep 20s
echo Creating admin admin user in couchdb
curl -H "Content-Type: application/json" -X POST -d '{"action":"enable_single_node","password":"admin","port":"5984","singlenode":"true","username":"admin"}' http://couchdb:5984/_cluster_setup
echo Starting Tomcat.
# Launch tomcat of Liferay
${CATALINA_HOME}/bin/catalina.sh run


