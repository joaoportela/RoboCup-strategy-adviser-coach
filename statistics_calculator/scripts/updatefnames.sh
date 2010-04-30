#! /bin/bash

if [ -d $1 ]
then
	echo "doing sutffs in directory $1"
else
	echo "directory argument not provided"
	exit
fi

find $1 -type f -name "*.xml" > filenames.txt
