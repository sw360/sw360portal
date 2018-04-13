#!/usr/bin/env bash

sw360=`pwd`
killall java

if [ "$1" == "reinstall" ]; then
echo ">>> Re Install liferay"
cd ${LIFERAY_PATH}
cd ..
rm -rf liferay-portal-6.2-ce-ga5/
wget https://sourceforge.net/projects/lportal/files/Liferay%20Portal/6.2.4%20GA5/liferay-portal-tomcat-6.2-ce-ga5-20151119152357409.zip/download \
          -O liferay-portal-tomcat-6.2-ce-ga5.zip
unzip liferay-portal-tomcat-6.2-ce-ga5.zip
rm liferay-portal-tomcat-6.2-ce-ga5.zip
fi

echo ">>> Install sw360portal"
cd ${sw360}
cp -rf portal-ext.properties ${LIFERAY_PATH}/tomcat-7.0.62/webapps/ROOT/WEB-INF/classes/portal-ext.properties
mvn clean install -DskipTests -Pdeploy -Ddeploy.dir=${LIFERAY_PATH}/deploy -Dwebapps.dir=${LIFERAY_PATH}/tomcat-7.0.62/webapps

echo ">>> Starting Tomcat of Liferay"
set JPDA_OPTS="-agentlib:jdwp=transport=dt_socket, address=5005, server=y, suspend=n"
${LIFERAY_PATH}/tomcat-7.0.62/bin/catalina.sh jpda start