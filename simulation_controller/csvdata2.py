#! /usr/bin/env python

"""
csv data that needs to be generated. this module is more complex and
complete.
"""

__all__ = ["BONUS_INFO_KEYS", "bonus_info", "STATISTICS14_KEYS", "statistics14", "EVALUATORS_KEYS", "evaluators"]

from datastructures import SortedDict
from evaluator import *

BONUS_INFO_KEYS=["xml","team"]

def bonus_info(st):
    """contains bonus info to be included in the csvs"""
    d=SortedDict()
    d["xml"]=st.xml
    d["team"]=st.team
    return d

STATISTICS14_KEYS=[
        "passmisses",
        "goals",
        "outsides",
        "goalkicks",
        "passmisses_offside",
        "fast_attacks",
        "all_attacks",
        "leftwing_1stquarter_possession",
        "leftwing_2ndquarter_possession",
        "middlewing_1stquarter_possession",
        "middlewing_2ndquarter_possession",
        "middlewing_4thquarter_possession",
        "rightwing_3rdquarter_possession",
        "passchains"
        ]

def statistics14(st):
    """
    the 14 chosen statistics.

    st - statistics object from where the statistics are extracted.
    """
    d=SortedDict()
    d["passmisses"]=st.passmisses()
    d["goals"]=st.goals()
    d["outsides"]=st.goalkicks()+st.corners()+st.kicks_in()
    d["goalkicks"]=st.goalkicks()
    d["passmisses_offside"]=st.passmisses(receiver_offside=True)
    d["fast_attacks"]=st.attacks(attacktype="FAST")
    d["all_attacks"]=st.attacks()
    d["leftwing_1stquarter_possession"]=st.ballpossession(zone="leftwing_1stquarter")
    d["leftwing_2ndquarter_possession"]=st.ballpossession(zone="leftwing_2ndquarter")
    d["middlewing_1stquarter_possession"]=st.ballpossession(zone="middlewing_1stquarter")
    d["middlewing_2ndquarter_possession"]=st.ballpossession(zone="middlewing_2ndquarter")
    d["middlewing_4thquarter_possession"]=st.ballpossession(zone="middlewing_4thquarter")
    d["rightwing_3rdquarter_possession"]=st.ballpossession(zone="rightwing_3rdquarter")
    d["passchains"]=st.passchains()

    return d

EVALUATORS_KEYS=[
        "PointsEvaluator",
        "GoalDifferenceEvaluator",
        "ReliefEvaluator",
        "MARSEvaluator"
        ]
def evaluators(st):
    """
    the result of the evaluators on the statistics object.
    """
    d=SortedDict()
    d["PointsEvaluator"]=PointsEvaluator(st,st.team).value()
    d["GoalDifferenceEvaluator"]=GoalDifferenceEvaluator(st,st.team).value()
    d["ReliefEvaluator"]=ReliefEvaluator(st,st.team).value()
    d["MARSEvaluator"]=MARSEvaluator(st,st.team).value()

    return d

