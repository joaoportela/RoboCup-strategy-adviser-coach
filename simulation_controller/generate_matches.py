#! /usr/bin/env python

import itertools
import datetime
import logging

import config
import logging
from team import *
from confrontation import *
from utils import *
import report
import sys


def fcpd_configurations_by_strategy(data=config.strategy_data):
    for values in itertools.product(*data.values()):
        args={}
        for i, key in enumerate(data.keys()):
            args[key] = values[i]
        yield args

def confrontations_by_strategy(data=config.strategy_data,opponents=config.opponents):
    for opponent in opponents:
        for conf in fcpd_configurations_by_strategy(data):
                # initialize the teams...
                fcpD = FCPortugal(strategy_params=conf)
                opp = Team(opponent)

                # instantiate the confrontation
                fcpD_vs_opp = Confrontation(fcpD, opp)

                yield fcpD_vs_opp

def fcpd_configurations_by_decisiontree(decision_trees,window_sizes):
    for dt in decision_trees:
        for ws in window_sizes:
            res={}
            res['decision_tree']=dt
            res['window_size']=ws
            yield res


def confrontations_by_decisiontree(decision_trees=config.decision_trees,
        window_sizes=config.window_sizes, opponents=config.opponents):
    for opponent in opponents:
        for tree_conf in fcpd_configurations_by_decisiontree(decision_trees,window_sizes):
            fcpD = FCPortugal(**tree_conf)
            opp = Team(opponent)

            # instantiate the confrontation
            fcpD_vs_opp = Confrontation(fcpD, opp)

            yield fcpD_vs_opp

def smart_prediction(confrontations=confrontations_by_strategy(), min_matches=config.min_matches,
        matchduration=config.duration, matchsize=config.size):
    """calculates the duration of generating the required matches

    confrontations - confrontations to be runned
    min_matches - minimum number of matches per confrontation
    matchduration - average match duration
    """
    runsmissing=0
    for fcpD_vs_opp in confrontations:
            # play the required number of matches
            n_played_matches=len(fcpD_vs_opp)
            if n_played_matches < min_matches:
                runsmissing+=(min_matches-n_played_matches)

    print "{0} runs estimated to be missing".format(runsmissing)
    totalduration=matchduration*runsmissing
    totalsize=matchsize*runsmissing
    return (runsmissing, totalduration, totalsize)

def naive_prediction(data=config.strategy_data, opponents=config.opponents,
        min_matches=config.min_matches, matchduration=config.duration,
        matchsize=config.size):
    """predicts the number of matches missing.
    does not work for decision tree runs."""
    nconfigs=reduce(lambda x,y: x*len(y), data.values(), 1)
    # print "nconfigs", nconfigs
    # print "nopponents", len(opponents)
    # print "min_matches", min_matches
    nruns=min_matches*len(opponents)*nconfigs
    duration=matchduration*nruns
    disk_space=matchsize*nruns
    return (nruns, duration, disk_space)

def runmatches(confrontations=confrontations_by_strategy(), min_matches=config.min_matches,
        matches_missing=0, matchduration=config.duration):
    """confrontations with the matches that have to be run

    confrontations - confrontations to be runned
    min_matches - minimum number of matches per confrontation
    matches_missing - if the total number of matches missing is supplied prints
    the progress
    """
    if matches_missing:
        matches_played=0

    for fcpD_vs_opp in confrontations:
        # play the required number of matches
        n_played_matches=len(fcpD_vs_opp)
        dbgmsg="{2} - {0} of {1} matches played."
        logging.debug(dbgmsg.format(n_played_matches,min_matches,fcpD_vs_opp))

        while n_played_matches < min_matches:
            dbgmsg="{2} - {0} of {1} matches played, playing new match."
            logging.debug(dbgmsg.format(n_played_matches, min_matches,
                fcpD_vs_opp))

            fcpD_vs_opp.playnewmatch()
            if matches_missing:
                # update the counter...
                matches_played+=1
                print_progress(matches_played, matches_missing,
                        matchduration=matchduration)
            n_played_matches=len(fcpD_vs_opp)

def print_progress(matches_played, matches_missing, matchduration=config.duration):
    now=datetime.datetime.now()
    stillmissing=matches_missing-matches_played
    etime=now+stillmissing*matchduration
    msg="{0} progress {1}/{2} - finish @ {3}".format(now,
            matches_played, matches_missing, etime)
    logging.info(msg)
    print msg

def main():
    (nmatches, naive_duration, naive_size)=naive_prediction()
    naive_prediction_msg="naive prediction: {1} runs, {0} duration".format(naive_duration, nmatches)
    print naive_prediction_msg
    logging.info(naive_prediction_msg)

    cfs=list(confrontations_by_decisiontree())
    (nmatches_missing, duration, size)=smart_prediction(cfs)
    print "{0} expected duration time.".format(duration)
    print "{0} expected size.".format(human_size(size))
    finish=datetime.datetime.now()+duration

    expected_finish_str="expected to finish @ {0}".format(finish)
    print expected_finish_str
    logging.info(expected_finish_str)

    report.report("upload",passwd=passwd)

    # runmatches(cfs,matches_missing=nmatches_missing)

if __name__ == "__main__":
    # get the upload pass
    global passwd
    passwd=None
    if len(sys.argv) == 2:
        passwd=sys.argv[1]

    # clean the log file
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
        # report
        report.report("upload", passwd=passwd)

