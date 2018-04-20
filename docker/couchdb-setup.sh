#! /bin/bash

bash -c 'sleep 20s && curl -H "Content-Type: application/json" -X POST -d '"'"'{"action":"enable_single_node","password":"admin","port":"5984","singlenode":"true","username":"admin"}'"'"' http://localhost:5984/_cluster_setup' &
disown
echo "$1"
set -- /docker-entrypoint.sh "$@"
echo "$@"

exec "$@"

