# Copyright Siemens AG, 2017.
# Part of the SW360 Portal Project.
#
# SPDX-License-Identifier: EPL-1.0
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#!/usr/bin/env bash

docker=`pwd`
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
cd ${docker}
sed -i '' "s|LIFERAY_PATH|$LIFERAY_PATH|g" portal-ext-local.properties
cp -rf portal-ext-local.properties ${LIFERAY_PATH}/tomcat-7.0.62/webapps/ROOT/WEB-INF/classes/portal-ext.properties
git checkout -- portal-ext-local.properties
cd ..
mvn clean install -DskipTests -Pdeploy -Ddeploy.dir=${LIFERAY_PATH}/deploy -Dwebapps.dir=${LIFERAY_PATH}/tomcat-7.0.62/webapps

echo ">>> Starting Tomcat of Liferay"
#set CATALINA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005" in setenv.sh for debug
${LIFERAY_PATH}/tomcat-7.0.62/bin/catalina.sh run