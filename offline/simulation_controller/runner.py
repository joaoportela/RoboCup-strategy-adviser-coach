#! /usr/bin/env python

import logging
import sys
import os
import config

# configure log file
FORMAT = "%(asctime)s %(levelname)s: %(message)s"
LOG_FILENAME = os.path.join(config.logfile)
logging.basicConfig(filename=LOG_FILENAME,level=logging.DEBUG,format=FORMAT)
logging.Formatter("%(asctime)s - %(levelname)s:%(message)s")

from team import *
from match import *

def main():
    match = Match("fcportugalX", FCPortugal({"formation": 1}))
    print match.play()
    match = Match("bahia2d", FCPortugal({"formation": 1}))
    print match.play()
    match = Match("wrighteagle", FCPortugal({"formation": 1}))
    print match.play()

if __name__ == '__main__':
    try:
        logging.info("----------- '%s' started  ----------", sys.argv[0])
        main()
        logging.info("----------- '%s' finished ----------", sys.argv[0])
    except:
        logging.exception("Unforeseen exception:")
        raise
