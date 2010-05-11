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
from utils import confrontation_metadata_files

def dotest(confrontation):
    # play the number of matches I want.
    min_matches=1
    print str(confrontation),"matches:",len(confrontation),"/",min_matches
    while len(confrontation) <  min_matches:
        confrontation.playnewmatch()

    # print their info
    print "this confrontation has",len(confrontation), "matches.",
    if len(confrontation):
        print "they are: \n\t"+"\n\t".join([str(x) for x in confrontation.allmatches()])

    peval=PointsEvaluator(confrontation.statistics(),"fcportugalD")
    print "according to the points evaluator the score is: ", peval.value()

    gdiff=GoalDifferenceEvaluator(confrontation.statistics(),"fcportugalD")
    print "according to the goal difference evaluator the score is: ", gdiff.value()

    reval=ReliefEvaluator(confrontation.statistics(),"fcportugalD")
    print "relief evaluator score ", reval.value()

    marseval=MARSEvaluator(confrontation.statistics(),"fcportugalD")
    print "MARS evaluator score ", marseval.value()

def main():
    fcpD = FCPortugal({"formation" : 1})
    fcpX = Team("nemesis")
    fcpD_vs_fcpX = Confrontation(fcpD, fcpX)
    dotest(fcpD_vs_fcpX)
    # for metadata_f in confrontation_metadata_files():
    #     print metadata_f
    #     dotest(Confrontation.from_metadata(metadata_f))

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
        # get the upload pass
        passwd=None
        if len(sys.argv) == 2:
            passwd=sys.argv[1]

        # report
        report.report("upload", passwd=passwd)

