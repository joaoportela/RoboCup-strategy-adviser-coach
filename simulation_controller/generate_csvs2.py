#! /usr/bin/env python

""" script to generate the several csv files needed."""

import config
from utils import *
from statistics import *
from csvdata2 import *
import logging
from datastructures import SortedDict
import report

import os
import sys
import shutil
import tarfile
from functools import partial

T_CONFIG_NAMES=["formation","mentality","gamepace"]

def walk_statistics(dir_):
    for xml in walk_xmls(dir_):
        logging.debug("processing {0}".format(xml))
        yield Statistics(xml)

def gen_group1(dir_, filename=None):
    ROWS_HEAD=BONUS_INFO_KEYS+STATISTICS14_KEYS
    rows=[]
    for st in walk_statistics(dir_):
        for side in ["left", "right"]:
            st.side=side
            teamdata=SortedDict()
            teamdata.update(bonus_info(st))
            teamdata.update(statistics14(st))

            # another row.
            rows.append(teamdata)

    if filename is not None:
        with open(filename, "w") as f:
            f.write(";".join(ROWS_HEAD)+"\n")
            for row in rows:
                f.write(";".join([str(value) for _, value in row.iteritems()]))
                f.write("\n")

    return rows

def gen_group2(matchesdir, target_dir=None):
    ROWS_HEAD=BONUS_INFO_KEYS+STATISTICS14_KEYS
    teams={}
    for st in walk_statistics(matchesdir):
        for side in ["left", "right"]:
            st.side=side
            if st.team not in teams:
                teams[st.team]=[]

            teamdata=SortedDict()
            teamdata.update(bonus_info(st))
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
                f.write(";".join(STATISTICS14_KEYS)+"\n")
                for row in teamdata:
                    f.write(";".join([str(value) for _, value in row.iteritems()]))
                    f.write("\n")

    return teams

def gen_group3(matchesdir, filename=None):
    ROWS_HEAD=BONUS_INFO_KEYS+EVALUATORS_KEYS+T_CONFIG_NAMES
    rows=[]
    for st in walk_statistics(matchesdir):
        for side in ["left", "right"]:
            st.side=side
            if not(st.team.lower() == "fcportugald"):
                # each row contains fcportugal data only
                continue

            teamdata=SortedDict()
            teamdata.update(bonus_info(st))
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
    ROWS_HEAD=BONUS_INFO_KEYS+EVALUATORS_KEYS+T_CONFIG_NAMES+["opponent"]+STATISTICS14_KEYS
    rows=[]
    for st in walk_statistics(matchesdir):
        fcp,other = teams_sides(st)
        teamsdata=SortedDict()

        st.side=fcp
        teamsdata.update(bonus_info(st))
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


def valid(argv):
    return len(argv)==2 and os.path.isdir(argv[1])

def compress_cleanup(tarname,filestotar):
    tar = tarfile.open(tarname,'w:gz')
    try:
        add_striped(tar,filestotar)
    finally:
        tar.close()

    for f in filestotar:
        if os.path.isdir(f):
            shutil.rmtree(f)
        else:
            os.remove(f)


def usage():
    return "usage: python {0} <STATISTICS_DIR>".format(sys.argv[0])

def main():
    if valid(sys.argv):
        j=partial(os.path.join,config.running_dir)
        logging.info("STARTING {0}".format(sys.argv[0]))
        print "generating csvs for {0}".format(sys.argv[1])
        print "group1..."
        gen_group1(sys.argv[1],j("group1.csv"))
        print "group1 done. group2..."
        gen_group2(sys.argv[1],j("group2"))
        print "group2 done. group3..."
        gen_group3(sys.argv[1],j("group3.csv"))
        print "group3 done. group 4..."
        gen_group4(sys.argv[1],j("group4.csv"))
        print "group4 done."

        # now compress in a convenient archive.
        tarname=j("csvs.tar.gz")
        filestotar=[j("group1.csv"),j("group2"), j("group3.csv"),
                j("group4.csv")]
        compress_cleanup(tarname,filestotar)

        if password is not None:
            report.dotheupload(tarname,password)
    else:
        print usage()

if __name__ == "__main__":
    # get the password before anything else so that the argv can be clean.
    password=None
    for i, arg in enumerate(sys.argv):
        if arg.startswith("-p"):
            password=arg[2:]
            del sys.argv[i]
            break

    with open(config.logfile,"w") as f:
        f.truncate(0) # no need for this line because "w" already truncates...
    try:
        logging.info("----------- '%s' started  ----------", sys.argv[0])
        logging.info("running main()")
        main()
        logging.info("----------- '%s' finished ----------", sys.argv[0])
    except:
        logging.exception("Unforeseen exception:")
        raise
    finally:
        # always report
        report.report("upload", passwd=password)

