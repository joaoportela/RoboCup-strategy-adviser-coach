#! /usr/bin/env python

import logging
import time

def runcommand(command):
    logging.debug("running command: {0}".format(command))
    retcode = os.system(command)
    logging.debug("command returned {0}".format(retcode))

def fake_runcommand(command)
    logging.debug("pretending to run command: {0}".format(command))
    time.sleep(1)
    logging.debug("no return (fake_command)")

