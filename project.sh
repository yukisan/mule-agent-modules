#!/usr/bin/env bash

BASEDIR=$(dirname $0)

usage() { echo "Usage: $0 [-c (compile)]" 1>&2; exit 1; }

run_maven=0

while :; do
  case $1 in
    -c|--compile)
      echo "Compiling project" >&2
      run_maven=1
      ;;
    -h|--help|-\?)
      usage
      ;;
    *)
      break
  esac
  shift
done


if [ ${run_maven} -eq 1 ]
then
    echo Cleaning platform distribution.....
    mvn clean install -Denvironment=dev -DBUILD_NUMBER=1 -DJOB_NAME=local-build -f $BASEDIR/pom.xml
fi