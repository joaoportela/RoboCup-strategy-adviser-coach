#! /usr/bin/env python

"""script to generate the csv file with the matches
statistics and teams evaluation"""

import config
import logging
from statistics import *
import os
import sys

def walk_xmls(dir_):
    for root, dirs, files in os.walk(dir_):
        for f in files:
            if f.endswith(".xml"):
                yield os.path.join(root,f)

def gen_csv(dir_, csv_name):
    matches=[]
    for xml in  walk_xmls(dir_):
        matchdata=[]
        logging.debug("processing {0}".format(xml))
        s = Statistics(xml)
        for side in ["left","right"]:
            teamdata=["empty"]
            s.side=side
            # TODO construct the team data
            # TODO when the team is fcportugalD print its configuration
            matchdata.append((s.team,teamdata))
        matches.append(matchdata)

    # write the file...
    with open(csv_name, 'w') as csv_file:
        # TODO print headers
        for match in matches:
            for team,teamdata in match:
                csv_file.write(team+"; ")
                for d in teamdata:
                    csv_file.write(str(d)+"; ")
                csv_file.write("\n")
            csv_file.write("\n")

def usage():
    return "usage: python {0} <STATISTICS_DIR>".format(sys.argv[0])

if __name__ == "__main__":
    if len(sys.argv) == 2:
        print "generating output.csv for {0}".format(sys.argv[0])
        gen_csv(sys.argv[1],"output.csv")
    else:
        print usage()
