#! /usr/bin/env python

__all__ = ["Confrontation"]

import config
import os
import logging
import random
import functools

from utils import *
from match import *
from statistics import *

class StatisticsAgregatorError(Exception):
    def __init__(self, msg='Unspecified'):
        Exception.__init__(self, msg)

class ConfrontationError(Exception):
    def __init__(self, msg='Unspecified'):
        Exception.__init__(self, msg)

class StatisticsAgregator(object):
    """class that agregates statistics objects"""

    def __init__(self, statistics, team=None):
        """ constructor for the StatisticsAgregator object.

        statistics - the statistics list to agregate.
        teams - if provided should be a tuple containing the teams names
        team - if provided should be the name of the team selected by default
        """
        self.statistics = statistics
        self._validate_teams_in_statistics()
        self._teams=statistics[0].teams
        self.team=team

    @property
    def teams(self):
        return self._teams

    def _validate_teams_in_statistics():
        std=self.statistics[0].teams
        for s in self.statistics:
            if (s.teams[0],s.teams[1]) == (std[0], std[1]):
                continue
            if (s.teams[1],s.teams[0]) == (std[0], std[1]):
                continue
            errmsg="Teams {0} are not valid according to the standard {1}".format(s.teams,std)
            raise StatisticsAgregatorError(errmsg)

    @property
    def team(self):
        """gets or sets the team to be used as default in the statistics """
        return self._team

    @team.setter
    def team(self, value):
        if value is not None and value not in self.teams:
            if same_team(value,self.teams[0]):
                value=self.teams[0]
            if same_team(value,self.teams[1]):
                value=self.teams[1]
            else:
                raise StatisticsAgregatorError("Could not recognize the team %s"%(value,))
        for s in self.statistics:
            s.team=value
        self._team = value

    def _average(self, func, team=None, *args,**kwargs):
        #backup
        backup=self.team

        if team is not None:
            self.team=team

        # the team must be set by now...
        if self.team is None:
            raise StatisticsAgregatorError("team must be set before calling these methods")

        results = [func(s,*args,**kwargs) for s in self.statistics]
        sum_ = sum(results,0.0)
        average = sum_/len(results)

        #restore
        self.team=backup
        return average

    FUNCS_TO_AVERAGE=["passes", "passmisses", "passchains", "wingchanges", "goals",
            "goalmisses", "goalssuffered", "goalopportunities",
            "favouritezones", "zonedominance", "attacks"]
    def __getattr__(self,name):
        if name in StatisticsAgregator.FUNCS_TO_AVERAGE:
            meth = getattr(Statistics, name)
            return functools.partial(self._average, meth)

        raise AttributeError("%r object has no attribute %r" % (type(self).__name__, name))

    # I really should really implement this correctly
    # naaa...
    # def __dir__(self):
    #     pass

class Confrontation(object):
    def __init__(self, teamA, teamB):
        self._allmatches=None
        self._nmatches=None
        self.name = confrontation_name(teamA,teamB)
        self.confrontationdir = os.path.join(config.matchesdir, self.name)
        self.teamA=teamA
        self.teamB=teamB
        # create the dir
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
        # if not already cached...
        if self._allmatches is None:
            matchesids = allmatchesids(self.confrontationdir)
            newmatch = lambda mid: Match(matchid=mid, matchdir=self.confrontationdir)
            self._allmatches=[newmatch(mid) for mid in matchesids]

        return self._allmatches

    def statistics(self, team=None):
        """list of the matches statistics"""
        all_statistics = [match.statistics() for match in self.allmatches()]
        return StatisticsAgregator(all_statistics, team=team)

    @property
    def teamnames(self):
        return (teamA.name,teamB.name)

    def playnewmatch(self):
        """force the teams to play a new match"""
        teams=(self.teamA,self.teamB)
        #random.shuffle(teams)
        Match(teams=teams,matchdir=self.confrontationdir)

        # reset the allmatches cache because it is no longer valid
        self._allmatches=None

        # if i already have a number matches cache, increment it by one
        if self._nmatches is not None:
            self._nmatches+=1

    def __str__(self):
        return "{self.teamA} vs {self.teamB}".format(**locals())

    def __repr_(self):
        return "Confrontation({0}, {1})".format(repr(self.teamA),
                repr(self.teamB))

    def __len__(self):
        if self._nmatches is None:
            self._nmatches=len(self.allmatches())

        return self._nmatches

    def __nonzero__(self):
        """ nonzero always returns true.

        this is the expected behaviour for most classes. Had to be implemented
        because the len method would conflict with this (when len(obj) == 0,
        obj is considered False)
        """
        return True

