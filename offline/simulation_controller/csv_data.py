#! /usr/bin/env python

"""csv data that needs to be generated"""

all= ["EVALUATORS","STATISTICS"]

from statistics import Statistics
import statistics
from evaluator import *

# EVALUATORS contains the list of evaluators.
# it is in the format: (name,constructor)
EVALUATORS=[("BasicEvaluator", BasicEvaluator), ("GoalDifferenceEvaluator",
    GoalDifferenceEvaluator), ("ReliefEvaluator", ReliefEvaluator),
    ("MARSEvaluator", MARSEvaluator)]

# STATISTICS is a tripple. 1-the name 2-the statistics funtions to call
# 3-the function arguments
# calling all the functions is gives the data for the CSV
# to obtain the required data for the CSV
STATISTICS=[]

# attacks
func=Statistics.attacks
STATISTICS.append(("attacks_total", func, {}))
for t in statistics.ATTACK_TYPES:
    gen=("attacks_"+str(t), func, {"attacktype":t})
    STATISTICS.append(gen)
del func

#ball possession
func=Statistics.ballpossession
STATISTICS.append(("possession-ratio_total",func,{"ratio":True}))
for zone in statistics.ZONES_LIST:
    gen=("possession-ratio_"+str(zone), func, {"zone": zone,"ratio":True})
    STATISTICS.append(gen)

STATISTICS.append(("possession-time_total",func,{"ratio":False}))
for zone in statistics.ZONES_LIST:
    gen=("possession-timein_"+str(zone), func, {"zone": zone,"ratio":False})
    STATISTICS.append(gen)
del func

#goals
func=Statistics.goals
STATISTICS.append(("goals_total",func,{}))
for a in statistics.KICK_AREAS:
    gen=("goals-from_"+str(a),func,{"kick_area":a})
    STATISTICS.append(gen)
del func

#goalssuffered
func=Statistics.goalssuffered
STATISTICS.append(("goalssuffered",func, {}))
del func


#goalmisses
func=Statistics.goalmisses
STATISTICS.append(("goalmisses_total",func, {}))
for misst in statistics.GOALMISS_TYPES:
    gen=("goamisses_"+str(misst),func,{"misstype":misst})
    STATISTICS.append(gen)
del func
# goalmisses_kickedfrom_FAR_SHOT
# goalmisses_kickedfrom_GOAL_AREA
# goalmisses_kickedfrom_PENALTY_AREA


#goalopportunities
func=Statistics.goalopportunities
STATISTICS.append(("goalopportunities", func, {}))
del func

#passchains
func=Statistics.passchains
STATISTICS.append(("passchains",func, {}))
del func

# passes
func=Statistics.passes
STATISTICS.append(("passes",func,{}))
del func

#passmisses
func=Statistics.passmisses
STATISTICS.append(("passmisses",func,{}))
del func

#wingchanges
func=Statistics.wingchanges
STATISTICS.append(("wingchanges_total",func,{}))
STATISTICS.append(("wingchanges_partialvariation",func,{"totalvariation":False}))
STATISTICS.append(("wingchanges_totalvariation",func,{"totalvariation":True}))
del func
