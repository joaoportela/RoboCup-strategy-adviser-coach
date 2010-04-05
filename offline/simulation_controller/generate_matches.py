#! /usr/bin/env python

import itertools
import datetime
import logging

import runner # to initialize the logger
import config
from team import *
from confrontation import *
import report
import sys


def fcpd_configurations(data=config.strategy_data):
    for values in itertools.product(*data.values()):
        args={}
        for i, key in enumerate(data.keys()):
            args[key] = values[i]
        yield args

def confrontations(data=config.strategy_data,opponents=config.opponents):
    for opponent in opponents:
        for conf in fcpd_configurations(data):
                # initialize the teams...
                fcpD = FCPortugal(conf)
                opp = Team(opponent)

                # instantiate the confrontation
                fcpD_vs_opp = Confrontation(fcpD, opp)

                yield fcpD_vs_opp

def duration(confrontations=confrontations(), min_matches=config.min_matches, matchduration=config.duration):
    """calculates the duration of generating the required matches

    confrontations - confrontations to be runned
    min_matches - minimum number of matches per confrontation
    matchduration - average match duration"""
    runsmissing=0
    for fcpD_vs_opp in confrontations:
            # play the required number of matches
            n_played_matches=len(fcpD_vs_opp)
            if n_played_matches < min_matches:
                runsmissing+=(min_matches-n_played_matches)

    print "{0} runs estimated to be missing".format(runsmissing)
    totalduration=matchduration*runsmissing
    return totalduration

def naive_prediction(data=config.strategy_data, opponents=config.opponents,
        min_matches=config.min_matches, matchduration=config.duration):
    """predicts the number of matches missing."""
    nconfigs=reduce(lambda x,y: x*len(y), data.values(), 1)
    nruns=min_matches*len(opponents)*nconfigs
    d=matchduration*nruns
    return (d, nruns)

def runmatches(confrontations=confrontations(), min_matches=config.min_matches,
        n_missing=0):
    """confrontations with the matches that have to be run

    confrontations - confrontations to be runned
    min_matches - minimum number of matches per confrontation
    n_missing - if the number of matches missing is supplied prints the
    progress
    """
    if n_missing:
        total_matches_played=0

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
            n_played_matches=len(fcpD_vs_opp)

        if n_missing:
            total_matches_played+=min_matches
            print "progress {0}/{1}".format(total_matches_played, n_missing)

def main():
    cfs=list(confrontations())
    (naive_duration, nmatches)=naive_prediction()
    print "naive prediction: {1} runs, {0} duration".format(naive_duration,
            nmatches)
    totalduration=duration(cfs)
    print "{0} expected total time.".format(totalduration)
    finish=datetime.datetime.now()+totalduration
    print "expected to finish @ {0}".format(finish)
    runmatches(cfs,n_missing=nmatches)

if __name__ == "__main__":
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
        # always report
        report.report("upload")

