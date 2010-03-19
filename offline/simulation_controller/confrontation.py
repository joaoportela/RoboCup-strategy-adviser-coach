#! /usr/bin/env python

__all__ = ["Confrontation"]

import config
from utils import *
from match import *

class Confrontation(object):
    def __init__(self, teamA, TeamB):
        self.confrontationdir = None
        # TODO calculate confrontation dir...
        # create the dir and whatever...

    def allmatches(self):
        """ return all the matches for these two teams"""
        matchesids = allmatchesids(self.confrontationdir)
        newmatch = lambda mid: Match(teamA, teamB, mid)
        return [newmatch(mid) for mid in matchesids]

    def statistics(self):
        """list of the matches statistics"""
        return [Match.statistics(match) for match in self.allmatches()]

    def playnewmatch(self):
        """force the teams to play a new match"""
        pass


