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
MYCLASSPATH="${{scriptsdir}}/soccerscope.jar:${{scriptsdir}}/java-xmlbuilder-0.3.jar:${{scriptsdir}}/sexpr.jar"
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

ATTACK_TYPES=["BROKEN", "SLOW", "MEDIUM", "FAST"]
GOALMISS_TYPES=["GOALIE_CATCHED", "FAR_OUTSIDE", "OUTSIDE"]
KICK_AREAS=["GOAL_AREA", "PENALTY_AREA", "FAR_SHOT"]
ZONES_LIST=["leftwing_1stquarter", "leftwing_2ndquarter",
        "middlewing_1stquarter", "middlewing_2ndquarter",
        "rightwing_1stquarter", "rightwing_2ndquarter", "leftwing_3rdquarter",
        "leftwing_4thquarter", "middlewing_3rdquarter",
        "middlewing_4thquarter", "rightwing_3rdquarter",
        "rightwing_4thquarter"]


# name that the supported version rcg will have
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
        analysis_doms=dom.getElementsByTagName("analysis")
        if len(analysis_doms) != 1:
            return False
        version=analysis_doms[0].getAttribute("version")
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
        if not self.valid_magic(side):
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
# Statistics mega class
##

class Statistics(object):
    SIDES=["left","right"]

    def __init__(self, xml, side=None, teams=None):
        self.strange_zones=False

        xml=self._valid_xml(xml)
        self._xml=xml
        if teams is None:
            self._teams = Statistics.teamnames_from_xml(self.xml)
            logging.debug("calculating team names from the xml file")
        else:
            self._teams = teams
            self._validate_teams()

        self.magic_side = side

        for team in self.teams:
            if team in Statistics.SIDES:
                warn = "there is a team named '{0}'. This is sh*t prone!".format(team)
                logging.warning(warn)

        dbg="Statistics object instantiated. ( xml: '{0}' teams: '{1}', side: '{2}')"
        logging.debug(dbg.format(self.xml,self.teams, self.team))


    def _valid_xml(self, xml):
        """auxiliary method that checks if the XML file is valid and if it is not
        tries to make it valid. Raises an exception when it gracefully cannot recover.
        """
        if not (os.path.exists(xml) and valid_version(xml)):
            rcg, ext = os.path.splitext(xml)
            if os.path.isfile(rcg):
                warnmsg="xml was not valid. Recreating from {0}.".format(rcg)
                logging.warn(warnmsg)
                xml=rcgtoxml(rcg,convert=False)
                if not(os.path.exists(xml) and valid_version(xml)):
                    # we still couldn't get the right xml.
                    errmsg="even after trying to recreate the correct xml file could"
                    errmsg+="not be obtained."
                    raise StatisticsError(errmsg)
            else:
                # print the appropriate error message when we could not have the
                # correct version xml and could not recover.
                if not os.path.exists(xml):
                    errmsg="{0} not found. Could not find the rcg file to"
                    errmsg+="recreate."
                elif not valid_version(xml):
                    errmsg="{0} version was wrong."
                    errmsg+="rcg file to recreate was not found."
                raise StatisticsError(errmsg.format(xml))
        # when we get this far without an exception, everything is OK
        return xml


    def _validate_teams(self):
        teams=Statistics.teamnames_from_xml(self.xml)
        valid=(same_team(self.teams[0], teams[0]) and
                same_team(self.teams[1],teams[1]))
        if not valid:
            errmsg = "teams {0} are not valid according to the xml file name"
            errmsg = "{1} {2}"
            raise StatisticsError(errmsg.format(self.teams,self.xml,teams))


    @property
    def _teamslower(self):
        if not hasattr(self, "__teamslower_cache") or self.__teamslower_cache is None:
            if self.teams is None:
                self.__teamslower_cache=None
            else:
                self.__teamslower_cache=(self.teams[0].lower(), self.teams[1].lower())

        return self.__teamslower_cache

    @property
    def dom(self):
        """document object model of this statistics instance"""
        if (not hasattr(self, "_dom")) or self._dom is None:
            logging.debug("instancianting minidom object")
            self._dom = minidom.parse(self.xml)
            logging.debug("minidom object instanciated")

        return self._dom

    @dom.deleter
    def dom(self):
        # this operation is allowed to save memory.
        logging.debug("deleting minidom object")
        del self._dom

    def save_mem(self):
        """method that tries to free some memory. may cause the next call to
        some of the statistics methods slower.

        note: currently only deletes the dom object. next call to a method that
        uses this will require waiting for instanciating a new dom object.
        """
        #frees the dom object that ocupies a lot of memory
        del self.dom

    def valid_magic(self, val):
        if val is None:
            return True
        return val.lower() in Statistics.SIDES+list(self._teamslower)

    def magic_side(self,value):
        if not self.valid_magic(value):
            raise StatisticsError("side not specified")

        if value is None:
            self.side=value
        # guess if the value is the side or the team
        elif Statistics.isside(value):
            self.side = value
        elif self.isteam(value):
            self.team = value

        assert self.side in Statistics.SIDES or self.side is None
        assert self.team in self.teams or self.team is None

    magic_side=property(fset=magic_side)

    @staticmethod
    def isside(value):
        if not hasattr(value, "lower"):
            return False
        return value.lower() in Statistics.SIDES

    def isteam(self,value):
        if not hasattr(value, "lower"):
            return False
        return value.lower() in self._teamslower

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
        if value is not None:
            value=value.lower()
        if not(value in self._teamslower or value is None):
            raise StatisticsError("team {0} is invalid".format(value))

        if value is not None:
            index=self._teamslower.index(value)
            self._team = self.teams[index]
            self._side=Statistics.SIDES[index]
        else:
            self._team=None
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
        if value is not None:
            value=value.lower()

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
        elif self.side == "right":
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
    # Statistics extraction starts here.
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
    def passmisses(self, half=None, offensive=None, receiver_offside=None):
        """ passes misses.
        the arguments are filters, None does not filter.

        half - filter the number of passes by game half (1 or 2)
        """
        # no filters set. return all passmisses
        if half is None and offensive is None and receiver_offside is None:
            return int(self.dom.getElementsByTagName("passmisses")[0].getAttribute(self.side))

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
        dom_goalmisses=self.dom.getElementsByTagName("goalmisses")[0]

        if half is None and misstype is None:
            # no filters. return all goals
            return int(dom_goalmisses.getAttribute(self.side))

        starttime, endtime = Statistics._timeofhalf(half)

        # validate argument
        if misstype not in GOALMISS_TYPES + [None]:
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

    @accept_side
    def kicks_in(self, half=None):
        """return the number of kicks in. 'half' filters it by game half"""
        kicksin_dom=self.dom.getElementsByTagName("kicksin")[0]
        if half is None:
            # no filters, easy :)
            return int(kicksin_dom.getAttribute(self.side))

        # validate argument
        starttime, endtime = Statistics._timeofhalf(half)

        n=0
        for kickin in kicksin_dom.getElementsByTagName("kickin"):
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
    def goalkicks(self, half=None):
        """return the number of goal kicks. 'half' filters it by game half"""
        goalkicks_dom=self.dom.getElementsByTagName("goalkicks")[0]
        if half is None:
            # no filters, easy :)
            return int(goalkicks_dom.getAttribute(self.side))

        # validate argument
        starttime, endtime = Statistics._timeofhalf(half)

        n=0
        for goalkick in goalkicks_dom.getElementsByTagName("goalkick"):
            if goalkick.getAttribute("team") != self.side_id:
                continue

            if half is not None:
                # of the desired half
                gk_time = int(goalkick.getAttribute("time"))
                if not(starttime <= gk_time and gk_time <= endtime):
                    # invalid time window, move along
                    continue

            # we got this far, its valid!
            n+=1

        return n

    @accept_side
    def ballpossession(self, zone=None, ratio=False):
        """get the number of cicles with ball possession.
        zone - only give the number of cicles for that zone
        ratio - ratio is a boolean variable that when True makes the function return the ball
        possession ratio instead of ball possession time"""

        if ratio not in [True, False]:
            errmsg="ratio must be boolean. got {0}".format(ration)
            raise StatisticsError(errmsg)

        bpos_dom = self.dom.getElementsByTagName("ballpossession")[0]
        if zone is None:
            # no filters defined, easy
            n=int(bpos_dom.getAttribute(self.side))
            if ratio:
                n=n/float(bpos_dom.getAttribute("total"))
            return n

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
                    if ratio:
                        n+=float(possession.getAttribute("percent"))
                    else:
                        n+=int(possession.getAttribute("time"))

        return n

    @accept_side
    def goalopportunities(self):
        """number of goal opportunities"""
        goal_opp_dom = self.dom.getElementsByTagName("goalopportunities")[0]

        return int(goal_opp_dom.getAttribute(self.side))

    @accept_side
    def passchains(self):
        passchains_dom=self.dom.getElementsByTagName("passchains")[0]
        return int(passchains_dom.getAttribute(self.side))

    @accept_side
    def wingchanges(self, totalvariation=None):
        if totalvariation not in [True, False, None]:
            errmsg="totalvariation must be boolean of none. was {0}"
            raise StatisticsError(errmsg.format(totalvariation))

        dom_wingchanges=self.dom.getElementsByTagName("wingchanges")[0]

        if totalvariation is None:
            # no filters, easy.
            return int(dom_wingchanges.getAttribute(self.side))

        n=0
        for dom_wingchange in dom_wingchanges.getElementsByTagName("wingchange"):
            wingchange_sideid=dom_wingchange.getElementsByTagName("pass")[0].getAttribute("team")
            if wingchange_sideid != self.side_id:
                #not my team, move along
                continue

            if totalvariation is not None:
                tot_var=str2bool(dom_wingchange.getAttribute("totalvariation"))
                if tot_var != totalvariation:
                    # not the type of variation i wanted.
                    continue

            # we got this far, increment
            n+=1


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
