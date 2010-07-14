#! /bin/bash

set -u

MYCLASSPATH=soccerscope.jar:java-xmlbuilder-0.3.jar
if [ $# -eq 2 ];then
	batch="--batch"
	RCG=$1
	XML=$2
else
	batch=""
	RCG=""
	XML=""
fi

CLASSPATH=${MYCLASSPATH} java soccerscope.SoccerScope "${batch}" "${RCG}" "${XML}" > soccerscope.out 2> soccerscope.err
echo "return value $?"
