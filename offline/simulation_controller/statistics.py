#! /usr/bin/env python

import logging
import re
import os
from utils import *
import config

statistics_script="""#! /bin/bash

scriptsdir="{scriptsdir}"
dirname="{dirname}"
rcgconvert="{rcgconvert}"
rcg="{rcg}"
rcgc="{rcgc}"
xml="{xml}"

rcgout="${{dirname}}/rcgconvert-output.log"
rcgerr="${{dirname}}/rcgconvert-error.log"

cd ${{dirname}}

# convert to the right version
"${{rcgconvert}}" -v 3 -o "${{rcgc}}" "${{rcg}}" > ${{rcgout}} 2> ${{rcgerr}}

jclass="soccerscope.SoccerScope"
sout="${{dirname}}/statistics-output.log"
serr="${{dirname}}/statistics-error.log"

# generate the statistics
MYCLASSPATH="${{scriptsdir}}/soccerscope.jar:${{scriptsdir}}/java-xmlbuilder-0.3.jar"
CLASSPATH="${{MYCLASSPATH}}" java ${{jclass}} --batch "${{rcgc}}" "${{xml}}" > ${{sout}} 2> ${{serr}}
"""

# convert the rcg to a supported version
def converted_name(rcg):
    bname, ext = os.path.splitext(rcg)
    if ext == ".gz":
        bname, ext2 = os.path.splitext(bname)
        ext = ext2+ext
    rcgc = bname + "_convert" + ext
    return rcgc

def calculate(rcg):
    scriptsdir=config.scripts_dir
    dirname=os.path.dirname(rcg)
    rcgconvert = config.rcgconvert
    rcgc=converted_name(rcg)
    xml=rcgc+".xml"

    script_name = "calculate_statistics.sh"
    script_name = os.path.join(dirname,script_name)
    content = statistics_script.format(**locals())
    write_script(script_name, content)

    runcommand(script_name)
    return xml


####
# functions that provide easier access to the statistics data
####
teamnamespattern = re.compile(r'^\d+-(.*)(?:_\d+)-vs-(.*)(?:_\d+)(?:_convert(?:ido)?)?\.rcg\.gz\.xml$')
yearpattern = re.compile(r'\d{4}')


def opositeside(side):
    assert side == "left" or side == "right", "%s is invalid" % (side,)
    if side == "left":
        return "right"
    if side == "right":
        return "left"

def side_identifier(side):
    assert side == "left" or side == "right", "%s is invalid" % (side,)
    if side == "left":
        return "LEFT_SIDE"
    if side == "right":
        return "RIGHT_SIDE"

def tournamentinfo(dirname):
    ilsplit = dirname[yearpattern.search(dirname).start():].split("/")
    year = ilsplit[0]
    atom = "->".join(ilsplit[1:])
    return (year, atom)

def teamnames(bname):
    (t1, t2) = teamnamespattern.match(bname).groups()
    if t1.endswith("_0"):
        t1 = t1[:-2]
    if t2.endswith("_0"):
        t2 = t2[:-2]
    return (t1,t2)

def zones_dict(side):
    assert side == "left" or side == "right", "%s is invalid" % (side,)
    if side == "left":
        return {
               "TopLeftleft":       "leftwing_1stquarter",
               "TopLeftright":      "leftwing_2ndquarter",
               "MiddleLeftleft":    "middlewing_1stquarter",
               "MiddleLeftright":   "middlewing_2ndquarter",
               "BottomLeftleft":    "rightwing_1stquarter",
               "BottomLeftright":   "rightwing_2ndquarter",
               "TopRightleft":      "leftwing_3rdquarter",
               "TopRightright":     "leftwing_4thquarter",
               "MiddleRightleft":   "middlewing_3rdquarter",
               "MiddleRightright":  "middlewing_4thquarter",
               "BottomRightleft":   "rightwing_3rdquarter",
               "BottomRightright":  "rightwing_4thquarter"
               }
    if side == "right":
        return {
               "TopLeftleft":       "rightwing_4thquarter",
               "TopLeftright":      "rightwing_3rdquarter",
               "MiddleLeftleft":    "middlewing_4thquarter",
               "MiddleLeftright":   "middlewing_3rdquarter",
               "BottomLeftleft":    "leftwing_4thquarter",
               "BottomLeftright":   "leftwing_3rdquarter",
               "TopRightleft":      "rightwing_2ndquarter",
               "TopRightright":     "rightwing_1stquarter",
               "MiddleRightleft":   "middlewing_2ndquarter",
               "MiddleRightright":  "middlewing_1stquarter",
               "BottomRightleft":   "leftwing_2ndquarter",
               "BottomRightright":  "leftwing_1stquarter"
               }

# start statistics extractors!
def passes(side, dom):
    n = int(dom.getElementsByTagName("passes")[0].getAttribute(side))
    return [("passes", n)]

def passmisses(side, dom):
    n = int(dom.getElementsByTagName("passmisses")[0].getAttribute(side))
    return [("passmisses", n)]

def passchains(side, dom):
    n = int(dom.getElementsByTagName("passchains")[0].getAttribute(side))
    return [("passchains", n)]

def wingchanges(side, dom):
    teamid = side_identifier(side)
    dom_wingchanges = dom.getElementsByTagName("wingchanges")[0]
    wc_n = int(dom_wingchanges.getAttribute(side))
    wct_n = 0
    wcp_n = 0
    for dom_wingchange in dom_wingchanges.getElementsByTagName("wingchange"):
        total_variation = dom_wingchange.getAttribute("totalvariation")
        assert total_variation == "true" or total_variation == "false", "'totalvariation' attribute can only be true or false"
        # if not of our team, skip
        if dom_wingchange.getElementsByTagName("pass")[0].getAttribute("team") != teamid:
            continue
        # check if we sould increment the total or the partial counter
        if total_variation == "true":
            wct_n+=1
        elif total_variation == "false":
            wcp_n+=1
    assert wc_n == wct_n + wcp_n, "%d must be == %d+%d" % (wc_n, wct_n, wcp_n)
    return [("wingchanges", wc_n), ("wingchanges_totalvariation", wct_n), ("wingchanges_partialvariation", wcp_n) ]
kick_areas = ["GOAL_AREA", "PENALTY_AREA", "FAR_SHOT"]

def goals(side, dom):
    teamid = side_identifier(side)
    res = {}
    prefix = "goals_kickedfrom_"
    for area in kick_areas:
        res[prefix + area] = 0

    dom_goals = dom.getElementsByTagName("goals")[0]
    n = int(dom_goals.getAttribute(side))
    for kick in dom_goals.getElementsByTagName("kick"):
        if kick.getAttribute("team") != teamid:
            continue
        key = prefix + kick.getAttribute("zone")
        assert key in res, "key {0} not present in 'res'".format(key)
        res[key]+=1
    return [("goals", n)] + res.items()

def goalmisses(side, dom):
    teamid = side_identifier(side)
    zoneprefix = "goalmisses_kickedfrom_"
    reasonprefix = "goalmisses_"
    res = {}
    for area in kick_areas:
        res[zoneprefix + area] = 0
    for reason in ["OUTSIDE","GOALIE_CATCHED"]:
        res[reasonprefix + reason] = 0

    dom_goalmisses = dom.getElementsByTagName("goalmisses")[0]
    n = int(dom_goalmisses.getAttribute(side))
    for goalmiss in dom_goalmisses.getElementsByTagName("goalmiss"):
        if goalmiss.getAttribute("team") != teamid:
            continue
        key1 = reasonprefix + goalmiss.getAttribute("type")
        kick = goalmiss.getElementsByTagName("kick")[0]
        key2 = zoneprefix + kick.getAttribute("zone")
        for key in [key1, key2]:
            assert key in res, "key {0} not present in 'res'".format(key)
            res[key]+=1

    return [("goalmisses", n)] + res.items()

def goalssuffered(side,dom):
    opside = opositeside(side)
    n = int(dom.getElementsByTagName("goals")[0].getAttribute(opside))
    return [("goalssuffered", n)]

def goalopportunities(side, dom):
    n = int(dom.getElementsByTagName("goalopportunities")[0].getAttribute(side))
    return [("goalopportunities", n)]

def favouritezones(side,dom):
    #inits
    results = []
    zonesdict = zones_dict(side)
    teamid = side_identifier(side)

    ballpossession = dom.getElementsByTagName("ballpossession")[0]
    bpos_zones = ballpossession.getElementsByTagName("zone")
    for zone in bpos_zones:
        zonename = zone.getAttribute("name")
        if zonename not in zonesdict:
            continue
        storein = "timein_" + zonesdict[zonename]
        for possession in zone.getElementsByTagName("possession"):
            if possession.getAttribute("team") == teamid:
                timeinzone = int(possession.getAttribute("time"))
                results.append( (storein, timeinzone) )
                break
    total = reduce(lambda x, y: x+y[1], results, 0)
    return [(x[0], float(x[1])/float(total)) for x in results]

def zonedominance(side,dom):
    results = []
    zonesdict = zones_dict(side)
    teamid = side_identifier(side)
    bpos_zones = dom.getElementsByTagName("ballpossession")[0].getElementsByTagName("zone")
    for zone in bpos_zones:
        zonename = zone.getAttribute("name")
        if zonename not in zonesdict:
            continue
        storein = "dominance_" + zonesdict[zonename]
        for possession in zone.getElementsByTagName("possession"):
            if possession.getAttribute("team") == teamid:
                ratio = float(possession.getAttribute("percent"))/100.0
                results.append( (storein, ratio) )
                break
    return results

def attacks(side,dom):
    results = { "total": 0, "BROKEN": 0, "SLOW": 0, "MEDIUM":0, "FAST":0 }

    teamid = side_identifier(side)

    attacks = dom.getElementsByTagName("attacks")[0]
    results["total"]+= int(attacks.getAttribute(side))
    attacks = attacks.getElementsByTagName("attack")

    for attack in attacks:
        if attack.getAttribute("team") == teamid:
            results[attack.getAttribute("type")]+=1

    retarray = []
    for k,v in results.items():
        retarray.append( (("attacks_" + k), v) )
    return retarray

#end statistics extractors

