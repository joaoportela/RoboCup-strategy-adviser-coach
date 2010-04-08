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

def main():
    fcpD = FCPortugal({"formation" : 1})
    fcpX = Team("fcportugalX")
    fcpD_vs_fcpX = Confrontation(fcpD, fcpX)
    print len(fcpD_vs_fcpX)
    print "matches: \n"+"\n".join([str(x) for x in fcpD_vs_fcpX.allmatches()])

    while len(fcpD_vs_fcpX) <  1:
        fcpD_vs_fcpX.playnewmatch()

    beval=BasicEvaluator(fcpD_vs_fcpX.statistics(),"fcportugalD")
    print "according to the basic evaluator the score is: ", beval.value()

    gdiff=GoalDifferenceEvaluator(fcpD_vs_fcpX.statistics(),"fcportugalD")
    print "according to the goal difference evaluator the score is: ", gdiff.value()

    reval=ReliefEvaluator(fcpD_vs_fcpX.statistics(),"fcportugalD")
    reval_v=reval.value()
    print "relief evaluator score (zero for now)... ", reval_v


if __name__ == '__main__':
    # clean the log file
    with open(config.logfile,"w") as f:
        f.truncate(0) # no need for this line because "w" already truncates...

    try:
        logging.info("----------- '%s' started  ----------", sys.argv[0])
        if len(sys.argv) == 3:
            match = match(sys.arg[1], sys.argv[2])
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


