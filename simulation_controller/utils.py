#! /usr/bin/env python

__all__ = ["all_equal", "runcommand", "fake_runcommand", "write_script",
        "allrcgs", "allmetadata", "allmatchesids", "theid",
        "confrontation_name", "same_team", "str2bool", "human_size", "average",
        "confrontation_metadata_files", "walk_xmls", "discoverteamconfig"]

import logging
import time
import os
import stat
import glob
import re
import config

def walk_xmls(dir_):
    for root, dirs, files in os.walk(dir_):
        for f in files:
            if f.endswith(".xml"):
                yield os.path.join(root,f)

def discoverteamconfig(xml, side):
    dbgmsg="discovering config for {0} team in {1}".format(side,xml)
    logging.debug(dbgmsg)
    if side not in ["left","right"]:
        raise Exception("unkown side argument {0}".format(side))
    if side == "left":
        side = "team_l"
    elif side == "right":
        side = "team_r"

    dirname=os.path.dirname(xml)
    id_=theid(xml)
    metadata=os.path.join(dirname,id_+"_metadata.json")
    with open(metadata) as f:
        data=json.load(f)

    assert data['id'] == id_
    class_ = data[side][0]
    params = data[side][1]

    return params

def confrontation_metadata_files(dir_=config.matchesdir):
    for root, dirs, files in os.walk(dir_):
        for file_ in files:
            if file_ == "confrontation_metadata.json":
                yield os.path.join(root,file_)

def average(l):
    """returns the average of the elements of the list l."""
    sum_ = sum(l,0.0)
    return sum_/len(l)

def str2bool(s):
    if s.strip().lower() in ["true","t","1"]:
        return True
    if s.strip().lower() in ["false","f","0"]:
        return False
    raise ValueError("cannot determine if {0} is true or false".format(s))

def all_equal(l):
    if l:
        il=iter(l)
        first = il.next()
        for e in il:
            if e != first:
                return False
    return True

def same_team(nameA,nameB):
    """method to check if the two team names (nameA and nameB)
    can refer to the same team

    this method is based only on experience and may fail."""

    for i in range(2):
        if nameA.lower().startswith(nameB.lower()):
            return True
        # known special cases:
        if nameA == "lishang" and nameB == "HfutEngine2D":
            return True

        # switch and try again...
        nameA,nameB=nameB,nameA

    return False

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

def theid(name):
    """from the filename (basename) get the numbers in the beggining (the
    date)"""
    NUMBERS_PATTERN = re.compile(r'\d+')
    match = NUMBERS_PATTERN.match(os.path.basename(name))
    if match and len(match.group(0)) == 12:
        return match.group(0)
    return None

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

def allstatisticsxml(confrontationdir):
    """search for all the statistics xml files in "confrontationdir" directory
    """

    logging.info("searching all statistics xml files in {confrontationdir}".format(**locals()))

    # the matches files are the games logs (end in .rcg.gz)
    possible_files=glob.glob(os.path.join(confrontationdir,"*_convert.rcg.gz.xml"))

    return sorted(possible_files, key=theid)

def allmetadata(confrontationdir):
    """search for all the metadata files in "confrontationdir" directory"""

    logging.info("searching all metadata files in {confrontationdir}".format(**locals()))

    # the metadata files are json files
    possible_files = glob.glob(os.path.join(confrontationdir,"*_metadata.json"))

    return sorted(possible_files, key=theid)

def allmatchesids(confrontationdir):
    """search for all the matches ids in "confrontationdir" directory (from the
    rcg files...)"""

    possible_rcgs = allrcgs(confrontationdir)
    possible_metadata = allmetadata(confrontationdir)

    rcg_ids = map(theid, possible_rcgs)
    meta_ids = map(theid, possible_metadata)

    # only return if the metadata files also exist
    ids = filter(lambda x: x in meta_ids, rcg_ids)

    # check that all the ids have the correct length
    correct_len = 12
    assert all(map(lambda x: len(x) == correct_len, ids))

    return sorted(ids)

def matchfiles(confrontationdir,matchid):
    files={}

    # check if the file x has the id 'matchdid'
    matchingid=lambda x: theid(x) == matchid

    # get the all files names
    rcg=allrcgs(confrontationdir)
    statistics=allstatisticsxml(confrontationdir)
    metadata=allmetadata(confrontationdir)

    # filter by matching the match id
    rcg=filter(matchingid,rcg)
    statistics=filter(matchingid,statistics)
    metadata=filter(matchingid,metadata)

    # the number of results can only be 0 or 1
    for var in [rcg,statistics,metadata]:
        assert len(var) < 2

    if rcg:
        files['rcg'] = rcg[0]
    if statistics:
        files['statistics'] = statistics[0]
    if metadata:
        files['metadata'] = metadata[0]

    return files

def confrontation_name(teamA,teamB):
    lower_str = lambda obj: str(obj).lower()
    teams = sorted((teamA,teamB), key=lower_str)
    name = "{0}__vs__{1}".format(teams[0],teams[1])
    return name

def human_size(size, a_kilobyte_is_1024_bytes=True):
    '''Convert a file size to human-readable form.

    Keyword arguments:
    size -- file size in bytes
    a_kilobyte_is_1024_bytes -- if True (default), use multiples of 1024
                                if False, use multiples of 1000

    Returns: string

    adapted from: http://diveintopython3.org/your-first-python-program.html

    Examples:
    >>> human_size(1024)
    '1.0 KiB'
    >>> human_size(1000,False)
    '1.0 KB'
    >>> human_size(1024**2)
    '1.0 MiB'
    >>> human_size(1000**2,False)
    '1.0 MB'
    '''
    SUFFIXES = {1000: ['KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
                1024: ['KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB']}

    if size < 0:
        raise ValueError('number must be non-negative')

    multiple = 1024 if a_kilobyte_is_1024_bytes else 1000
    for suffix in SUFFIXES[multiple]:
        size /= multiple
        if size < multiple:
            break

    return '{0:.1f} {1}'.format(size, suffix)

if __name__ == '__main__':
    import doctest
    doctest.testmod()

