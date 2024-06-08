#!/bin/bash

current="$(dirname "$0")"
cd $current
cd ..

cd superset-scripts || {
  echo "Error cd"
  exit 1
}

docker build -t superset:latest .