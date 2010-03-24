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

# convert the rcg to a supported version
def _converted_name(rcg):
    bname, ext = os.path.splitext(rcg)
    if ext == ".gz":
        bname, ext2 = os.path.splitext(bname)
        ext = ext2+ext
    rcgc = bname + "_convert" + ext
    return rcgc

def create_from_rcg(rcg, *args,**kwds):
    scriptsdir=config.scripts_dir
    dirname=os.path.dirname(rcg)
    rcgconvert = config.rcgconvert
    rcgc=_converted_name(rcg)
    xml=rcgc+".xml"

    if os.path.isfile(xml):
        dbg="statistics file for {0} already exists".format(rcg)
        logging.debug(dbg)
    else:
        script_name = "calculate_statistics.sh"
        script_name = os.path.join(dirname,script_name)
        content = STATISTICS_SCRIPT.format(**locals())
        write_script(script_name, content)
        dbg="converting {0} and creating statistics xml".format(rcg)
        logging.debug(dbg)
        runcommand(script_name)

    return Statistics(xml=xml,*args,**kwds)

##
# statistics mega class
##

class StatisticsError(Exception):
    def __init__(self, msg='Unspecified'):
        Exception.__init__(self, msg)

def accept_side(fn):
    @wraps(fn)
    def wrapper(self, side=None, *args, **kwds):
        # validate side argument.
        assert side in self.valid_magic, "%s is invalid" % (side,)

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

class Statistics(object):
    SIDES=["left","right"]

    def __init__(self, xml, side=None, teams=None):
        if not os.path.exists(xml):
            raise StatisticsError('{0} not found'.format(xml))
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
        """the default team when no team/side argument is supplied.
        """
        assert self._team in self.teams or self._team is None
        return self._team

    @team.setter
    def team(self,value):
        # TODO - maibe this should raise an exception instead
        assert value in self.teams or value is None, "%s is invalid" % (value,)
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
        # TODO - maibe this should raise an exception instead
        assert value in Statistics.SIDES or value is None, "%s is invalid" % (value,)
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

    @accept_side
    def opponent_side(self):
        if self.side == "left":
            return "right"
        if self.side == "right":
            return "left"

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
        raise StatisticsError( errmsg.format(half))
    ###
    # statistics extraction starts here.
    ##
    @accept_side
    def passes(self, half=None):
        # half is not set. return all passes
        if half is None:
            return int(self.dom.getElementsByTagName("passes")[0].getAttribute(self.side))

        starttime, endtime = Statistics._timeofhalf(half)

        passes_tag=self.dom.getElementsByTagName("passes")[0]
        passes_count=0
        for pass_ in passes_tag.getElementsByTagName("pass"):
            # not my team... move along
            if pass_.getAttribute("team") != self.side_id:
                continue

            kick_t = int(pass_.getElementsByTagName("kick").getAttribute("time"))
            reception_t = pass_.getElementsByTagName("reception")
            # time window is valid
            if kick_t >= starttime and reception_t <= endtime:
                passes_count+=1

        return passes_count

    @accept_side
    def passmisses(self):
        # half is not set. return all passmisses
        if half is None:
            return int(dom.getElementsByTagName("passmisses")[0].getAttribute(self.side))

        starttime, endtime = Statistics._timeofhalf(half)

        misses_tag=self.dom.getElementsByTagName("passmisses")[0]
        misses_count=0
        for miss in passes_tag.getElementsByTagName("passmiss"):
            # not my team... move along
            if pass_.getAttribute("team") != self.side_id:
                continue

            kick_t = int(miss.getElementsByTagName("kick").getAttribute("time"))
            # time window is valid
            if kick_t >= starttime and kick_t <= endtime:
                misses_count+=1

        return misses_count

    @accept_side
    def passchains(self):
        raise NotImplementedError()

    @accept_side
    def wingchanges(self):
        raise NotImplementedError()

    @accept_side
    def goals(self, kick_area=None):
        """ get the number of goals. can accept the side, and the kick area as optional
        arguments.

        sorry for the function signature not being clear. should be:
            goals(self, side=None, kick_area=None)

	to avoid confusion use named arguments.

        side - the side argument will overrides the default side.
        kick_area - the kick area argument will only return the number of goals
        scored from that area. (the valid areas are: "GOAL_AREA",
        "PENALTY_AREA", "FAR_SHOT"), None returns the number of goals from any
        area.
        """
        KICK_AREAS = ["GOAL_AREA", "PENALTY_AREA", "FAR_SHOT"]

        dom_goals = self.dom.getElementsByTagName("goals")[0]
        if kick_area is None:
            # no area specified, return all goals (for that team)
            n = int(dom_goals.getAttribute(self.side))
        else:
            n=0
            assert kick_area in KICK_AREAS, "kick_area must be one of {0}".format(KICK_AREAS)
            # if we want to filter by kick area we have to check every kick...
            for kick in dom_goals.getElementsByTagName("kick"):
                # check if the kick was made by my team
                if kick.getAttribute("team") != self.side_id():
                    continue
                # if its the area we are looking for
                if kick.getAttribute("zone") == kick_area:
                    n+=1
        return n

    @accept_side
    def goalmisses(self):
        raise NotImplementedError()

    @accept_side
    def goalssuffered(self):
        raise NotImplementedError()

    @accept_side
    def goalopportunities(self):
        raise NotImplementedError()

    @accept_side
    def favouritezones(self):
        raise NotImplementedError()

    @accept_side
    def zonedominance(self):
        raise NotImplementedError()

    @accept_side
    def attacks(self):
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
