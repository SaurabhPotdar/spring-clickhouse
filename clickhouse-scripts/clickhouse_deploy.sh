#!/bin/bash

current="$(dirname "$0")"
cd $current

#Random password for default user
declare -x CLICKHOUSE_PASSWORD
CLICKHOUSE_PASSWORD=$(openssl rand -base64 12)

docker-compose up -d