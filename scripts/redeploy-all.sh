#!/usr/bin/env bash

# -----------------------------------------------------------------------------
#
# Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# provisioning script for deploying the SW360 project
#
# author: daniele.fognini@tngtech.com
# -----------------------------------------------------------------------------

set -e

defaultOptsBackend=()
defaultOptsFrontend=()

# to set debug ports and mode
#defaultOptsBackend=(-p 5006 -d)
#defaultOptsFrontend=(-p 5005 -d)

srcdir="$( realpath "$(dirname "$0")" )"
. "${srcdir}/dirs.conf"

echo "-[shell provisioning] Stopping SW360 Liferay server"
"$opt"/liferay-portal-6.*/tomcat-7.0.*/bin/shutdown.sh

eval "$( "$srcdir"/catalinaOpts.sh "${defaultOptsBackend[@]}" "$@" )"
echo "-[shell provisioning] Starting Backed Tomcat, opts: '${CATALINA_OPTS}'"
CATALINA_OPTS="${CATALINA_OPTS}" "$opt"/apache-tomcat-8.*/bin/startup.sh

echo "-[shell provisioning] Undeploy backend"
cd "$src"/backend
mvn tomcat7:undeploy

echo "-[shell provisioning] Maven clean install"
cd "$src"
mvn clean install

echo "-[shell provisioning] Deploy backend"
cd "$src"/backend
mvn tomcat7:deploy-only

echo "-[shell provisioning] Deploy frontend"
cd "$src"/frontend
mvn install -Pdeploy

eval "$( "$srcdir"/catalinaOpts.sh "${defaultOptsFrontend[@]}" "$@" )"
echo "-[shell provisioning] Starting SW360 Liferay server, opts: '${CATALINA_OPTS}'"
CATALINA_OPTS="${CATALINA_OPTS}" "$opt"/liferay-portal-6.*/tomcat-7.0.*/bin/startup.sh

echo "-[shell provisioning] End of sw360 provisioning"
