#!/usr/bin/env bash

# -----------------------------------------------------------------------------
#
# Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
#
# This program is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License Version 2.0 as published by the
# Free Software Foundation with classpath exception.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
#  more details.
#
# You should have received a copy of the GNU General Public License along with
# this program (please see the COPYING file); if not, write to the Free
# Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
# 02110-1301, USA.
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
CATALINA_OPTS="${CATALINA_OPTS}" "$opt"/apache-tomcat-7.0.54/bin/startup.sh

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
