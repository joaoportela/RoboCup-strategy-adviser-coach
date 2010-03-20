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

def main():
    match = Match(("fcportugalX", FCPortugal({"formation": 1})))
    print match.result()
    # match = Match(("bahia2d", FCPortugal({"formation": 1})))
    # print match.result()
    # match = Match(("wrighteagle", FCPortugal({"formation": 1})))
    # print match.result()

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

    # reporting results
    report.report(rtypes=["upload"])

