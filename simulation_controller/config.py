#! /usr/bin/env python

import os
import logging
import datetime

def _gen_strategy(base_strategy):
    from generate_strategy import Strategy
    s=Strategy()
    s.write(base_strategy)

def _number_of_tactics(base_strategy):
    #garantee that the base_strategy was generated
    _gen_strategy(base_strategy)
    with open(base_strategy) as fin:
        count = 0
        for line in fin:
            if line.startswith('#'):
                continue
            if count==1:
                # on the second (index == 1) non-commented line we have the
                # number of existing tactics
                return int(line.split()[0])
            count+=1

# DATA START
running_dir = "/home/joao/autorun/"
#serverconf = "server_fast.conf"
#serverconf = "server_official_notautostart.conf"
serverconf = "server_official.conf"
logfile = "runner.log"
matchesdir = "matches/"
teamsdir = "teams/"
matchhost = "127.0.0.1"
statistics_version="1.03"

# fcportugal_dynamic
# strategy file to generate/modify/use
base_strategy="generated_base_strategy.conf"
_base_strategy_path=os.path.join("confs/",base_strategy) # h8 this line.
strategy_folder = "strategies/"
strategy_default={"tactic": 1}
strategy_data = {
        "tactic": range(1,_number_of_tactics(_base_strategy_path)+1)
        }

# decision_tree
decision_trees=["bagging", "randomforest", "svm"]
window_sizes=[500, 1000,2000]

#match generation
opponents=["wrighteagle", "bahia2d", "nemesis"]
min_matches=10
# typical duration of a match (used for time prediction)
duration=datetime.timedelta(minutes=12)
size=9.5*(1024**2)#9.5MiB

# scripts...
scripts_dir=os.getcwd()
serverbin = "/usr/local/bin/rcssserver"
rcgconvert = "/usr/local/bin/rcgconvert"
# DATA END

# metadata start
conf_files=["serverconf", "base_strategy"]
files = ["serverconf", "base_strategy"]
to_write = ["logfile"]
dirs = ["matchesdir", "teamsdir", "strategy_folder"]

s_dirs = []
s_files = ["serverbin", "rcgconvert"]

special_dirs = ["running_dir", "scripts_dir"]
other = ["matchhost"]
# metadata end

class ConfigurationError(Exception):
    def __init__(self, msg='Unspecified'):
        Exception.__init__(self, msg)

import functools

# note1: os.path.join("/lulz","bananas") -> "/lulz/bananas"
# note2: os.path.join("/lulz","/bananas") -> "/bananas"

def config():
    """
    validate the current configuration data
    and rebuild some variables when necessary

    """
    import shutil
    # copy confs to running dir
    for conf in conf_files:
        value=globals()[conf]
        src=os.path.join(scripts_dir,"confs/",value)
        shutil.copy2(src,running_dir)

    for var in special_dirs:
        value = globals()[var]
        if not os.path.isdir(value):
            errmsg = "'{var}'({value}) is not a dir".format(**locals())
            logging.error(errmsg)
            raise ConfigurationError(errmsg)
        # transform it to absolute path
        globals()[var]=os.path.abspath(value)

    # transform paths into absolute paths
    fixp = functools.partial(os.path.join,running_dir)
    for var in files+dirs+to_write:
        globals()[var] = fixp(globals()[var])

    # transform paths into absolute paths (scripts only)
    s_fixp = functools.partial(os.path.join,scripts_dir)
    for var in s_dirs+s_files:
        globals()[var] = s_fixp(globals()[var])

    # check if the files paths refer to files
    for var in files+s_files:
        value = globals()[var]
        if not os.path.isfile(value):
            errmsg = "'{var}'({value}) is not a file".format(**locals())
            logging.error(errmsg)
            raise ConfigurationError(errmsg)

    # check if can write in the files
    for var in to_write:
        value = globals()[var]
        try:
            f = open(value,"a")
            f.close()
        except(IOError):
            errmsg = "'{var}'({value}) cannot be written to".format(**locals())
            raise ConfigurationError(errmsg)

    # check if the dirs paths refer to dirs
    for var in dirs+s_dirs:
        value = globals()[var]
        if not os.path.isdir(value):
            errmsg = "'{var}'({value}) is not a dir".format(**locals())
            logging.error(errmsg)
            raise ConfigurationError(errmsg)

    # configure log file
    FORMAT = "%(asctime)s %(levelname)s: %(message)s"
    LOG_FILENAME = logfile
    logging.basicConfig(filename=LOG_FILENAME,level=logging.DEBUG,format=FORMAT)
    logging.Formatter("%(asctime)s - %(levelname)s:%(message)s")


def validate_strategy(data):
    for param_name, value in data.items():
        if value not in strategy_data[param_name]:
            return False
    return True

def printvars():
    for var in special_dirs+files+s_files+dirs+s_dirs+to_write+other:
        print(var,"=",globals()[var])

# when imported validate and build configuration
config()
