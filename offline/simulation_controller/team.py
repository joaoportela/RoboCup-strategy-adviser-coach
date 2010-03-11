#! /usr/bin/env python

import os
import functools

__all__ = ["Team", "FCPortugal"]

import logging
import config
from utils import runcommand

class TeamError(Exception):
    def __init__(self, msg='Unspecified'):
        Exception.__init__(self, msg)

# holds the team data for use in the match class...
class Team(object):
    def __init__(self, name):
        if name not in Team.all_teams():
            errmsg = "invalid team {0}".format(name)
            logging.error(errmsg)
            raise TeamError(errmsg)
        self.name = name
        self.teamdir = os.path.join(config.teamsdir,name)
        self.bonus_args = []
        logging.debug("assuming %s teamdir to be %s",self.name, self.teamdir)

    def command_start(self, matchdir):
        # the team start script
        team_start = config.team_start
        # the match host (usually 127.0.0.1)
        matchhost = config.matchhost

        # format bonus args
        #quote = lambda a: "\"" + str(a) + "\""
        #bonus = " ".join([quote(arg) for arg in self.bonus_args])
        bonus = " ".join([str(arg) for arg in self.bonus_args])

        # command format
        commf = "\"{team_start}\" \"{matchdir}\" "
        commf+= "\"{self.teamdir}\" \"{self.name}\" "
        commf+= "\"{matchhost}\" {bonus}"

        return commf.format(**locals())

    def command_stop(self):
        team_stop = config.team_stop
	return "\"{team_stop}\" \"{self.teamdir}\"".format(**locals())

    def start(self, matchdir):
        runcommand(self.command_start(matchdir))

    def stop(self):
        logging.info("stopping %s", self.name)
        runcommand(self.command_stop())

    def __str__(self):
        return self.name

    def __repr__(self):
        return "Team('{0}')".format(self.name)

    @staticmethod
    def all_teams():
        # functions
        join = functools.partial(os.path.join,config.teamsdir)
        has_start = lambda d: os.path.isfile(join(d,"start"))
        has_kill = lambda d: os.path.isfile(join(d,"kill"))
        # initial list
        teams = os.listdir(config.teamsdir)
        # filter
        return [team for team in teams if has_start(team) and has_kill(team)]

# remember: ./FCPortugalPlayer -file client.conf -file server.conf -host 127.0.0.1 -team_name LOL > rawr.out -strategy_file strategy.conf -formations_file /home/joao/robocup/runner_workdir/teams/fcportugalY/formations.conf && cat rawr.out
class FCPortugal(Team):
    def __init__(self, formation):
        Team.__init__(self, "fcportugal_dynamic")
        self.strategy_data={"formation":formation}
        config.validate_strategy(self.strategy_data)
        # generate the strategy file
        self._gen_strategy_file()
        # set the bonus args with the formation
        self.bonus_args = ["-strategy_file \"{0}\"".format(self.strategy_fname)]

    def _gen_strategy_file(self):
        # calculate_file_name
        dynamic_part = []
        for name, value in self.strategy_data.items():
            value = str(value)
            dynamic_part.append("{name}{value}".format(**locals()))
        dynamic_part = "__".join(dynamic_part)
        target_fname = "strategy__{0}.conf".format(dynamic_part)
        target_fname = os.path.join(config.strategy_folder, target_fname)
        if os.path.isfile(target_fname):
            warnmsg = "{0} already exists and will be overwritten".format(target_fname)
            logging.warning(warnmsg)

        # write new file with the data replaced
        with open(config.base_strategy,"r") as source:
            with open(target_fname, "w") as target:
                for line in source:
                    for name, value in self.strategy_data.items():
                        name = "${0}$".format(name)
                        value = str(value)
                        line = line.replace(name, value)
                    target.write(line)

        self.strategy_fname = target_fname

