#! /bin/bash

matchdir=$1
teamdir=$2
name=$3
matchhost=$4

#consume the arguments
shift
shift
shift
shift

cd $matchdir
teamComm="${teamdir}/start"

echo command: $teamComm $matchhost $teamdir "$@" > $name.out 2> $name.err
# todo passar o host
$teamComm $matchhost $teamdir "$@" > $name.out 2> $name.err

