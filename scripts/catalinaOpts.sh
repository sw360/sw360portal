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
# script for setting debugging options while deploying SW360
#
# author: daniele.fognini@tngtech.com
# -----------------------------------------------------------------------------

set -e
exec 2>/dev/null

debugPort=
devel=
while getopts p:d opt; do
   case ${opt} in
      p) debugPort="$OPTARG";;
      d) devel="yes";;
   esac
done

CATALINA_OPTS=""
if [[ "${debugPort}" =~ ^[0-9]+$ ]]; then
   CATALINA_OPTS+="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${debugPort} "
fi
if [[ -n "${devel}" ]]; then
   CATALINA_OPTS+="-Dorg.ektorp.support.AutoUpdateViewOnChange=true "
fi

echo "CATALINA_OPTS=\"${CATALINA_OPTS}\""
