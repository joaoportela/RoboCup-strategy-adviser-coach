#! /usr/bin/env python

__all__ = ["Confrontation"]

import config
from utils import *
from match import *

class ConfrontationError(Exception):
    def __init__(self, msg='Unspecified'):
        Exception.__init__(self, msg)


class Confrontation(object):
    def __init__(self, teamA, TeamB):
        lower_str = lambda obj: str(obj).lower()
        teams = tuple(sorted((teamA,teamB), key=lower_str))
        self.name = "{0}__vs__{1}".format(teams[0],teams[1])
        self.confrontationdir = os.path.join(config.matchesdir, self.name)
        self.teamA=teamA
        self.teamB=teamB
        # TODO
        # create the dir and whatever...
        if os.path.exists(self.confrontationdir):
            if os.path.isdir(self.confrontationdir):
                dbgmsg = "confrontation directory {0} already exists, no need to create".format(self.confrontationdir)
                logging.debug(dbgmsg)
            else:
                # path exists but is not a directory
                errmsg = "cannot create directory {0}".format(self.confrontationdir)
                logging.error(errmsg)
                raise ConfrontationError(errmsg)
        else:
            dbgmsg = "creating confrontation directory {0}".format(self.confrontationdir)
            logging.debug(dbgmsg)
            os.mkdir(self.confrontationdir)


    def allmatches(self):
        """ return all the matches for these two teams"""
        matchesids = allmatchesids(self.confrontationdir)
        newmatch = lambda mid: Match(matchid=mid, matchdir=self.confrontationdir)
        return [newmatch(mid) for mid in matchesids]

    def statistics(self):
        """list of the matches statistics"""
        return [Match.statistics(match) for match in self.allmatches()]

    def playnewmatch(self):
        """force the teams to play a new match"""
        teams=(self.teamA,self.teamB)
        random.shuffle(teams)
        Match(teams,self.confrontationdir)

