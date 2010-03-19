#! /usr/bin/env python

__all__ = ["runcommand", "fake_runcommand", "write_script", "allrcgs",
        "allmatchesids", "theid" ]

import logging
import time
import os
import stat
import glob

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

# from the filename (basename) get the numbers in the beggining (the date)

def theid(name):
    NUMBERS_PATTERN = re.compile(r'\d+')
    return NUMBERS_PATTERN.match(os.path.basename(name)).group(0)

def allrcgs(confrontationdir):
    """search for all the rcgs in "confrontationdir" directory
    """

    logging.info("searching all rcgs in {confrontationdir}".format(**locals()))

    # the matches files are the games logs (end in .rcg.gz)
    possible_files = glob.glob(os.path.join(confrontationdir,"*.rcg.gz"))
    # but do not include the converted ones...
    dont_include_convert = lambda fname: not fname.endswith("_convert.rcg.gz")
    possible_files = filter(dont_include_convert, possible_files)

    return sorted(possible_files, key=theid)

# TODO - use metadata?
def allmatchesids(confrontationdir):
    """search for all the matches ids in "confrontationdir" directory
    (from the rcg files...)"""

    possible_files = allrcgs(confrontationdir)
    ids = map(theid, possible_files)

    # check that all the ids have the correct length
    correct_len = 12
    assert all(map(lambda x: len(x) == correct_len, ids))

    return sorted(justids)

