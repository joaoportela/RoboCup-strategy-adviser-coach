#! /usr/bin/env python

import itertools
import datetime
from collections import namedtuple


Formation=namedtuple('Formation','default bysituation')
# TODO - find a way to make the generated file have a description or something
# like that.

# >>>setplays_by_group=[(10,29),(2,37),(43,45),(41,36)]
# >>>setplays_combinations=list(itertools.product(*setplays_by_group))
# [(10, 2, 43, 41), (10, 2, 43, 36), (10, 2, 45, 41), (10, 2, 45, 36), (10, 37, 43, 41), (10, 37, 43, 36), (10, 37, 45, 41), (10, 37, 45, 36), (29, 2, 43, 41), (29, 2, 43, 36), (29, 2, 45, 41), (29, 2, 45, 36), (29, 37, 43, 41), (29, 37, 43, 36), (29, 37, 45, 41), (29, 37, 45, 36)]

class Strategy(object):
    DATA = {
            "formation":
            [
                Formation('3', '3 3 13 13 8 9 4 9 5 10 6 11 7 12 1 1' ), # 433
                Formation('14', '14 14 24 24 19 20 15 20 16 21 17 22 18 23 1 1' ) # 442
                ]
            ,
            "setplans": range(1,16+1)
            }

    BASE_FILE="confs/base_strategy.conf.2D"

    def __init__(self):
        self.data=Strategy.DATA
        self.base_file=Strategy.BASE_FILE

    @property
    def tactics_count(self):
        if not hasattr(self, "_tactics_count"):
            self._tactics_count=reduce(lambda x,y: x*len(y), data.values(), 1)
        return self._tactics_count

    def _possible_tactics(self):
        data=self.data
        tactic_n=1
        for values in itertools.product(*data.itervalues()):
            t=Tactic()
            t["tactic_n"]=tactic_n
            for i, key in enumerate(data.iterkeys()):
                t[key] = values[i]
            yield t
            tactic_n+=1
        assert not hasattr(self, "_tactics_count") or self._tactics_count == (tactic_n-1)
        self._tactics_count=(tactic_n-1)

    def tactics(self):
        if not hasattr(self,"_possible_tactics_cache"):
            self._possible_tactics_cache=list(self._possible_tactics())

        assert (self.tactics_count == len(self._possible_tactics_cache))
        return self._possible_tactics_cache

    def itertactics(self):
        if not hasattr(self,"_possible_tactics_cache"):
            return self._possible_tactics()
        else:
            return self._possible_tactics_cache

    def tactics_as_str(self):
        return "\n".join([str(tactic).strip() for tactic in self.itertactics()])

    def write(self,outfile):
        with open(outfile, 'w') as fout:
            fout.write(str(self))

    def __str__(self):
        with open(self.base_file) as fin:
            str_=fin.read()
            str_=str_.replace("$TACTICS$", self.tactics_as_str())
            str_=str_.replace("$N_TACTICS$", str(self.tactics_count))
        return str_

class Tactic(dict):
    # TODO - formation by situation...
    TACTIC_STR="""{tactic_n} # Tactic {tactic_n} - Tactic Description
{formation.default} 3 {setplans}  0.7 0.3 0.0  0.5 0.5 # Formation, Flux, SetPlans, WFlux, WSafe, WEasy, WPass, WDrib
{formation.bysituation} # Form used in each situation (Att/Def, KickOff(O/T), CornKickIn, FKick, GFKick, GKick, IndFK, Pen"""

    def __str__(self):
        return Tactic.TACTIC_STR.format(**self)

if __name__ == '__main__':
    s=Strategy()
    print s
    match_duration=datetime.timedelta(minutes=12)
    n_runs = s.tactics_count * 3 * 10 # 3 oppoents 10 repetitions
    total_duration=match_duration*n_runs
    print "n_tactics", s.tactics_count
    print "n_runs", n_runs
    print "duration", total_duration
    print "finish @", datetime.datetime.now()+total_duration
    print "duration/7", total_duration/7
    print "finish @", datetime.datetime.now()+(total_duration/7)

    s.write("confs/generated_base_strategy.conf")

