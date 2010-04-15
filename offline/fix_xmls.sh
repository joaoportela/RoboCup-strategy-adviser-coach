#! /bin/bash

# script to fix a small error that occured when generating the xmls.
# the java that generates the xmls was already fixed but this fixes old xml versions.

if [ -z "$1" ]
then
        echo "\$1 is not defined"
	exit
fi

bname=`basename $1`
if [ "$bname" != "matches" ]
then
	echo "this is not the directory you want. exiting"
	exit
fi


matchesdir=$1
cd $matchesdir || (echo "cannot change dir" && exit 255)

sed -i "s/<analysis>/<analysis version=\"1.0\">/g" */*.xml
sed -i "s/ side=\"-1\"/ side=\"RIGHT_SIDE\"/g" */*.xml
sed -i "s/ side=\"1\"/ side=\"LEFT_SIDE\"/g" */*.xml
sed -i "s/ side=\"0\"/ side=\"NEUTRAL\"/g" */*.xml
sed -i "s/ side=\"/ team=\"/g" */*.xml
echo "worked!"
