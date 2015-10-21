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
