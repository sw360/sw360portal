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

#defaultOpts=(-p 5005 -d)
defaultOpts=()

srcdir="$(dirname "$0")/"
. "${srcdir}dirs.conf"

eval "$( "$srcdir"catalinaOpts.sh "${defaultOpts[@]}" "$@" )"
echo "-[shell provisioning] using catalina options: '${CATALINA_OPTS}'"

# -------------------------------
echo "-[shell provisioning] shutdown liferay"
"$opt"/liferay-portal-6.2*/tomcat-7.0.*/bin/shutdown.sh

echo "-[shell provisioning] moved into the sw360frontend"
cd "$src"/frontend
mvn clean install -Pdeploy

echo "-[shell provisioning] Starting SW360 Liferay server"
cd "$opt"/liferay-portal-6.2*/tomcat-7.0.*/bin/
CATALINA_OPTS="${CATALINA_OPTS}" ./startup.sh

echo "-[shell provisioning] End of sw360 provisioning"
