#! /usr/bin/env python

import os
import functools

__all__ = ["Team", "FCPortugal"]

import logging
import config
from utils import *

class TeamError(Exception):
    def __init__(self, msg='Unspecified'):
        Exception.__init__(self, msg)

team_start_script = """#! /bin/bash

matchdir=\"{matchdir}\"
teamdir=\"{teamdir}\"
name=\"{teamname}\"
matchhost=\"{matchhost}\"

# bonus args:
{other}

cd $matchdir
teamComm="${{teamdir}}/start"
output="${{name}}-output.log"
outerror="${{name}}-error.log"

#echo "--$name" >> le_argv.txt
#python /home/joao/RoboCup-strategy-adviser-coach/offline/simulation_controller/print_argv.py $teamComm $matchhost $teamdir "$@" >> $matchdir/le_argv.txt
#echo "--" >> le_argv.txt

sleep 1
echo command: \"${{teamComm}}\" \"${{matchhost}}\" \"${{teamdir}}\" {other_as_arg} > ${{output}} 2> ${{outerror}}
\"${{teamComm}}\" \"${{matchhost}}\" \"${{teamdir}}\" {other_as_arg} > ${{output}} 2> ${{outerror}}

"""

team_stop_script = """#! /bin/bash

matchdir=\"{matchdir}\"
teamdir=\"{teamdir}\"
name=\"{teamname}\"

cd ${{matchdir}}
teamComm="${{teamdir}}/kill"
output="${{name}}-output.log"
outerror="${{name}}-error.log"

$teamComm >> ${{output}} 2>> ${{outerror}}
"""
# holds the team data for use in the match class...
class Team(object):
    def __init__(self, name):
        if name not in Team.all_teams():
            errmsg = "invalid team {0}".format(name)
            logging.error(errmsg)
            raise TeamError(errmsg)
        self.name = name
        self.teamdir=os.path.join(config.teamsdir,name)
        self.matchdir=None
        # bonus args is to be filled in the format ("varname", "varvalue")
        self.bonus_args = []
        logging.debug("assuming %s teamdir to be %s",self.name, self.teamdir)

    def command_start(self, matchdir):
        """ build the start script and return the command
        to start it """
        self.matchdir=matchdir
        # variables required: matchdir, teamdir, teamname, matchhost,
        # other, other_as_arg
        teamdir = self.teamdir
        teamname = self.name
        # the match host (usually 127.0.0.1)
        matchhost = config.matchhost

        # build it in the format 'varname=varvalue'
        var_assign = lambda n,v: str(n) + "=\"" + str(value)+"\""
        other="\n".join([var_assign(name,value) for name, value in self.bonus_args])

        # build it in the format '$varname'
        var_value = lambda n: "\"${"+str(n)+"}\""
        other_as_arg=" ".join([var_value(n) for n,v in self.bonus_args])

        script_name = "start_" + teamname + ".sh"
        script_name = os.path.join(matchdir,script_name)
        content = team_start_script.format(**locals())
        write_script(script_name, content)

        # return the command to run it.
        return script_name

    def command_stop(self):
        matchdir=self.matchdir
        teamdir=self.teamdir
        teamname=self.name

        script_name = "stop_" + teamname + ".sh"
        script_name = os.path.join(matchdir,script_name)
        content = team_stop_script.format(**locals())
        write_script(script_name, content)

        self.matchdir=None
	return script_name

    def start(self, matchdir):
        logging.info("starting %s", self.name)
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

# remember: ./FCPortugalPlayer -file client.conf -file server.conf -host
# 127.0.0.1 -team_name LOL -strategy_file strategy.conf -formations_file
# /home/joao/robocup/runner_workdir/teams/fcportugalY/formations.conf 

class FCPortugal(Team):
    def __init__(self, strategy_params):
        # changed while trying to track a problem
        Team.__init__(self, "fcportugalD")
        self.strategy_params=strategy_params
        config.validate_strategy(self.strategy_params)
        # generate the strategy file
        strategy_fname = self._gen_strategy_file()
        # pass the generated strategy file as argument to the team
        self.bonus_args.append(("strategy_file", strategy_fname))

    def _gen_strategy_file(self):
        # calculate_file_name
        dynamic_part = self.params_summary()
        target_fname = "{self.name}__{dynamic_part}.conf".format(**locals())
        target_fname = os.path.join(config.strategy_folder, target_fname)
        if os.path.isfile(target_fname):
            warnmsg = "{0} already exists and will be overwritten".format(target_fname)
            logging.warning(warnmsg)

        # write new file with the data replaced
        with open(config.base_strategy,"r") as source:
            with open(target_fname, "w") as target:
                for line in source:
                    for name, value in self.strategy_params.items():
                        name = "${0}$".format(name)
                        value = str(value)
                        line = line.replace(name, value)
                    target.write(line)

        self.strategy_fname = target_fname
        return self.strategy_fname

    def params_summary(self):
        dynamic_part = []
        for name, value in self.strategy_params.items():
            value = str(value)
            dynamic_part.append("{name}{value}".format(**locals()))
        return "_".join(dynamic_part)

    def __str__(self):
        return self.name+"-"+self.params_summary()

