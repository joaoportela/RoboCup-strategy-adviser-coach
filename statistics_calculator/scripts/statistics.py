#! /usr/bin/env python

import re
import os
import xml.dom.minidom as minidom

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

def mergeteams(teams):
    for team,events in teams.items():
        new = {}
        for (event,count) in events:
            new[event] = new.get(event,0) + count
        teams[team] = new

def mergegames(games):
    for year,gamesb in games.items():
        for atom,gamesc in gamesb.items():
            for game, teams in gamesc.items():
                for t in game:
                    teams[t] = dict(teams[t])

def updateteams(teams, (t1, gdataleft), (t2, gdataright)):
    teams[t1] = teams.get(t1, []) + [("aa_gamesplayed", 1)]
    teams[t2] = teams.get(t2, []) + [("aa_gamesplayed", 1)]
    teams[t1] = teams.get(t1, []) + gdataleft
    teams[t2] = teams.get(t2, []) + gdataright

def updategames(games, (year, tournamentatom), (t1, gdataleft), (t2, gdataright)):
    # build the base of the dictionary
    if year not in games:
        games[year] = {}
    if tournamentatom not in games[year]:
        games[year][tournamentatom] = {}

    # games should not repeat in the same atom
    # note: (t1,t2) is suposed to be diferent from (t2,t1)
    assert (t1,t2) not in games[year][tournamentatom], ""

    games[year][tournamentatom][(t1,t2)] =  {}
    games[year][tournamentatom][(t1,t2)][t1] = gdataleft
    games[year][tournamentatom][(t1,t2)][t2] = gdataright

def teams_events():
    functions = [passes, passmisses, passchains, wingchanges, goals,
            goalmisses, goalssuffered, goalopportunities, favouritezones,
            zonedominance, attacks]
    teams= {}
    games= {}
    with open('filenames.txt') as fns:
        for fname in fns:
            print "processing {0}".format(fname.strip())
            fname = fname.strip()
            # allow comments...
            if fname.startswith("#"):
                continue

            dom = minidom.parse(fname)

            dirname = os.path.dirname(fname)
            bname = os.path.basename(fname)

            (year, tournamentatom) = tournamentinfo(dirname)
            (t1,t2) = teamnames(bname)
            print "\tyear:{0} atom:{1} t1:{2} t2:{3}".format(year, tournamentatom, t1, t2)

            gdataleft = []
            gdataright = []
            for funct in functions:
                gdataleft.extend(funct("left",dom))
                gdataright.extend(funct("right", dom))

            updateteams(teams, (t1, gdataleft), (t2, gdataright))
            updategames(games, (year, tournamentatom), (t1, gdataleft), (t2, gdataright))
            print "processed {0}\n".format(fname)

        mergeteams(teams)
        mergegames(games)
    return (teams,games)

def printteamscsv(fname, teams):
    with open(fname,'w') as f:
        eventnames = sorted(teams.values()[0].keys())
        # table head
        f.write(";".join([""] + eventnames) + "\n")
        # table rows
        for teamname, eventsteams in teams.items():
            row = []
            row.append(teamname)
            for eventname in eventnames:
                row.append(eventsteams[eventname])
            # print row
            f.write(";".join(str(x) for x in row) + "\n")


def printgamescsv(fname, games):
    #find event names
    eventnames = sorted(games.values()[0].values()[0].values()[0].values()[0].keys())
    with open(fname,'w') as f:
        # table head
        f.write(";".join([""] + eventnames) + "\n")
        #table rows
        for year,gamesb in sorted(games.items(),key=lambda x: (int(x[0]),x[1])):
            # row of the year
            f.write("{0}\n".format(year))
            for atom,gamesc in sorted(gamesb.items()):
                # row of the atom
                f.write("{0}\n".format(atom))
                for game, teams in sorted(gamesc.items()):
                    # print a game.
                    for t in game:
                        row = []
                        for evtname in eventnames:
                            #print "--{0}".format(teams[t][evtname])
                            row.append(teams[t][evtname])
                        # row by team
                        f.write(";".join(str(x) for x in [t]+row) + "\n")
                    f.write("\n")
                f.write("\n")
            f.write("\n\n")

def printteamnames():
    teams = set()
    with open('filenames.txt') as fns:
        for fname in fns:
            print "processing {0}".format(fname)
            fname = fname.strip()
            # allow comments...
            if fname.startswith("#"):
                continue

            dirname = os.path.dirname(fname)
            bname = os.path.basename(fname)

            (t1,t2) = teamnames(bname)
            teams.add(t1)
            teams.add(t2)
    print "\n".join(team for team in sorted(teams))

if __name__ == '__main__':
    import sys
    if len(sys.argv) > 1:
        if sys.argv[1] == "teamnames":
            printteamnames()
            sys.exit(0)
    (teams, games) = teams_events()
    # print games
    printteamscsv("teams_statistics.csv", teams)
    printgamescsv("games_statistics.csv", games)

