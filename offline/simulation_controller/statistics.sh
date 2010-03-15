#! /bin/bash

rcg={rcg}
rcgconver={rcgconvert}
rcgc={rcgc}
xml={xml}

# convert to the right version
${{rcgconvert}} -v 3 -o ${{rcgc}} ${{rcg}}

# generate the statistics
MYCLASSPATH="soccerscope.jar:java-xmlbuilder-0.3.jar"
CLASSPATH=${{MYCLASSPATH}} java soccerscope.SoccerScope --batch ${{rcgc}} ${{xml}}

# JAVA_COMMAND="CLASSPATH={CLASSPATH} java soccerscope.SoccerScope --batch {rcg} {xml}"
