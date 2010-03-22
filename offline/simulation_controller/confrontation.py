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

class ConfrontationError(Exception):
    def __init__(self, msg='Unspecified'):
        Exception.__init__(self, msg)

class StatisticsAgregator(object):
    """class that agregates statistics objects"""

    def __init__(self, statistics, team=None):
        self.statistics = statistics
        self._teams = statistics[0].teams
        self.team=team

    @property
    def teams(self):
        return self._teams

    @property
    def team(self):
        """gets or sets the team to be used as default in the statistics

        this is very strange because the expected name is the one from the
        statistics filename and not the same as used to instanciate the
        teams (will try to guess if not accurate)"""
        return self._team

    @team.setter
    def team(self, value):
        if value is not None and value not in self.teams:
            if value.lower().startswith(self.teams[0].lower()):
                value=self.teams[0]
            elif value.lower().startswith(self.teams[1].lower()):
                value=self.teams[1]
            else:
                raise ConfrontationError("Could not recognize the team %s"%(value,))
        # TODO -method to check that the teams are coherent (remember that
        # s0.teams[0] == s1.teams[1] and s0.team[1] == s1.teams[0] is valid)
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
            raise ConfrontationError("team must be set before calling these methods")

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

        """nevermindthis:
        # don't check the statistics objects for private or protected methods...
        if not name.startswith("_"):
            # when we don't know which attribute is, try to return whatever the
            # statistics objects would return but check that the result is the same
            if "statistics" in self.__dict__ and all([hasattr(s,name) for s in self.statistics]):
                res=[getattr(s, name) for s in self.statistics]
                assert all_equal(res), "Statistics objects are not consistent %s" % (res,)
                return res[0]
        """

        raise AttributeError("%r object has no attribute %r" % (type(self).__name__, name))

    def __setattr__(self,name,value):
        # note: the "statistics" atribute is very special...

        """nevermindthis:
        # don't check the statistics objects for private or protected methods
        # or for the statistics atribute
        if not (name.startswith("_") or name == "statistics"):
            # only set existing attributes new atributes are for the default
            # behaviour
            if all([hasattr(s,name) for s in self.statistics]):
                res = [setattr(s, name, value) for s in self.statistics]
                # the return value should not be important, but ok!
                assert all_equal(res), "Statistics objects are not consistent %s" % (res,)
                return res[0]
        """

        # default behaviour
        object.__setattr__(self, name, value)



    # I really should really implement this correctly
    # naaa
    def __dir__(self):
        pass

class Confrontation(object):
    def __init__(self, teamA, teamB):
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
        matchesids = allmatchesids(self.confrontationdir)
        newmatch = lambda mid: Match(matchid=mid, matchdir=self.confrontationdir)
        return [newmatch(mid) for mid in matchesids]

    def statistics(self, team=None):
        """list of the matches statistics"""
        all_statistics = [match.statistics() for match in self.allmatches()]
        return StatisticsAgregator(all_statistics, team=team)

    def playnewmatch(self):
        """force the teams to play a new match"""
        teams=(self.teamA,self.teamB)
        #random.shuffle(teams)
        Match(teams=teams,matchdir=self.confrontationdir)

