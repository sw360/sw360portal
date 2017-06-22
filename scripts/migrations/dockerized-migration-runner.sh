#!/usr/bin/env bash

# Copyright Bosch Software Innovations GmbH, 2017.
# Part of the SW360 Portal Project.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

# call as:
# ./dockerized-migration-runner.sh migrationScript [migrationScript [...]]
# e.g.:
# ./dockerized-migration-runner.sh 003_rename_release_contacts_to_contributors.py 004_move_release_ecc_fields_to_release_information.py

set -e

cd "$(dirname "${BASH_SOURCE[0]}")"

# build docker image for migration
docker build -t sw360/migration-runner \
       --rm=true --force-rm=true \
       - < ./dockerized-migration-runner.Dockerfile

for scriptpath in "$@"; do
    scriptname="$(basename $scriptpath)"
    if [ ! -e "$scriptname" ]; then
        echo "migration script \"$scriptname\" not found"
        continue
    fi

    # run docker image
    docker run -it --net=host \
           -v "$(pwd)":/migrations \
           sw360/migration-runner \
           "./$scriptname"
done
