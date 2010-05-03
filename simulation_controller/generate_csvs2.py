#! /usr/bin/env python

""" script to generate the several csv files needed."""

from utils import *
from statistics import *
from csvdata2 import *
import logging
from datastructures import SortedDict

import sys
import os

def walk_statistics(dir_):
    for xml in walk_xmls(dir_):
        logging.debug("processing {0}".format(xml))
        yield Statistics(xml)

def gen_group1(dir_, filename=None):
    rows=[]
    for st in walk_statistics(dir_):
        for side in ["left", "right"]:
            st.side=side
            teamdata=SortedDict()
            teamdata.update(statistics14(st))

            # another row.
            rows.append(teamdata)

    if filename is not None:
        with open(filename, "w") as f:
            f.write(";".join(statistics14_keys)+"\n")
            for row in rows:
                f.write(";".join([str(value) for _, value in row.iteritems()]))
                f.write("\n")

    return rows

def gen_group2(matchesdir, target_dir=None):
    teams={}
    for st in walk_statistics(matchesdir):
        for side in ["left", "right"]:
            st.side=side
            if st.team not in teams:
                teams[st.team]=[]

            teamdata=SortedDict()
            teamdata.update(statistics14(st))

            # another row
            teams[st.team].append(teamdata)


    # write
    if target_dir is not None:
        if not os.path.isdir(target_dir):
            os.mkdir(target_dir)
        for teamname, teamdata in teams.iteritems():
            fname=os.path.join(target_dir, teamname+".csv")
            with open(fname, "w") as f:
                f.write(";".join(statistics14_keys)+"\n")
                for row in teamdata:
                    f.write(";".join([str(value) for _, value in row.iteritems()]))
                    f.write("\n")

    return teams


def validate(argv):
    foldername=argv[1].split("/")[-1]
    return len(argv)==2 and os.path.isdir(argv[1])# and foldername=="matches"

def usage():
    return "usage: python {0} <STATISTICS_DIR>".format(sys.argv[0])

if __name__ == "__main__":
    if validate(sys.argv):
        print "generating csvs for {0}".format(sys.argv[0])
        print "group 1..."
        gen_group1(sys.argv[1],"group1.csv")
        print "group1 generated. group2..."
        gen_group2(sys.argv[1],"group2")
        print "group2 done"
    else:
        print usage()
