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
    # match = Match("fcportugalX", "fcportugalY")
    # match = Match("fcportugalX", "fcportugalD")
    match = Match("fcportugalX", FCPortugal({"formation": 1}))
    print match.play()

if __name__ == '__main__':
    logging.info("----------- '%s' started -----------", sys.argv[0])
    main()
