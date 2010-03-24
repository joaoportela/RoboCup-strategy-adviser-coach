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

# definition
OPPONENTS=["nemesis", "kickofftug", "wrighteagle", "bahia2d"]
MIN_MATCHES=3

# typical duration of a match
DURATION=datetime.timedelta(minutes=12)


def fcpd_configurations(data=config.strategy_data):
    for values in itertools.product(*data.values()):
        args={}
        for i, key in enumerate(data.keys()):
            args[key] = values[i]
        yield args

def confrontations(data=config.strategy_data,opponents=OPPONENTS):
    for opponent in opponents:
        for conf in fcpd_configurations(data):
                # initialize the teams...
                fcpD = FCPortugal(conf)
                opp = Team(opponent)

                # instantiate the confrontation
                fcpD_vs_opp = Confrontation(fcpD, opp)

                yield fcpD_vs_opp

def duration(confrontations=confrontations(), min_matches=MIN_MATCHES, matchduration=DURATION):
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

def runmatches(confrontations=confrontations(), min_matches=MIN_MATCHES):
    """confrontations with the matches that have to be run

    confrontations - confrontations to be runned
    min_matches - minimum number of matches per confrontation
    """
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

def main():
    cfs=list(confrontations())
    totalduration=duration(cfs)
    print "{0} expected total time.".format(totalduration)
    finish=datetime.datetime.now()+totalduration
    print "expected to finish @ {0}".format(finish)
    runmatches(cfs)

#def naive():
#    nconfigs=reduce(lambda x,y: x*len(y), config.strategy_data, 1)
#    print "number of configs is", nconfigs
#    print "number of opponents is", len(OPPONENTS)
#    print "avg match duration is", DURATION
#    print "configs*opponents is", len(OPPONENTS)*nconfigs
#    return DURATION*MIN_MATCHES*len(OPPONENTS)*nconfigs

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

