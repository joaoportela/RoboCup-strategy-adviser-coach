#! /bin/bash

teamdir=$1
#consume the argument
shift

teamComm="${teamdir}/kill"

$teamComm "$@" > /dev/null 2>&1
