#! /bin/bash

teamdir=$1
#consume the argument
shift

teamComm="${teamdir}/kill"

echo command: $teamComm "$@" > $name.out 2> $name.err
# todo passar o host
$teamComm "$@" > $name.out 2> $name.err

