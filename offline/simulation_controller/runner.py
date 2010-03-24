#! /usr/bin/env python

import logging
import sys
import os
import config
import report

# configure log file
FORMAT = "%(asctime)s %(levelname)s: %(message)s"
LOG_FILENAME = os.path.join(config.logfile)
logging.basicConfig(filename=LOG_FILENAME,level=logging.DEBUG,format=FORMAT)
logging.Formatter("%(asctime)s - %(levelname)s:%(message)s")

from team import *
from match import *
from confrontation import *

def main():
    fcpD = FCPortugal({"formation" : 1})
    fcpX = Team("fcportugalX")
    fcpD_vs_fcpX = Confrontation(fcpD, fcpX)
    all_m = fcpD_vs_fcpX.allmatches()
    print len(all_m)
    print "\n".join([str(x) for x in all_m])

    fcpD_vs_fcpX.playnewmatch()


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
        report.report("sound","eject")


