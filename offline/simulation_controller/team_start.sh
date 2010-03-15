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

echo "--$name" >> le_argv.txt
python /home/joao/RoboCup-strategy-adviser-coach/offline/simulation_controller/print_argv.py $teamComm $matchhost $teamdir "$@" >> $matchdir/le_argv.txt
echo "--"

echo command: $teamComm $matchhost $teamdir "$@" > $name.out 2> $name.err
# todo passar o host
$teamComm $matchhost $teamdir "$@" > $name.out 2> $name.err

