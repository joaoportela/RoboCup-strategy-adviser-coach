#! /usr/bin/env python

import logging
import re
import os
from utils import *
import config
import xml.dom.minidom as minidom
from functools import wraps

__all__ = ["Statistics", "create_from_rcg"]

STATISTICS_SCRIPT="""#! /bin/bash

scriptsdir="{scriptsdir}"
dirname="{dirname}"
rcgconvert="{rcgconvert}"
rcg="{rcg}"
rcgc="{rcgc}"
xml="{xml}"

libpath="/usr/local/lib/"

rcgout="${{dirname}}/rcgconvert-output.log"
rcgerr="${{dirname}}/rcgconvert-error.log"

cd ${{dirname}}

# convert to the right version
LD_LIBRARY_PATH=${{libpath}} "${{rcgconvert}}" -v 3 -o "${{rcgc}}" "${{rcg}}" > ${{rcgout}} 2> ${{rcgerr}}

jclass="soccerscope.SoccerScope"
sout="${{dirname}}/statistics-output.log"
serr="${{dirname}}/statistics-error.log"

# generate the statistics
MYCLASSPATH="${{scriptsdir}}/soccerscope.jar:${{scriptsdir}}/java-xmlbuilder-0.3.jar"
CLASSPATH="${{MYCLASSPATH}}" java ${{jclass}} --batch "${{rcgc}}" "${{xml}}" > ${{sout}} 2> ${{serr}}
"""

STATISTICS_SCRIPT_NO_CONVERT="""#! /bin/bash

scriptsdir="{scriptsdir}"
dirname="{dirname}"
rcg="{rcg}"
xml="{xml}"

cd ${{dirname}}

jclass="soccerscope.SoccerScope"
sout="${{dirname}}/statistics-output.log"
serr="${{dirname}}/statistics-error.log"

# generate the statistics
MYCLASSPATH="${{scriptsdir}}/soccerscope.jar:${{scriptsdir}}/java-xmlbuilder-0.3.jar"
CLASSPATH="${{MYCLASSPATH}}" java ${{jclass}} --batch "${{rcg}}" "${{xml}}" > ${{sout}} 2> ${{serr}}
"""

# convert the rcg to a supported version
def _converted_name(rcg):
    bname, ext = os.path.splitext(rcg)
    if ext == ".gz":
        bname, ext2 = os.path.splitext(bname)
        ext = ext2+ext
    rcgc = bname + "_convert" + ext
    return rcgc

def valid_version(xml):
    with open(xml,"r") as f:
        dom=minidom.parse(xml)
        version=dom.getElementsByTagName("analysis")[0].getAttribute("version")
        return version==config.statistics_version

def rcgtoxml(rcg,convert=False):
    scriptsdir=config.scripts_dir
    dirname=os.path.dirname(rcg)
    if convert:
        script=STATISTICS_SCRIPT
        rcgconvert=config.rcgconvert
        rcgc=_converted_name(rcg)
        xml=rcgc+".xml"
    else:
        script=STATISTICS_SCRIPT_NO_CONVERT
        xml=rcg+".xml"
    if os.path.isfile(xml):
        if valid_version(xml):
            dbg="statistics file for {0} already exists and is valid".format(rcg)
            logging.debug(dbg)
            return xml
        else:
            dbg="statistics file for {0} already exists but its version is wrong".format(rcg)
            logging.debug(dbg)

    script_name = "calculate_statistics.sh"
    script_name = os.path.join(dirname,script_name)
    content = script.format(**locals())
    write_script(script_name, content)
    dbg="creating statistics xml from {0}".format(rcg)
    logging.debug(dbg)
    runcommand(script_name)

    return xml


def create_from_rcg(rcg, *args,**kwds):
    xml=rcgtoxml(rcg,convert=True)
    return Statistics(xml=xml,*args,**kwds)

class StatisticsError(Exception):
    def __init__(self, msg='Unspecified'):
        Exception.__init__(self, msg)

def accept_side(fn):
    """ function decorator. functions with this decorator accept the side as
    the second argument. (looking back, maibe not a good idea, but i'm sticking
    with it)

    the side argument can either be "left", "right" or the team name.
    self.side will be set by this decorator for latter use by the function.
    """
    @wraps(fn)
    def wrapper(self, side=None, *args, **kwds):
        # validate side argument.
        if side not in self.valid_magic:
            raise StatisticsError("{0} is an invalid side".format(side))

        # backup the self.side variable
        backup = self.side

        # set the self.side variable
        if side is not None:
            self.magic_side=side

        # self.side should be valid by now...
        if self.side is None:
            raise StatisticsError("side not specified")

        # call the actual function
        rv=fn(self, *args, **kwds)

        # restore self.side
        self.side = backup

        # the return value of the function is important
        return rv

    # return the awesome function
    return wrapper

##
# statistics mega class
##

class Statistics(object):
    SIDES=["left","right"]

    def __init__(self, xml, side=None, teams=None):
        self.strange_zones=False
        if not os.path.exists(xml):
            # TODO maibe try to recover by finding the rcg
            raise StatisticsError('{0} not found'.format(xml))
        if not valid_version(xml):
            rcg, ext = os.path.splitext(xml)
            if os.path.isfile(rcg):
                warnmsg="xml version was not valid. recreating from {0}.".format(rcg)
                logging.warn(warnmsg)
                xml=rcgtoxml(rcg,convert=False)
            else:
                errmsg="invalid xml version. "
                errmsg+="rcg file to recreate was not found."
                raise StatisticsError(errmsg)
        self._xml=xml
        self._dom = minidom.parse(xml)
        if teams is None:
            self._teams = Statistics.teamnames_from_xml(self.xml)
            logging.warning("calculating team names from the xml file")
        else:
            self._teams = teams
            self._validate_teams()

        self.magic_side = side

        for team in self.teams:
            if team in Statistics.SIDES:
                warn = "there is a team named '{0}'. this is sh*t prone!".format(team)
                logging.warning(warn)

        # TODO - if the statistics analysis is of the wrong version, try to
        # recreate..

        dbg="statistics object instanciated. ( xml: '{0}' teams: '{1}', side: '{2}')"
        logging.debug(dbg.format(self.xml,self.teams, self.team))


    def _validate_teams(self):
        teams=Statistics.teamnames_from_xml(self.xml)
        valid=(same_team(self.teams[0], teams[0]) and
                same_team(self.teams[1],teams[1]))
        if not valid:
            errmsg = "teams {0} are not valid according to the xml file name"
            errmsg = "{1} {2}"
            raise StatisticsError(errmsg.format(self.teams,self.xml,teams))

    @property
    def dom(self):
        return self._dom

    @property
    def valid_magic(self):
        return Statistics.SIDES+list(self.teams)+[None]

    def magic_side(self,value):
        if value not in self.valid_magic:
            raise StatisticsError("side not specified")

        # guess if the value is the side or the team
        if value in Statistics.SIDES:
            self.side = value
        elif value in self.teams:
            self.team = value
        elif value is None:
            self.side = value

        assert self.side in Statistics.SIDES or self.side is None
        assert self.team in self.teams or self.team is None

    magic_side=property(fset=magic_side)

    @property
    def xml(self):
        """the xml file where the statistics are extracted"""
        return self._xml

    @property
    def teams(self):
        """the match teams (provided by constructor argument or extracted from the xml file name)"""
        return self._teams

    @property
    def team(self):
        """the default team when the team/side argument is not explicitly
        supplied to the methods."""
        assert self._team in self.teams or self._team is None
        return self._team

    @team.setter
    def team(self,value):
        if not(value in self.teams or value is None):
            raise StatisticsError("team {0} is invalid".format(value))

        self._team = value
        if value is not None:
            self._side=Statistics.SIDES[self.teams.index(value)]
        else:
            self._side=None

        assert self._team in self.teams or self._team is None

    @property
    def side(self):
        """ the default side when, in some methods, the side argument is
        ommited.
        """
        assert self._side in Statistics.SIDES or self._side is None
        return self._side

    @side.setter
    def side(self,value):
        if not(value in Statistics.SIDES or value is None):
            raise StatisticsError("side {0} is invalid".format(value))
        self._side = value
        if value is not None:
            self._team = self.teams[Statistics.SIDES.index(value)]
        else:
            self._team=None

        assert self._side in Statistics.SIDES or self._side is None

    @staticmethod
    def teamnames_from_xml(fname):
        """find the teams names from the xml file name
        """
        TEAMNAMESPATTERN = re.compile(r'^\d+-(.*)(?:_\d+)-vs-(.*)(?:_\d+)(?:_convert)?\.rcg\.gz\.xml$')
        bname = os.path.basename(fname)
        (t1, t2) = TEAMNAMESPATTERN.match(bname).groups()
        return (t1,t2)

    @property
    @accept_side
    def side_id(self):
        if self.side == "left":
            return "LEFT_SIDE"
        if self.side == "right":
            return "RIGHT_SIDE"
        return None

    @property
    def opponent_side(self):
        if self.side == "left":
            return "right"
        if self.side == "right":
            return "left"

    @property
    @accept_side
    def zones(self):
        if self.side == "left":
            return {
                    "leftwing_1stquarter": "TopLeftleft",
                    "leftwing_2ndquarter": "TopLeftright",
                    "middlewing_1stquarter": "MiddleLeftleft",
                    "middlewing_2ndquarter": "MiddleLeftright",
                    "rightwing_1stquarter": "BottomLeftleft",
                    "rightwing_2ndquarter": "BottomLeftright",
                    "leftwing_3rdquarter": "TopRightleft",
                    "leftwing_4thquarter": "TopRightright",
                    "middlewing_3rdquarter": "MiddleRightleft",
                    "middlewing_4thquarter": "MiddleRightright",
                    "rightwing_3rdquarter": "BottomRightleft",
                    "rightwing_4thquarter": "BottomRightright"
                    }
        elif side == "right":
            if self.strange_zones:
                # some evaluators have the zones defined
                # in a strange way. (but the left side is the same)
                return {
                        "leftwing_4thquarter": "TopLeftleft",
                        "leftwing_3rdquarter": "TopLeftright",
                        "middlewing_4thquarter": "MiddleLeftleft",
                        "middlewing_3rdquarter": "MiddleLeftright",
                        "rightwing_4thquarter": "BottomLeftleft",
                        "rightwing_3rdquarter": "BottomLeftright",
                        "leftwing_2ndquarter": "TopRightleft",
                        "leftwing_1stquarter": "TopRightright",
                        "middlewing_2ndquarter": "MiddleRightleft",
                        "middlewing_1stquarter": "MiddleRightright",
                        "rightwing_2ndquarter": "BottomRightleft",
                        "rightwing_1stquarter": "BottomRightright"
                        }
            else:
                return {
                        "rightwing_4thquarter": "TopLeftleft",
                        "rightwing_3rdquarter": "TopLeftright",
                        "middlewing_4thquarter": "MiddleLeftleft",
                        "middlewing_3rdquarter": "MiddleLeftright",
                        "leftwing_4thquarter": "BottomLeftleft",
                        "leftwing_3rdquarter": "BottomLeftright",
                        "rightwing_2ndquarter": "TopRightleft",
                        "rightwing_1stquarter": "TopRightright",
                        "middlewing_2ndquarter": "MiddleRightleft",
                        "middlewing_1stquarter": "MiddleRightright",
                        "leftwing_2ndquarter": "BottomRightleft",
                        "leftwing_1stquarter": "BottomRightright"
                        }

    @staticmethod
    def _timeofhalf(half=None):
        if half is None:
            # this is probably stupid, but ok.
            return (1, 6000)

        if half == 1 or half == "1st":
            return (1,3000)
        if half == 2 or half == "2nd":
            return (3001,6000)

        errmsg="unrecognized 'half' argument({0})"
        raise StatisticsError(errmsg.format(half))

    ###
    # statistics extraction starts here.
    ##
    @accept_side
    def passes(self, half=None, offensive=None, breakpass=None):
        """number of passes.

        the arguments are filteres, None does not filter.

        half - filter the number of passes by game half (1 or 2)
        offensive - filter just offensive passes (True), just not offensive
            passes (False).
        breakpass - (boolean) if the type of pass is breakpass or not.
        """
        # validate arguments...
        if offensive not in [True, False, None]:
            errmsg="unrecognized 'offensive' argument({0})"
            raise StatisticsError(errmsg.format(offensive))
        if breakpass not in [True, False, None]:
            errmsg="unrecognized 'breakpass' argument({0})"
            raise StatisticsError(errmsg.format(breakpass))

        # no special options. return all passes
        if half is None and offensive is None and breakpass is None:
            return int(self.dom.getElementsByTagName("passes")[0].getAttribute(self.side))

        starttime, endtime = Statistics._timeofhalf(half)

        passes_tag=self.dom.getElementsByTagName("passes")[0]
        passes_count=0
        for pass_ in passes_tag.getElementsByTagName("pass"):
            valid_half=False
            valid_offensive=False
            valid_breakpass=False

            # not my team... move along
            if pass_.getAttribute("team") != self.side_id:
                continue

            # of the desired half
            if half is None:
                valid_half=True
            else:
                kick_t = int(pass_.getElementsByTagName("kick")[0].getAttribute("time"))
                reception_t = pass_.getElementsByTagName("reception")
                # time window is valid
                if kick_t >= starttime and reception_t <= endtime:
                    valid_half=True

            # of the desired offensive/defensive type
            if offensive is None:
                valid_offensive=True
            else:
                off_pass=str2bool(pass_.getAttribute("offensive"))
                # is of the same type as the pass
                if offensive==off_pass:
                    valid_offensive=True

            # of the desired breakpass/not breakpass type
            if breakpass is None:
                valid_breakpass=True
            else:
                break_pass=str2bool(pass_.getAttribute("breakpass"))
                if breakpass == break_pass:
                    valid_breakpass=True

            # if all conditions are met
            if valid_half and valid_offensive and valid_breakpass:
                passes_count+=1

        return passes_count

    @accept_side
    def passmisses(self, half=None, offensive=None, receiver_offside=True):
        """ passes misses.
        the arguments are filters, None does not filter.

        half - filter the number of passes by game half (1 or 2)
        """
        # no filters set. return all passmisses
        if half is None and offensive is None and receiver_offside is None:
            return int(dom.getElementsByTagName("passmisses")[0].getAttribute(self.side))

        starttime, endtime = Statistics._timeofhalf(half)
        # validate filters
        if offensive not in [True, False, None]:
            errmsg="unrecognized 'offensive' argument({0})"
            raise StatisticsError(errmsg.format(offensive))
        if receiver_offside not in [True, False, None]:
            errmsg="unrecognized 'receiver_offside' argument({0})"
            raise StatisticsError(errmsg.format(receiver_offside))

        misses_tag=self.dom.getElementsByTagName("passmisses")[0]
        misses_count=0
        for miss in misses_tag.getElementsByTagName("passmiss"):

            # not my team... move along
            if miss.getAttribute("team") != self.side_id:
                continue

            if half is not None:
                # of the desired half
                kick_t = int(miss.getElementsByTagName("kick")[0].getAttribute("time"))
                if not(kick_t >= starttime and kick_t <= endtime):
                    # time window is not valid
                    continue

            if offensive is not None:
                off_pass=str2bool(miss.getAttribute("offensive"))
                if off_pass != offensive:
                    # is not of the desired offensive/defensive type
                    continue

            if receiver_offside is not None:
                rcv_off=str2bool(miss.getElementsByTagName("target")[0].getAttribute("offside"))
                if rcv_off != receiver_offside:
                    # is not of the desired receiver offside/not offside
                    continue

            # we got this far. its valid!
            misses_count+=1

        return misses_count

    @accept_side
    def goals(self, half=None, kick_area=None):
        """ get the number of goals. can accept the side, and the kick area as
        optional arguments.

        sorry for the function signature not being clear. should be:
            goals(self, side=None, half=None, kick_area=None)

        to avoid confusion use named arguments.

        side - the side argument will override the default side.
        kick_area - the kick area argument will only return the number of goals
        half - filter the goals by game half
        scored from that area. (the valid areas are: "GOAL_AREA",
        "PENALTY_AREA", "FAR_SHOT"), None returns the number of goals from any
        area.

        note: goals inside the GOAL_AREA (smaller one) are not considered to be
        inside the PENALTY_AREA (although it would be correct to assume that).
        """
        KICK_AREAS = ["GOAL_AREA", "PENALTY_AREA", "FAR_SHOT"]

        dom_goals = self.dom.getElementsByTagName("goals")[0]
        if half is None and kick_area is None:
            # no filters, return all goals (for that team)
            return int(dom_goals.getAttribute(self.side))


        # validate argument
        if kick_area not in KICK_AREAS+[None]:
            raise StatisticsError("unkown kick area {0}".format(kick_area))
        starttime, endtime = Statistics._timeofhalf(half)

        n=0
        # if we want to filter by kick area we have to check every kick...
        for kick in dom_goals.getElementsByTagName("kick"):
            # check if the kick was made by my team
            if kick.getAttribute("team") != self.side_id:
                continue

            if half is not None:
                # of the desired half
                kick_t = int(kick.getAttribute("time"))
                if not(kick_t >= starttime and kick_t <= endtime):
                    # invalid time window, move along
                    continue

            if kick_area is not None:
                # if it is not the area we are looking for
                if kick.getAttribute("zone") != kick_area:
                    continue

            # we got this far, all conditions are met
            n+=1

        return n

    @accept_side
    def goalmisses(self, half=None, misstype=None):
        """get the number of missed goals.

        misstype - type of goal miss must be one of ["GOALIE_CATCHED", "FAR_OUTSIDE", "OUTSIDE"]
        """
        MISS_TYPES=["GOALIE_CATCHED", "FAR_OUTSIDE", "OUTSIDE"]
        dom_goalmisses=self.dom.getElementsByTagName("goalmisses")[0]

        if half is None and misstype is None:
            # no filters. return all goals
            return int(dom_goalmisses.getAttribute(self.side))

        starttime, endtime = Statistics._timeofhalf(half)

        # validate argument
        if misstype not in MISS_TYPES + [None]:
            raise StatisticsError("unkown miss type {0}".format(misstype))
        n=0
        # for every goal miss
        for goalmiss in dom_goalmisses.getElementsByTagName("goalmiss"):

            if goalmiss.getAttribute("team") != self.side_id:
                # not my team... move along
                continue

            if half is not None:
                # of the desired half
                kick_t = int(goalmiss.getElementsByTagName("kick")[0].getAttribute("time"))
                if not(kick_t >= starttime and kick_t <= endtime):
                    # invalid time window, move along
                    continue

            if misstype is not None:
                if goalmiss.getAttribute("type") != misstype:
                    # invalid type of goal miss, move along
                    continue

            # we got this far, all conditions are met
            n+=1

        return n

    @accept_side
    def attacks(self,half=None, attacktype=None):
        """gets the number of attacks for a team

        the arguments are filters.
        half - the game half
        attacktype - the type of attack, one of ["BROKEN","SLOW","MEDIUM", "FAST"]
        """
        ATTACK_TYPES=["BROKEN", "SLOW", "MEDIUM", "FAST"]
        attacks_dom = self.dom.getElementsByTagName("attacks")[0]

        if half is None and attacktype is None:
            # no filters, easy :)
            return int(attacks_dom.getAttribute(self.side))

        # validate argument
        if attacktype not in ATTACK_TYPES+[None]:
            raise StatisticsError("unkown attack type {0}".format(attacktype))
        starttime, endtime = Statistics._timeofhalf(half)

        # 'do the math'
        n=0
        for attack in attacks_dom.getElementsByTagName("attack"):
            if attack.getAttribute("team") != self.side_id:
                continue

            if attacktype is not None:
                if attack.getAttribute("type") != attacktype:
                    continue

            if half is not None:
                # of the desired half
                start_t = int(attack.getAttribute("start"))
                end_t = int(attack.getAttribute("end"))
                if not(start_t >= starttime and end_t <= endtime):
                    # invalid time window, move along
                    continue

            # we got this far, its valid!
            n+=1

        return n

    @accept_side
    def goalssuffered(self):
        return self.goals(side=self.opponent_side)

    @accept_side
    def corners(self, half=None):
        """return the number of corners. half filters the number of corners by
        half"""
        corners_dom=self.dom.getElementsByTagName("corners")[0]
        if half is None:
            # no filters, easy :)
            return int(corners_dom.getAttribute(self.side))

        # validate argument
        starttime, endtime = Statistics._timeofhalf(half)

        n=0
        for corner in corners_dom.getElementsByTagName("corner"):
            if corner.getAttribute("team") != self.side:
                # not my team, move along
                continue

            if half is not None:
                # of the desired half
                c_time = int(corner.getAttribute("time"))
                if not(starttime <= c_time and c_time <= endtime):
                    # invalid time window, move along
                    continue

            # we got this far, its valid!
            n+=1

        return n

    def kicks_in(self, half=None):
        """return the number of kicks in(?). 'half' filters it by game half"""
        kicksin_dom=self.dom.getElementsByTagName("kicksin")[0]
        if half is None:
            # no filters, easy :)
            return int(kicksin_dom.getAttribute(self.side))

        # validate argument
        starttime, endtime = Statistics._timeofhalf(half)

        n=0
        for kickin in kicksin_dom.getElementsByTagName("corner"):
            if kickin.getAttribute("team") != self.side_id:
                continue

            if half is not None:
                # of the desired half
                k_time = int(kickin.getAttribute("time"))
                if not(starttime <= k_time and k_time <= endtime):
                    # invalid time window, move along
                    continue

            # we got this far, its valid!
            n+=1

        return n

    @accept_side
    def ballpossession(self, zone=None):
        """get the number of cicles with ball possession.
        zone - only give the number of cicles for that zone"""

        bpos_dom = self.dom.getElementsByTagName("ballpossession")[0]
        if zone is None:
            # filters defined, easy
            return int(bpos_dom.getAttribute(self.side))

        # validate the zone argument
        if zone not in self.zones:
            raise StatisticsError("unkown zone {0}".format(zone))

        n=0
        for zone_dom in bpos_dom.getElementsByTagName("zone"):
            # is it the zone we are looking for?
            if zone_dom.getAttribute("name") == self.zones[zone]:
                for possession in zone_dom.getElementsByTagName("possession"):
                    if possession.getAttribute("team") != self.side_id:
                        # wrong team, skip
                        continue

                    # we got this far, its valid
                    n+=int(possession.getAttribute("time"))

        return n

    @accept_side
    def goalopportunities(self):
        """number of goal opportunities"""
        goal_opp_dom = self.dom.getElementsByTagName("goalopportunities")[0]

        return int(goal_opp_dom.getAttribute(self.side))

    @accept_side
    def passchains(self):
        raise NotImplementedError()

    @accept_side
    def wingchanges(self):
        raise NotImplementedError()

####
# functions that provide easier access to the statistics data
####
## commented out but left here for reference.
# teamnamespattern = re.compile(r'^\d+-(.*)(?:_\d+)-vs-(.*)(?:_\d+)(?:_convert(?:ido)?)?\.rcg\.gz\.xml$')
# yearpattern = re.compile(r'\d{4}')
#
#
# def opositeside(side):
#     assert side == "left" or side == "right", "%s is invalid" % (side,)
#     if side == "left":
#         return "right"
#     if side == "right":
#         return "left"
#
# def side_identifier(side):
#     assert side == "left" or side == "right", "%s is invalid" % (side,)
#     if side == "left":
#         return "LEFT_SIDE"
#     if side == "right":
#         return "RIGHT_SIDE"
#
# def tournamentinfo(dirname):
#     ilsplit = dirname[yearpattern.search(dirname).start():].split("/")
#     year = ilsplit[0]
#     atom = "->".join(ilsplit[1:])
#     return (year, atom)
#
# def teamnames(bname):
#     (t1, t2) = teamnamespattern.match(bname).groups()
#     if t1.endswith("_0"):
#         t1 = t1[:-2]
#     if t2.endswith("_0"):
#         t2 = t2[:-2]
#     return (t1,t2)
#
# def zones_dict(side):
#     assert side == "left" or side == "right", "%s is invalid" % (side,)
#     if side == "left":
#         return {
#                "TopLeftleft":       "leftwing_1stquarter",
#                "TopLeftright":      "leftwing_2ndquarter",
#                "MiddleLeftleft":    "middlewing_1stquarter",
#                "MiddleLeftright":   "middlewing_2ndquarter",
#                "BottomLeftleft":    "rightwing_1stquarter",
#                "BottomLeftright":   "rightwing_2ndquarter",
#                "TopRightleft":      "leftwing_3rdquarter",
#                "TopRightright":     "leftwing_4thquarter",
#                "MiddleRightleft":   "middlewing_3rdquarter",
#                "MiddleRightright":  "middlewing_4thquarter",
#                "BottomRightleft":   "rightwing_3rdquarter",
#                "BottomRightright":  "rightwing_4thquarter"
#                }
#     if side == "right":
#         return {
#                "TopLeftleft":       "rightwing_4thquarter",
#                "TopLeftright":      "rightwing_3rdquarter",
#                "MiddleLeftleft":    "middlewing_4thquarter",
#                "MiddleLeftright":   "middlewing_3rdquarter",
#                "BottomLeftleft":    "leftwing_4thquarter",
#                "BottomLeftright":   "leftwing_3rdquarter",
#                "TopRightleft":      "rightwing_2ndquarter",
#                "TopRightright":     "rightwing_1stquarter",
#                "MiddleRightleft":   "middlewing_2ndquarter",
#                "MiddleRightright":  "middlewing_1stquarter",
#                "BottomRightleft":   "leftwing_2ndquarter",
#                "BottomRightright":  "leftwing_1stquarter"
#                }
#
# # start statistics extractors!
# def passes(side, dom):
#     n = int(dom.getElementsByTagName("passes")[0].getAttribute(side))
#     return [("passes", n)]
#
# def passmisses(side, dom):
#     n = int(dom.getElementsByTagName("passmisses")[0].getAttribute(side))
#     return [("passmisses", n)]
#
# def passchains(side, dom):
#     n = int(dom.getElementsByTagName("passchains")[0].getAttribute(side))
#     return [("passchains", n)]
#
# def wingchanges(side, dom):
#     teamid = side_identifier(side)
#     dom_wingchanges = dom.getElementsByTagName("wingchanges")[0]
#     wc_n = int(dom_wingchanges.getAttribute(side))
#     wct_n = 0
#     wcp_n = 0
#     for dom_wingchange in dom_wingchanges.getElementsByTagName("wingchange"):
#         total_variation = dom_wingchange.getAttribute("totalvariation")
#         assert total_variation == "true" or total_variation == "false", "'totalvariation' attribute can only be true or false"
#         # if not of our team, skip
#         if dom_wingchange.getElementsByTagName("pass")[0].getAttribute("team") != teamid:
#             continue
#         # check if we sould increment the total or the partial counter
#         if total_variation == "true":
#             wct_n+=1
#         elif total_variation == "false":
#             wcp_n+=1
#     assert wc_n == wct_n + wcp_n, "%d must be == %d+%d" % (wc_n, wct_n, wcp_n)
#     return [("wingchanges", wc_n), ("wingchanges_totalvariation", wct_n), ("wingchanges_partialvariation", wcp_n) ]
# kick_areas = ["GOAL_AREA", "PENALTY_AREA", "FAR_SHOT"]
#
# def goals(side, dom):
#     teamid = side_identifier(side)
#     res = {}
#     prefix = "goals_kickedfrom_"
#     for area in kick_areas:
#         res[prefix + area] = 0
#
#     dom_goals = dom.getElementsByTagName("goals")[0]
#     n = int(dom_goals.getAttribute(side))
#     for kick in dom_goals.getElementsByTagName("kick"):
#         if kick.getAttribute("team") != teamid:
#             continue
#         key = prefix + kick.getAttribute("zone")
#         assert key in res, "key {0} not present in 'res'".format(key)
#         res[key]+=1
#     return [("goals", n)] + res.items()
#
# def goalmisses(side, dom):
#     teamid = side_identifier(side)
#     zoneprefix = "goalmisses_kickedfrom_"
#     reasonprefix = "goalmisses_"
#     res = {}
#     for area in kick_areas:
#         res[zoneprefix + area] = 0
#     for reason in ["OUTSIDE","GOALIE_CATCHED"]:
#         res[reasonprefix + reason] = 0
#
#     dom_goalmisses = dom.getElementsByTagName("goalmisses")[0]
#     n = int(dom_goalmisses.getAttribute(side))
#     for goalmiss in dom_goalmisses.getElementsByTagName("goalmiss"):
#         if goalmiss.getAttribute("team") != teamid:
#             continue
#         key1 = reasonprefix + goalmiss.getAttribute("type")
#         kick = goalmiss.getElementsByTagName("kick")[0]
#         key2 = zoneprefix + kick.getAttribute("zone")
#         for key in [key1, key2]:
#             assert key in res, "key {0} not present in 'res'".format(key)
#             res[key]+=1
#
#     return [("goalmisses", n)] + res.items()
#
# def goalssuffered(side,dom):
#     opside = opositeside(side)
#     n = int(dom.getElementsByTagName("goals")[0].getAttribute(opside))
#     return [("goalssuffered", n)]
#
# def goalopportunities(side, dom):
#     n = int(dom.getElementsByTagName("goalopportunities")[0].getAttribute(side))
#     return [("goalopportunities", n)]
#
# def favouritezones(side,dom):
#     #inits
#     results = []
#     zonesdict = zones_dict(side)
#     teamid = side_identifier(side)
#
#     ballpossession = dom.getElementsByTagName("ballpossession")[0]
#     bpos_zones = ballpossession.getElementsByTagName("zone")
#     for zone in bpos_zones:
#         zonename = zone.getAttribute("name")
#         if zonename not in zonesdict:
#             continue
#         storein = "timein_" + zonesdict[zonename]
#         for possession in zone.getElementsByTagName("possession"):
#             if possession.getAttribute("team") == teamid:
#                 timeinzone = int(possession.getAttribute("time"))
#                 results.append( (storein, timeinzone) )
#                 break
#     total = reduce(lambda x, y: x+y[1], results, 0)
#     return [(x[0], float(x[1])/float(total)) for x in results]
#
# def zonedominance(side,dom):
#     results = []
#     zonesdict = zones_dict(side)
#     teamid = side_identifier(side)
#     bpos_zones = dom.getElementsByTagName("ballpossession")[0].getElementsByTagName("zone")
#     for zone in bpos_zones:
#         zonename = zone.getAttribute("name")
#         if zonename not in zonesdict:
#             continue
#         storein = "dominance_" + zonesdict[zonename]
#         for possession in zone.getElementsByTagName("possession"):
#             if possession.getAttribute("team") == teamid:
#                 ratio = float(possession.getAttribute("percent"))/100.0
#                 results.append( (storein, ratio) )
#                 break
#     return results
#
# def attacks(side,dom):
#     results = { "total": 0, "BROKEN": 0, "SLOW": 0, "MEDIUM":0, "FAST":0 }
#
#     teamid = side_identifier(side)
#
#     attacks = dom.getElementsByTagName("attacks")[0]
#     results["total"]+= int(attacks.getAttribute(side))
#     attacks = attacks.getElementsByTagName("attack")
#
#     for attack in attacks:
#         if attack.getAttribute("team") == teamid:
#             results[attack.getAttribute("type")]+=1
#
#     retarray = []
#     for k,v in results.items():
#         retarray.append( (("attacks_" + k), v) )
#     return retarray
#
# #end statistics extractors
#
