#! /usr/bin/env python

import logging
from utils import runcommand, fake_runcommand

CLASSPATH="soccerscope.jar:java-xmlbuilder-0.3.jar"

CONVERT_COMMAND="{config.rcgconvert} -v 3 -o {rcgc} {rcg}"
JAVA_COMMAND="CLASSPATH={CLASSPATH} java soccerscope.SoccerScope --batch {rcg} {xml}"

# convert the rcg to a supported version
def _rcgconvert(rcg):
    bname, ext = os.path.splitext(rcg)
    if ext == ".gz":
        bname, ext2 = os.path.splitext(bname)
        ext = ext+ext2
    rcgc = bname + "_convert" + ext
    command = CONVERT_COMMAND.format(**locals())
    fake_runcommand(command)

def calculate(rcg):
    rcg=_rcgconvert(rcg)
    xml=rcg+".xml"
    command = JAVA_COMMAND.format(CLASSPATH=CLASSPATH,**locals())
    fake_runcommand(command)
    return xml

