#! /usr/bin/env python

__all__ = ["Match"]

import logging
import os
import xml.dom.minidom as minidom
import re
import json
import glob
import tarfile

import config
from team import Team
import statistics
from utils import *

MATCH_START_SCRIPT = """#!/bin/bash

# match directory
matchdir={matchdir}
#server binary file
serverbin={serverbin}
# configuration file
include={serverconf}

# teams start scripts
team_l_start={team_l_command}
team_r_start={team_r_command}

libpath="/usr/local/lib/"

cd ${{matchdir}}

# version that does not start the teams...
# ${{serverbin}} include="${{include}}" > server-output.log 2> server-error.log

# start the server and it starts the teams
LD_LIBRARY_PATH=${{libpath}} ${{serverbin}} include="${{include}}" server::team_l_start = "${{team_l_start}}" server::team_r_start = "${{team_r_start}}" > server-output.log 2> server-error.log

"""

class MatchError(Exception):
    def __init__(self, msg='Unspecified'):
        Exception.__init__(self, msg)

class Match(object):

    def __init__(self, teams=None, matchid=None, matchdir=None):
        """team_l, team_r - left and right team. each team should be of the Team class
        or a string with the name of the team.

        teams - if the teams are supplied, use them (is ignored when matchid is
        supplied)

        matchid - if the matchid is supplied fetch it from cache... ("last" can
        be supplied to fetch the last available match)

        matchdir - the directory to run the match

        """
        # do the initial checks..
        if teams is None and matchid is None:
            raise MatchError("either teams or matchid must be supplied.")
        if teams is None and matchdir is None:
            raise MatchError("no way to find out the matchdir")

        if teams is not None:
            team_l = teams[0]
            team_r = teams[1]
            # also allow strings with the name of the team
            if isinstance(team_l, basestring):
                team_l = Team(team_l)
            if isinstance(team_r, basestring):
                team_r = Team(team_r)
        else:
            team_l = None
            team_r = None

        # assign the variables
        self._id = matchid
        self._statistics = None
        self._result = None
        self.matchdir = matchdir
        self.team_l=team_l
        self.team_r=team_r

        if self.matchdir is None and teams is not None:
            # needs match name...
            self.name=confrontation_name(self.team_l,self.team_r)
            logging.warning("matchdir not provided to the match instance.")
            # the matchdir
            self.matchdir = os.path.join(config.matchesdir, self.name)
            if not os.path.isdir(self.matchdir):
                os.mkdir(self.matchdir)
                warnmsg="matchdir had to be created by match class. this should"
                warnmsg+="have been done by the confrontation class."
                logging.warning(warnmsg)

        if self._id is not None:
            # needs match dir...
            # load the relevant data...
            self._load_metadata()
            # self.team_* are now instantiated...
            # find out the name
            self.name=confrontation_name(self.team_l,self.team_r)

        self._play()

        # logging
        logging.info("Match object instanciated { teams: ('%s', '%s') }",
                self.team_l, self.team_r)

    def _play(self):

        # play the game
        if self._id is None:
            # call the very expensive method
            self._run()
            # dump metadata to match directory
            self._dump_metadata()

        # the id must be set...
        assert self._id is not None

        # convert the log to a version supported and calculate statistics to
        # the statistics.xml file
        stat = self.statistics()
        # get the match result from the statistics
        left_goals = stat.goals("left")
        right_goals = stat.goals("right")

	left_goals = (str(self.team_l), str(left_goals))
	right_goals = (str(self.team_r), str(right_goals))
        self._result = (left_goals, right_goals)

        return self.result()

    def _run(self):
        if os.path.exists(self.matchdir):
            if not os.path.isdir(self.matchdir):
                # path exists but is not a directory
                errmsg = "cannot create directory {0}".format(self.matchdir)
                logging.error(errmsg)
                raise MatchError(errmsg)
        else:
            warnmsg = "match directory {0} did not exist, shouldn't the confrontation"
            warnmsg+= " class create this?".format(self.matchdir)
            logging.warning(warnmsg)
            os.mkdir(self.matchdir)

        allids = allmatchesids(self.matchdir)

        # match directory
        matchdir = self.matchdir
        # server binary file
        serverbin = config.serverbin
        # default configs file
        serverconf = config.serverconf
        # left team
        team_l_command=self.team_l.command_start(self.matchdir)
        # right team
        team_r_command=self.team_r.command_start(self.matchdir)

        command = os.path.join(self.matchdir,"start_match.sh")
        content = MATCH_START_SCRIPT.format(**locals())
        write_script(command, content)

        logging.info("match start.")
        retcode = runcommand(command)
        logging.info("match end.")
        if retcode != 0:
            errmsg = "something went wrong while running the match.\n"
            errmsg+= "consider checking {0}".format(self.matchdir+"/server-error.log")
            logging.error(errmsg)

        # stop teams
        self.team_l.stop()
        self.team_r.stop()

        # calculate my id
        # the last rcg must be mine...
        lastrcg = allrcgs(self.matchdir)[-1]
        self._id=theid(lastrcg)

        # the id must be new...
        assert self._id not in allids


        #-check that the match runned fine
        (team_l,team_r) = self.teamnames_from_rcg(self.rcg())
        if team_l.lower() == "null" or team_r.lower() == "null":
            err="the match file ({0}) indicates that the match did not run successfully"
            raise MatchError(err.format(self.rcg()))

        # check that the match teams are the expected ones. this logs an error
        # but does not stop execution because it can be an error with the
        # same_team function
        if not same_team(team_l,self.team_l.name):
            err="{0} is not the same as {1}"
            logging.error(err.format(team_l,self.team_l.name))
        if not same_team(team_r,self.team_r.name):
            err="{0} is not the same as {1}"
            logging.error(err.format(team_r,self.team_r.name))

        # call the statistics method to force the files to be generated
        self.statistics()

        # archive and delete the logs and other outputs
        self._archive_outputs()

    def result(self):
        return self._result

    def _metadata_fname(self):
        basename = str(self._id) + "_metadata.json"
        fname = os.path.join(self.matchdir, basename)
        return fname

    def _dump_metadata(self):
        data={}
        data['id'] = self._id
        data['team_l'] = self.team_l.encode()
        data['team_r'] = self.team_r.encode()

        fname = self._metadata_fname()
        with open(fname, "w") as f:
            json.dump(data, f, sort_keys=True, indent=4)

    def _load_metadata(self):
        fname = self._metadata_fname()
        with open(fname) as f:
            data=json.load(f)

        # TODO temporary fix...
        # assert data['id'] == self._id, "metadata id is {0} but should be {1}".format(data['id'], self._id)
        self.team_l = Team.decode(data['team_l'])
        self.team_r = Team.decode(data['team_r'])

    def statistics(self):
        if self._statistics is not None:
            return self._statistics

        self._statistics = statistics.create_from_rcg(self.rcg(), teams=self.teamnames)

        return self._statistics

    def rcg(self):
        """return my rcg file name"""
        fnamepart="{0}*.rcg.gz".format(self._id)
        possible_files = glob.glob(os.path.join(self.matchdir,fnamepart))

        # do not include the converted ones...
        dont_include_convert = lambda fname: not fname.endswith("_convert.rcg.gz")
        possible_files = filter(dont_include_convert, possible_files)

        # matchid thingy is unique
        assert len(possible_files) == 1
        return possible_files[0]

    def _archive_outputs(self):
        # tar outputs (for future checking if necessary)
        filestotar=[]
        filestotar+=glob.glob(os.path.join(self.matchdir,"*-output.log"))
        filestotar+=glob.glob(os.path.join(self.matchdir,"*-error.log"))
        filestotar+=glob.glob(os.path.join(self.matchdir,"out.csv"))
        logging.debug("archiving {0}.".format(filestotar))

        assert self._id is not None

        tarname="{0}_archive.tar.gz".format(self._id)
        tarname=os.path.join(self.matchdir,tarname)
        tar = tarfile.open(tarname,'w:gz')
        try:
            for name in filestotar:
                tar.add(name)
        finally:
            tar.close()

        # files are safely stored in tar file..  remove them.
        for f in filestotar:
            os.remove(f)

    @property
    def teamnames(self):
        return (self.team_l.name, self.team_r.name)

    def __str__(self):
        res = self.result()
        return "{res[0][0]} ({res[0][1]}) vs {res[1][0]} ({res[1][1]})".format(**locals())
#
#    def __repr__(self):
#        pass

    @staticmethod
    def teamnames_from_rcg(rcg):
        """find the teams names from the rcg name"""
        #(re)use the statistics method that does just this...
        return statistics.Statistics.teamnames_from_xml(rcg+".xml")

