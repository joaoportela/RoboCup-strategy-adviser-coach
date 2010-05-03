#! /usr/bin/env python

"""
csv data that needs to be generated. this module is more complex and
complete.
"""

__all__ = ["statistics14_keys","statistics14"]

from datastructures import SortedDict

statistics14_keys=[
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

