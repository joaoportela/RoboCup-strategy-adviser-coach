#! /usr/bin/env python

"""script to generate the csv file with the matches
statistics and teams evaluation"""

import config
import logging
from statistics import *
from utils import *
import os
import sys

def walk_xmls(dir_):
    for root, dirs, files in os.walk(dir_):
        for f in files:
            if f.endswith(".xml"):
                yield os.path.join(root,f)

def discoverteamconfig(xml, side):
    dbgmsg="discovering config for {0} team in {1}".format(side,xml)
    logging.debug(dbgmsg)
    if side not in ["left","right"]:
        raise Exception("unkown side argument {0}".format(side))
    if side == "left":
        side = "team_l"
    elif side == "right":
        side = "team_r"

    dirname=os.path.dirname(xml)
    id_=theid(xml)
    metadata=os.path.join(dirname,id_+"_metadata.json")
    with open(metadata) as f:
        data=json.load(f)

    assert data['id'] == id_
    class_ = data[side][0]
    params = data[side][1]

    return params

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
            # TODO construct the team data
            # when the team is fcportugalD include its configuration
            if s.team.lower() == "fcportugald":
                teamdata["team_config"] = discoverteamconfig(xml,side)
            else:
                teamdata["team_config"] = ""
            matchdata.append((s.team,teamdata))
        matches.append(matchdata)

    return matches

def print_data(csv_name, matches):
    # write the file...
    with open(csv_name, 'w') as csv_file:
        # table headers
        headers=matches[0][1].keys()
        csv_file.write("Team;")
        for h in headers:
            csv_file.write(headers)
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
