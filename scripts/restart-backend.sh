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

sleep 10s

cd "$opt"/apache-tomcat-8.0.*/bin/
CATALINA_OPTS="${CATALINA_OPTS}" ./startup.sh
