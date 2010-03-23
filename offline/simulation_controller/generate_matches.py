#! /usr/bin/env python

import itertools
from datetime import timedelta
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

# definition
OPPONENTS=["fcportugalX"]
MIN_MATCHES=2

# typical duration of a match
DURATION=timedelta(minutes=12)

def main():
    data = config.strategy_data
    # calculate the expected output size
    nconfigs = reduce(lambda x,y: x*len(y), data.values(),1)

    # print usefull information
    print "{0} configurations.".format(nconfigs)
    print "{0} opponents".format(len(OPPONENTS))
    print "{0} matches per configuration/opponent combination".format(MIN_MATCHES)
    print "{0} expected match duration".format(DURATION)
    totalduration=DURATION*nconfigs*len(OPPONENTS)*MIN_MATCHES
    print "{0} expected total time if no match is cached.".format(totalduration)

    for opponent in OPPONENTS:
        for conf in fcpd_configurations(data):

            logging.debug("using config({0}) against {1}".format(conf,opponent))
            # initialize the teams...
            fcpD = FCPortugal(conf)
            opp = Team(opponent)

            # instantiate the confrontation
            fcpD_vs_opp = Confrontation(fcpD, opp)

            # play the required number of matches
            n_played_matches=len(fcpD_vs_opp.allmatches())
            dbgmsg="{2} - {0} of {1} matches played."
            logging.debug(dbgmsg.format(n_played_matches,MIN_MATCHES,fcpD_vs_opp))

            while n_played_matches < MIN_MATCHES:
                dbgmsg="{2} - {0} of {1} matches played. playing another match."
                logging.debug(dbgmsg.format(n_played_matches, MIN_MATCHES,
                    fcpD_vs_opp))

                fcpD_vs_opp.playnewmatch()
                n_played_matches=len(fcpD_vs_opp.allmatches())



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
        report.report("upload","sound","eject")

