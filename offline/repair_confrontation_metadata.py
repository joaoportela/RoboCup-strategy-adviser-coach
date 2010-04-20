#! /usr/bin/env python

"""class to (re)generate the confrontation metadata from the match in the
confrontation folder
"""

import os
import sys
from os.path import join
from contextlib import nested
import functools
import json

from merger import theid

def usage():
    return "USAGE:\npython {0} <MATCHES_DIR>".format(sys.argv[0])

def each_firstmatch_in_confrontation(confrontation_dir):
    join=functools.partial(os.path.join, confrontation_dir)
    confrontations=map(join, os.listdir(confrontation_dir))

    # just iterate over directories
    confrontations=filter(os.path.isdir, confrontations)
    for confrontation_folder in confrontations:
        for file_ in os.listdir(confrontation_folder):
            if file_.endswith(".json") and theid(file_):
                file_ = os.path.join(confrontation_folder,file_)
                if os.path.isfile(file_):
                    yield file_
                    # just return the first match to show up in each dir.
                    break


def match_to_confrontation(match_metadata):
    """transform match metada into confrontation metadata"""
    return {'team_a': match_metadata['team_l'],
            'team_b':match_metadata['team_r']}

def repair(dir_):
    for match_metadata_f in each_firstmatch_in_confrontation(dir_):
        confrontation_dir=os.path.dirname(match_metadata_f)
        confrontation_metadata_f=join(confrontation_dir, "confrontation_metadata.json")
        with nested(open(match_metadata_f),open(confrontation_metadata_f,'w')) as (fin, fout):
            match_metadata=json.load(fin)
            confrontation_metadata=match_to_confrontation(match_metadata)
            json.dump(confrontation_metadata, fout, sort_keys=True, indent=4)
            print fin.name, "->", fout.name
            print match_metadata,"->", confrontation_metadata

if __name__ == '__main__':
    if len(sys.argv) > 1 and os.path.isdir(sys.argv[1]):
        target_dir=sys.argv[1]

        dname=(target_dir.rstrip("/")).rpartition("/")[-1]
        if dname != "matches":
            errmsg="matches dir {0} is probably invalid".format(target_dir)
            raise Exception(errmsg)

        print "repairing", target_dir
        repair(target_dir)
    else:
        print usage()
        # print "running doctest"
        import doctest
        doctest.testmod()
