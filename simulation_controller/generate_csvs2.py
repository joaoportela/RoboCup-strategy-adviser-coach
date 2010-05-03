#! /usr/bin/env python

""" script to generate the several csv files needed."""

from utils import *
from statistics import *
from csvdata2 import *
import logging
from datastructures import SortedDict

import sys
import os

T_CONFIG_NAMES=["formation","mentality","gamepace"]

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

def gen_group3(matchesdir, filename=None):
    ROWS_HEAD=evaluators_keys+T_CONFIG_NAMES
    rows=[]
    for st in walk_statistics(matchesdir):
        for side in ["left", "right"]:
            st.side=side
            if not(st.team.lower() == "fcportugald"):
                # each row contains fcportugal data only
                continue

            teamdata=SortedDict()
            teamdata.update(evaluators(st))
            t_config=discoverteamconfig(st.xml,st.side)[0]
            teamdata["formation"]=t_config["formation"]
            teamdata["mentality"]=t_config["mentality"]
            teamdata["gamepace"]=t_config["gamepace"]

            # another row.
            rows.append(teamdata)

    if filename is not None:
        with open(filename, "w") as f:
            f.write(";".join(ROWS_HEAD)+"\n")
            for row in rows:
                f.write(";".join([str(value) for _, value in row.iteritems()]))
                f.write("\n")

    return rows

def teams_sides(st):
    backup=st.side
    st.side="left"
    if st.team.lower() == "fcportugald":
        fcp="left"
        other="right"
    else:
        fcp="right"
        other="left"

        # sanity check
        st.side="right"
        assert st.team.lower() == "fcportugald"

    st.side=backup

    return (fcp,other)

def gen_group4(matchesdir, filename=None):
    ROWS_HEAD=evaluators_keys+T_CONFIG_NAMES+["opponent"]+statistics14_keys
    rows=[]
    for st in walk_statistics(matchesdir):
        fcp,other = teams_sides(st)
        teamsdata=SortedDict()

        st.side=fcp
        teamsdata.update(evaluators(st))
        t_config=discoverteamconfig(st.xml,st.side)[0]
        teamsdata["formation"]=t_config["formation"]
        teamsdata["mentality"]=t_config["mentality"]
        teamsdata["gamepace"]=t_config["gamepace"]

        st.side=other
        teamsdata["opponent"]=st.team
        teamsdata.update(statistics14(st))

        #another row
        rows.append(teamsdata)

    if filename is not None:
        with open(filename, "w") as f:
            f.write(";".join(ROWS_HEAD)+"\n")
            for row in rows:
                f.write(";".join([str(value) for _, value in row.iteritems()]))
                f.write("\n")

    return rows


def validate(argv):
    foldername=argv[1].split("/")[-1]
    return len(argv)==2 and os.path.isdir(argv[1])# and foldername=="matches"

def usage():
    return "usage: python {0} <STATISTICS_DIR>".format(sys.argv[0])

if __name__ == "__main__":
    if validate(sys.argv):
        logging.info("STARTING {0}".format(sys.argv[0]))
        print "generating csvs for {0}".format(sys.argv[1])
        print "group1..."
        gen_group1(sys.argv[1],"group1.csv")
        print "group1 done. group2..."
        gen_group2(sys.argv[1],"group2")
        print "group2 done. group3..."
        gen_group3(sys.argv[1],"group3.csv")
        print "group3 done. group 4..."
        gen_group4(sys.argv[1],"group4.csv")
        print "group4 done."
        logging.info("DONE")
    else:
        print usage()
