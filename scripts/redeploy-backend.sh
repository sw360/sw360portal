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
# provisioning script for deploying prerequisites
# in the Vagrant machine of the SW360 project
#
# initial author: michael.c.jaeger@siemens.com
#         author: cedric.bodet@tngtech.com
# $Id$
# -----------------------------------------------------------------------------

set -e

#defaultOpts=(-p 5006 -d)
defaultOpts=()

srcdir="$(dirname "$0")/"
. "${srcdir}dirs.conf"

eval "$( "$srcdir"catalinaOpts.sh "${defaultOpts[@]}" "$@" )"
echo "-[shell provisioning] using catalina options: '${CATALINA_OPTS}'"

echo "-[shell provisioning] Cleaning Tomcat"
"$opt"/apache-tomcat-8.0.*/bin/shutdown.sh
rm -rf "$opt"/apache-tomcat-8.0.*/webapps/*service*

echo "-[shell provisioning] Cleaning Maven"
cd "$src"/libraries
mvn clean
cd "$src"/backend
mvn clean

echo "-[shell provisioning] Starting Tomcat for the backend"
cd "$opt"/apache-tomcat-8.0.*/bin/
CATALINA_OPTS="${CATALINA_OPTS}" ./startup.sh

echo "-[shell provisioning] Start of installing of SW360"
cd "$src"/libraries
mvn install
cd "$src"/backend
mvn install tomcat7:redeploy-only

echo "-[shell provisioning] End of sw360 provisioning"
