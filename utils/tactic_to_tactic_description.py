#! /usr/bin/env python

"""
file that converts a tactic to its description

note: this file is very specific and it is not likely that it can ever be
reused (also, changes in config.py may result erroneous behaviour).
"""

from collections import namedtuple

Tactic=namedtuple('Tactic', 'id formation setplays')

def setplaysets_gendict(fname='../simulation_controller/confs/generated_base_strategy.conf'):
    processing=False
    setplayid=None
    setplaysets={}
    with open(fname) as fin:
        for line in fin:
            if not processing:
                # check if we should be processing
                if 'SetPlays Definition' in line:
                    processing=True
                continue

            if 'General Domain Parameters' in line:
                # processing section ended
                break

            # when inside the processing section
            if not setplayid:
                setplayid=int(line.split()[0])
            else:
                expanded_setplayset=map(int, line.split())
                setplaysets[setplayid] = expanded_setplayset
                setplayid=None

    return setplaysets

setplaysets_dict=setplaysets_gendict()

def tactic_withparams(fname='../simulation_controller/confs/generated_base_strategy.conf',
        setplaysets_dict=setplaysets_dict):
    tactic_id=None
    tactics=[]
    with open(fname) as fin:
        for line in fin:
            if tactic_id:
                s_line=line.split()
                formation=int(s_line[0])
                setplays=setplaysets_dict[int(s_line[2])]
                assert tactic_id not in tactics
                tactics.append(Tactic(tactic_id, formation, setplays))

            if 'Tactic ' in line:
                # next line has info
                tactic_id=int(line.split()[0])
            else:
                tactic_id=None

    return tactics

formations_description={
        3:"433",
        14:"442"
        }

setplays_description={
        43 : "kickOffToWingerIndirect5P",
        45 : "kickOffToWingerIndirect3P",
        41 : "freeKickCenter4P_simple",
        36 : "indirectFreeKick_middle_simple",
        37 : "goalieFreeKick_left_dynamic_forward_positions_6players_forward_fast",
        2  : "goalieFreeKick_centre_zig_zag",
        29 : "ck4",
        10 : "ck5"
        }

def human_readable(tactics):
    tactics_h={}
    for tactic in tactics:
        formation=formations_description[tactic.formation]
        setplays=map(lambda x : setplays_description[x], tactic.setplays)
        tactics_h[tactic.id]=Tactic(tactic.id, formation, setplays)
    return tactics_h


tactics=tactic_withparams()
tactics_h=human_readable(tactics)

if __name__ == '__main__':
    import sys
    if len(sys.argv) > 1:
        tactic_id=sys.argv[1]
        if tactic_id.startswith("tactic"):
            tactic_id=tactic_id[6:]
        if tactic_id.startswith("-"):
            tactic_id=tactic_id[1:]
        print tactics_h[int(tactic_id)]
    else:
        for tactic in tactics_h.itervalues():
            print tactic

