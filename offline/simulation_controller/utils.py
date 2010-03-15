#! /usr/bin/env python

__all__ = ["runcommand", "fake_runcommand", "write_script"]

import logging
import time
import os
import stat

def runcommand(command):
    logging.debug("running command: {0}".format(command))
    retcode = os.system(command)
    logging.debug("command returned {0}".format(retcode))
    return retcode

def fake_runcommand(command):
    logging.debug("pretending to run command: {0}".format(command))
    time.sleep(1)
    logging.debug("no return (fake_command)")
    return 0

def write_script(script_name, content):
    """ writes a script to the disk and chmods it to 755
    """
    if os.path.exists(script_name):
        warnmsg="{0} already exists. it will be overwritten".format(script_name)
        logging.warning(warnmsg)
    # write the script.
    with open(script_name, "w") as f:
        f.write(content)

    # chmod
    mask = stat.S_IRWXU | stat.S_IRGRP | stat.S_IXGRP | stat.S_IROTH | stat.S_IXOTH
    os.chmod(script_name, mask)

