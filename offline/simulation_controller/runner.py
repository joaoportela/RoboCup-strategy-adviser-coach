#! /usr/bin/env python

import logging
import sys
import os
import config
import report

from team import *
from match import *
from confrontation import *
from evaluator import *

METADATA_FILES=["/home/joao/autorun/matches/fcportugalD-formation1_mentality2_gamepace2__vs__fcportugalX/confrontation_metadata.json"]

def dotest(confrontation):
    print len(confrontation)
    print "matches: \n"+"\n".join([str(x) for x in confrontation.allmatches()])

    while len(confrontation) <  2:
        confrontation.playnewmatch()

    peval=PointsEvaluator(confrontation.statistics(),"fcportugalD")
    print "according to the basic evaluator the score is: ", peval.value()

    gdiff=GoalDifferenceEvaluator(confrontation.statistics(),"fcportugalD")
    print "according to the goal difference evaluator the score is: ", gdiff.value()

    reval=ReliefEvaluator(confrontation.statistics(),"fcportugalD")
    reval_v=reval.value()
    print "relief evaluator score ", reval_v

    marseval=MARSEvaluator(confrontation.statistics(),"fcportugalD")
    marseval_v=marseval.value()
    print "MARS evaluator score ", marseval_v

def main():
    # fcpD = FCPortugal({"formation" : 1})
    # fcpX = Team("fcportugalX")
    # fcpD_vs_fcpX = Confrontation(fcpD, fcpX)
    # dotest(fcpD_vs_fcpX)
    for metadata_f in METADATA_FILES:
        dotest(Confrontation.from_metadata(metadata_f))

if __name__ == '__main__':
    # clean the log file
    with open(config.logfile,"w") as f:
        f.truncate(0) # no need for this line because "w" already truncates...

    try:
        logging.info("----------- '%s' started  ----------", sys.argv[0])
        if len(sys.argv) == 3:
            logging.info("running a match between {0} {1}".format()) 
            match = Match(teams=(sys.arg[1], sys.argv[2]))
            loggin.info("match result is: {0}".format(match.result()))
        else:
            logging.info("running main()")
            main()
            logging.info("----------- '%s' finished ----------", sys.argv[0])
    except:
        logging.exception("Unforeseen exception:")
        raise
    finally:
        # always report
        report.report("upload")


