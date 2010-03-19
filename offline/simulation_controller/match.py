#! /usr/bin/env python

__all__ = ["Match"]

import logging
import os
import xml.dom.minidom as minidom
import re

import config
from team import Team
import statistics
from utils import *

#match.rb, resumo:
# def initialize team_l, team_r, results_csv, tournament_log_dir, config
#     @team_l = team_l
#     @team_l_config = YAML::load_file File.join(team_l, "team.yml")
#     @team_r = team_r
#     @team_r_config = YAML::load_file File.join(team_r, "team.yml")
#     @results = results_csv
#     @log_dir = Match.current_match_dir tournament_log_dir
#     @config = config
# end
# def start
#     setup
#     make_start_scripts
#     start_rcssserver
#     stop_teams
#     write_configuration File.join(@log_dir, "match.yml")
#     save_results
#     convert_results if @config.robocup2flash
#     save_logging "l", @team_l if @config.save_logging
#     save_logging "r", @team_r if @config.save_logging
#     statistics if @config.statistics
#     cleanup
#     Match.increment_match_index
# end

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

cd ${{matchdir}}

# version that does not start the teams...
# ${{serverbin}} include="${{include}}" > server-output.log 2> server-error.log

# start the server and it starts the teams
${{serverbin}} include="${{include}}" server::team_l_start = "${{team_l_start}}" server::team_r_start = "${{team_r_start}}" > server-output.log 2> server-error.log

"""

class MatchError(Exception):
    def __init__(self, msg='Unspecified'):
        Exception.__init__(self, msg)

class Match(object):

    def __init__(self, team_l, team_r, matchid=None):
        """team_l, team_r - left and right team. each team should be of the Team class
        or a string with the name of the team.

        matchid - if the matchid is supplied fetch it from cache... ("last" can
        be supplied to fetch the last available match)

        """
        # also allow strings with the name of the team
        if isinstance(team_l, basestring):
            team_l = Team(team_l)
        if isinstance(team_r, basestring):
            team_r = Team(team_r)

        # assign the variables
        self._statistics = None
        self._result = None
        self._id = matchid
        self.team_l = team_l
        self.team_r = team_r

        # sort the teams names
        lower_str = lambda obj: str(obj).lower()
        self.teams = tuple(sorted((team_l,team_r), key=lower_str))
        self.name = "{0}__vs__{1}".format(self.teams[0],self.teams[1])
        ## TODO - matchdir should be provided by the confrontation class??
        self.matchdir = os.path.join(config.matchesdir, self.name)

        self._play()

        # logging
        logging.info("Match object instanciated { teams: ('%s', '%s') }",
                team_l, team_r)
        logging.debug("assuming match dir to be %s", self.matchdir)

    def _play(self):

        # play the game
        if self._id is None:
            # call the very expensive method
            self._run()

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

        # write metadata to match directory
        self._write_metadata()

        return self.result()

    def _run(self):
        # TODO put this in the confrontation module
        if os.path.exists(self.matchdir):
            if os.path.isdir(self.matchdir):
                dbgmsg = "match directory {0} already exists, no need to create".format(self.matchdir)
                logging.warning(dbgmsg)
            else:
                # path exists but is not a directory
                errmsg = "cannot create directory {0}".format(self.matchdir)
                logging.error(errmsg)
                raise MatchError(errmsg)
        else:
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

        # TODO
        # check that the match runned fine
        # (none of that null in the name)

    def result(self):
        return self._result

    def _write_metadata(self):
        pass

    def statistics(self):
        if self._statistics is not None:
            return self._statistics

        self._statistics = statistics.create_from_rcg(self.rcg())

        return self._statistics

    def rcg():
        """return my rcg name"""
        fname_part="{0}*.rcg.gz".format(self._id)
        possible_files = glob.glob(os.path.join(self.matchdir,fnamepart))
        # matchid thingy is unique
        assert len(possible_files) == 1
        return possible_files[0]

    @staticmethod
    def teamnames(rcg):
        """find the teams names from the rcg name"""
        #(re)use the statistics method that does just this...
        statistics.Statistics.teamnames(rcg+".xml")

