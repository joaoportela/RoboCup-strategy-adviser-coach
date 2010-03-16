#! /usr/bin/env python

__all__ = ["Match"]

import logging
import os
import glob

import config
from team import Team
import statistics
from utils import *
import xml.dom.minidom as minidom

# TODO: call match.sh instead of calling directly so that there is no need to
# put the redirects here.

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

match_start_script = """#!/bin/bash

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

    def __init__(self, team_l, team_r):
        """team_l, team_r - left and right team. each team should be of the Team class

        """
        # also allow strings with the name of the team
        if isinstance(team_l, basestring):
            team_l = Team(team_l)
        if isinstance(team_r, basestring):
            team_r = Team(team_r)

        # assign the variables
	self.use_cache = False
        self._statistics = None
        self._result = None
        self.team_l = team_l
        self.team_r = team_r

        # sort the teams names
        lower_str = lambda obj: str(obj).lower()
        self.teams = tuple(sorted((team_l,team_r), key=lower_str))
        self.name = "{0}__vs__{1}".format(self.teams[0],self.teams[1])
        self.matchdir = os.path.join(config.matchesdir, self.name)

        # logging
        logging.info("Match object instanciated { teams: ('%s', '%s') }",
                team_l, team_r)
        logging.debug("assuming match dir to be %s", self.matchdir)

    def play(self):
        if self.in_cache() && self.use_cache:
            self.load_result_from_cache()
            return self.result()

        # play the game
        self._play_raw()

        # convert the log to a version supported and calculate statistics to
        # the statistics.xml file
        self.statistics()

        # calculate game result from whatever
        dom = minidom.parse(self._statistics)
        left_goals = statistics.goals("left",dom)[0][1]
        right_goals = statistics.goals("right",dom)[0][1]

	left_goals = tuple(str(self.team_l), str(left_goals))
	right_goals = tuple(str(self.team_r), str(right_goals))
        self._result = (left_goals, right_goals)

        # write metadata to match directory
        self._write_metadata()

        return self.result()

    def in_cache(self):
        # check the cache and return accordingly
        return False

    def _play_raw(self):
        if os.path.exists(self.matchdir):
            if os.path.isdir(self.matchdir):
                warnmsg = "reusing match directory {0}".format(self.matchdir)
                logging.warning(warnmsg)
            else:
                # path exists but is not a directory
                errmsg = "cannot create directory {0}".format(self.matchdir)
                logging.error(errmsg)
                raise MatchError(errmsg)
        else:
            os.mkdir(self.matchdir)

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
        content = match_start_script.format(**locals())
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

    def result(self):
        return self._result

    def _write_metadata(self):
        pass

    def statistics(self):
        if self._statistics is not None:
            return self._statistics

        # pattern = re.compile(r'\d+')
        # lekey = lambda name: pattern.match(name).group(0)

        # use the most recent rcg
        # find which file to use
        possible_files = glob.glob(os.path.join(self.matchdir,"*.rcg.gz"))
        rcg = max(possible_files, key=os.path.basename)
        xml = statistics.calculate(rcg)
        self._statistics = xml
        return xml

