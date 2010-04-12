#! /bin/bash

if [[ -d $1 ]]; then
   files_dir=$1
else
   echo "usage: $0 <files directory>"
   exit
fi

echo "processing $1"

# change input field separator to \n so that spaces are not 
# considered separators
IFS='
'
files_to_process=`find $files_dir -type f -name *.gz`
target_lvl=9

for file in ${files_to_process[*]}
do
	# (file -i ${file} | grep "application/x-gzip;")
	echo -n "recompressing ${file}... "
	gunzip ${file} && gzip -${target_lvl} ${file%.gz};
	if [[ $? -ne 0 ]]; then
		echo "failed."
	else
		echo "success."
	fi
done

# old compression command... (does the same as the current command)
# (gunzip -c ${file} | gzip -${target_lvl}) > ${file}.moarcompression && mv ${file}.moarcompression ${file}
