#! /usr/bin/env python

import itertools
import datetime


class Strategy(object):
    DATA = {
            "formation":
            [
                14, # 415
                15, # 433
                16, # 442 Rhombus
                17  # 541
                ]
            ,
            "weights": # WFlux, WSafe, WEasy
            [
                "0.5 0.3 0.2",
                "0.8 0.0 0.2",
                "0.0 0.8 0.2",
                "0.4 0.4 0.2"
                ]
            ,
            "flux":
            [
                3, # middle_flux.conf - flux that mostly uses the middle of the field.
                4, # wings_flux.conf - flux that mostly uses the wings of the field.
                5  # opponent_area_focus.conf - flux that has very high values on the opponent area when compared with the rest.
                ]
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
{formation} {flux} 1   {weights} # Formation, Flux, SetPlans, WFlux, WSafe, WEasy
{formation} {formation} {formation} {formation} {formation} {formation} {formation} {formation} {formation} {formation} {formation} {formation} {formation} {formation} {formation} {formation} # Form used in each situation (Att/Def, KickOff(O/T), CornKickIn, FKick, GFKick, GKick, IndFK, Pen"""
    TACTIC_STR_old="""{tactic_n} # Tactic {tactic_n} - Tactic Description
{formation} {flux} 1   {weights} # Formation, Flux, SetPlans, WFlux, WSafe, WEasy
0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 # Form used in each situation (Att/Def, KickOff(O/T), CornKickIn, FKick, GFKick, GKick, IndFK, Pen"""

    def __str__(self):
        return Tactic.TACTIC_STR.format(**self)

if __name__ == '__main__':
    s=Strategy()
    print s
    match_duration=datetime.timedelta(minutes=12)
    n_runs = s.tactics_count * 4 * 10
    total_duration=match_duration*n_runs
    print "n_runs", n_runs
    print "duration", total_duration
    print "finish @", datetime.datetime.now()+total_duration
    print "duration/4", total_duration/4
    print "finish @", datetime.datetime.now()+(total_duration/4)

    s.write("confs/generated_base_strategy.conf")

