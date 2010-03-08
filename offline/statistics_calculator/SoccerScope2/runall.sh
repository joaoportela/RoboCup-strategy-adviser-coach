#! /bin/bash

#find . -type f -name *.rcg.gz -exec basename {} \;
#find . -type f -name *.rcg.gz -exec dirname {} \;

files_dir="/home/joao/robocup/logs/"

if [[ -d $1 ]]; then
   files_dir=$1
else
#   echo "defaulting to $files_dir"
   echo "usage: $0 <files_directory>"
   exit
fi

# change input field separator to \n so that spaces are not 
# considered separators
IFS='
'
files_to_process=`find $files_dir -type f -name *.rcg.gz`

MYCLASSPATH=soccerscope.jar:java-xmlbuilder-0.3.jar

failcount=0
successcount=0

rm outputs.txt -vf
for RCG in ${files_to_process[*]}
do
   echo "processing $RCG"
   basen=`basename ${RCG}`
   dirn=`dirname ${RCG}`
   XML="${dirn}/${basen}.xml"
   CLASSPATH=${MYCLASSPATH} java soccerscope.SoccerScope --batch ${RCG} ${XML} >> outputs.txt 2>&1
   if [[ $? -ne 0 ]]; then
      echo "failed!!!! $RCG"
      let "failcount++"
   else
      echo "created    $XML"
      let "successcount++"
   fi
   echo ""
done

echo "failed $failcount times, succeeded $successcount times"
