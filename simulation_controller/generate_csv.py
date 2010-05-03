#! /usr/bin/env python

"""
script to generate the csv file with the matches statistics and teams
evaluation
"""

import config
import logging
from statistics import *
from utils import *
import csv_data
from evaluator import *
import os
import sys
import json

def get_data(dir_):
    # get the data
    matches=[]
    for xml in  walk_xmls(dir_):
        matchdata=[]
        logging.debug("processing {0}".format(xml))
        s = Statistics(xml)
        for side in ["left","right"]:
            teamdata={}
            s.side=side
            if s.team.lower() == "fcportugald":
                teamdata["team_config"] = discoverteamconfig(xml,side)
            else:
                teamdata["team_config"] = ""

            # the actual data
            for name, constructor in csv_data.EVALUATORS:
                ev=constructor(s,s.team)
                teamdata[name]=ev.value()
            for name, function, kwargs in csv_data.STATISTICS:
                teamdata[name]=function(s,**kwargs)

            matchdata.append((s.team,teamdata))
        matches.append(matchdata)

    return matches

def print_data(csv_name, matches):
    # headers=matches[0][0][1].keys()
    headers=[item[0] for item in csv_data.EVALUATORS+csv_data.STATISTICS]
    headers.insert(0,"team_config")
    # write the file...
    with open(csv_name, 'w') as csv_file:
        # table headers
        csv_file.write("Team; "+"; ".join(headers)+"\n")
        for match in matches:
            for team,teamdata in match:
                csv_file.write(team+"; ")
                for h in headers:
                    csv_file.write(str(teamdata[h])+"; ")
                csv_file.write("\n")
            csv_file.write("\n")

def gen_csv(dir_, csv_name):
    matches=get_data(dir_)
    print_data(csv_name,matches)


def usage():
    return "usage: python {0} <STATISTICS_DIR>".format(sys.argv[0])

if __name__ == "__main__":
    if len(sys.argv) == 2:
        print "generating output.csv for {0}".format(sys.argv[0])
        gen_csv(sys.argv[1],"output.csv")
    else:
        print usage()
