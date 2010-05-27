#! /usr/bin/env python
"""file where the base evaluator is defined and several other
evaluators are also defined. the evaluators are not to be used
intermixedly(?) (due to the return values not beeing in the same scale)
"""

__all__ = ["PointsEvaluator", "GoalDifferenceEvaluator", "ReliefEvaluator",
        "MARSEvaluator"]

from utils import *

class EvaluatorError(Exception):
    def __init__(self, msg='Unspecified'):
        Exception.__init__(self, msg)

class BaseEvaluator(object):
    def __init__(self, statistics, myteam):
        self.statistics=statistics
        self.statistics.team = myteam

    def value():
        raise NotImplementedError( "Must implement the 'value' method")

class PointsEvaluator(BaseEvaluator):
    """ evaluates a team performance in a match similar to the soccer rules (0
    - loss, 1 - draw, 3 - victory)
    """
    def __init__(self, statistics, myteam):
        BaseEvaluator.__init__(self, statistics, myteam)

    @staticmethod
    def _match_points(s):
        diff = s.goals()-s.goalssuffered()
        if diff > 0: # victory
            return 3
        elif diff < 0: # loss
            return 0
        else: # draw
            return 1

    def value(self):
        # if it is a statistics agregator do the average of points per match.
        if hasattr(self.statistics,"statistics"):
            ss=self.statistics.statistics
            return average([PointsEvaluator._match_points(s) for s in ss])
        else:
            return PointsEvaluator._match_points(self.statistics)

class GoalDifferenceEvaluator(BaseEvaluator):
    """ evaluates a team performance in a match based on the goal difference
    """
    def __init__(self, statistics_file, myteam):
        BaseEvaluator.__init__(self, statistics_file, myteam)

    def value(self):
        s=self.statistics
        return s.goals()-s.goalssuffered()

class ReliefEvaluator(BaseEvaluator):
    """ evaluates a team performance in a match according to the evaluation
    function discoreved using the Relief algorithm
    """
    def __init__(self, statistics, myteam):
        BaseEvaluator.__init__(self, statistics, myteam)
        # the evaluation function was calculated with a strange zone definition
        self.statistics.strange_zones=True

    def value(self):
        """The function:

        0.1085678173*1-GoodPassTot+ 0.0333793177*1-GoodDef+
        0.0796453572*2-GoodPassTot+ 0.0128294878*2-GoodDef+
        0.0083693729*2-GoodOff+ 0.0274159181*1-BadDefDefensive+
        0.0708654848*2-BadPassTot+ 0.0448306355*2-BadDef+
        0.0577098364*1-Shoot+ 0.0481160484*1-IntShoot+
        0.0018519818*1-ShootTarget+ 0.0098023028*2-Shoot+
        0.0099133630*2-IntShoot+ 0.0828931087*2-ShootTarget+
        0.2604277820* GoalsTot+ 0.1799456998*2-Goals+
        0.0273404987* PenBoxBack+ 0.2173460194* PenArea+
        0.0976643308* OutPenArea+ 0.1052917803*1-Corner+
        0.0734638367*1-ThrowIn+ 0.0593205483*2-Corner+
        0.0309027898*2-OffInt+ 0.0064832968* BroAtt+ 0.0173590182* MedAtt+
        0.0845213352* AttTot+ 0.0091394370* 2-LeftBposs-Def+
        0.0761864554*3-LeftBposs-Attack+ 0.0366654265* 2-MiddBposs-Def+
        0.0458992625*4-MiddBposs-Attack+0.0759206186*1-RightBposs-Def+
        0.0009476703*2-RightBposs-Def+ 0.0599595746* GoalsOpp
        """
        first_half_passes=self.statistics.passes(half=1)
        first_half_defensive_passes=self.statistics.passes(half=1,offensive=False)

        second_half_passes=self.statistics.passes(half=2)
        second_half_defensive_passes=self.statistics.passes(half=2,offensive=False)

        second_half_offensive_passes=self.statistics.passes(half=2,offensive=True)
        first_half_defensive_passmisses=self.statistics.passmisses(half=1,offensive=False)

        second_half_passmisses=self.statistics.passmisses(half=2)
        second_half_defensive_passmisses=self.statistics.passmisses(half=2,offensive=False)

        first_half_goalmiss_faroutside=self.statistics.goalmisses(half=1,misstype="FAR_OUTSIDE")
        first_half_goalmiss_intercepted=self.statistics.goalmisses(half=1,misstype="GOALIE_CATCHED")

        first_half_goalmiss_outside=self.statistics.goalmisses(half=1,misstype="OUTSIDE")
        second_half_goalmiss_faroutside=self.statistics.goalmisses(half=2,misstype="FAR_OUTSIDE")

        second_half_goalmiss_intercepted=self.statistics.goalmisses(half=2,misstype="GOALIE_CATCHED")
        second_half_goalmiss_outside=self.statistics.goalmisses(half=2,misstype="OUTSIDE")

        total_goals=self.statistics.goals()
        second_half_goals=self.statistics.goals(half=2)

        # penalty_area -> grande area; goal_area -> pequena area
        penalty_area_goals=self.statistics.goals(kick_area="PENALTY_AREA")
        goal_area_goals=self.statistics.goals(kick_area="GOAL_AREA")

        outside_penalty_area_goals=self.statistics.goals(kick_area="FAR_SHOT")
        first_half_corners=self.statistics.corners(half=1)

        first_half_kicksin=self.statistics.kicks_in(half=1)
        second_half_corners=self.statistics.corners(half=2)

        second_half_passmisses_offside=self.statistics.passmisses(half=2,receiver_offside=True)
        broken_attacks=self.statistics.attacks(attacktype="BROKEN")
        medium_attacks=self.statistics.attacks(attacktype="MEDIUM")

        total_attacks=self.statistics.attacks()
        leftwing_2ndquarter_possession=self.statistics.ballpossession(zone="leftwing_2ndquarter")

        leftwing_3rdquarter_possession=self.statistics.ballpossession(zone="leftwing_3rdquarter")
        middlewing_2ndquarter_possession=self.statistics.ballpossession(zone="middlewing_2ndquarter")

        middlewing_4thquarter_possession=self.statistics.ballpossession(zone="middlewing_4thquarter")
        rightwing_1stquarter_possession=self.statistics.ballpossession(zone="rightwing_1stquarter")

        rightwing_2ndquarter_possession=self.statistics.ballpossession(zone="rightwing_2ndquarter")
        total_goal_opportunities=self.statistics.goalopportunities()

        # I think someone might cry when they see this line...
        # I personally think it is disturbing but works :)
        return (0.1085678173*first_half_passes+
                0.0333793177*first_half_defensive_passes+
                0.0796453572*second_half_passes+
                0.0128294878*second_half_defensive_passes+
                0.0083693729*second_half_offensive_passes+
                0.0274159181*first_half_defensive_passmisses+
                0.0708654848*second_half_passmisses+
                0.0448306355*second_half_defensive_passmisses+
                0.0577098364*first_half_goalmiss_faroutside+
                0.0481160484*first_half_goalmiss_intercepted+
                0.0018519818*first_half_goalmiss_outside+
                0.0098023028*second_half_goalmiss_faroutside+
                0.0099133630*second_half_goalmiss_intercepted+
                0.0828931087*second_half_goalmiss_outside+
                0.2604277820*total_goals+
                0.1799456998*second_half_goals+
                0.0273404987*penalty_area_goals+
                0.2173460194*goal_area_goals+
                0.0976643308*outside_penalty_area_goals+
                0.1052917803*first_half_corners+
                0.0734638367*first_half_kicksin+
                0.0593205483*second_half_corners+
                0.0309027898*second_half_passmisses_offside+
                0.0064832968*broken_attacks+
                0.0173590182*medium_attacks+
                0.0845213352*total_attacks+
                0.0091394370*leftwing_2ndquarter_possession+
                0.0761864554*leftwing_3rdquarter_possession+
                0.0366654265*middlewing_2ndquarter_possession+
                0.0458992625*middlewing_4thquarter_possession+
                0.0759206186*rightwing_1stquarter_possession+
                0.0009476703*rightwing_2ndquarter_possession+
                0.0599595746*total_goal_opportunities)

class MARSEvaluator(BaseEvaluator):
    """ evaluates a team performance in a match according to the evaluation
    function discoreved using the MARS (Multivariate Additive Regression
    Splines) algorithm
    """
    def __init__(self, statistics, myteam):
        BaseEvaluator.__init__(self, statistics, myteam)
        # the evaluation function was calculated with a strange zone definition
        self.statistics.strange_zones=True

    def value(self):
        """the function:

        8.38818
        +    1.644371 * pmax(0,               2007 -                Ano)
        -   0.3069785 * pmax(0,    `1-GoodPassTot` -                 28)
        +   0.5093597 * pmax(0,        `1-GoodDef` -                 45)
        -   0.2746145 * pmax(0,                 45 -        `1-GoodDef`)
        +   0.4722099 * pmax(0,        `1-GoodOff` -                 13)
        -   0.1705543 * pmax(0,        `1-GoodOff` -                 21)
        -  0.05941206 * pmax(0,        `2-GoodDef` -                 22)
        +   0.1417468 * pmax(0,                 38 -     `1-BadPassTot`)
        +   0.5710572 * pmax(0,     `2-BadPassTot` -                 38)
        -   0.5607114 * pmax(0,         `2-BadDef` -                 23)
        -   0.7210807 * pmax(0,                  1 -       `2-IntShoot`)
        +    1.087935 * pmax(0,           GoalsTot -                  2)
        -    1.516579 * pmax(0,                  2 -           GoalsTot)
        -   0.2664058 * pmax(0,                  6 -        `1-ThrowIn`)
        -    1.154919 * pmax(0,                  1 -         `1-OffInt`)
        -    1.654616 * pmax(0,                  2 -             FasAtt)
        -   0.6579848 * pmax(0,                  6 -             AttTot)
        -    11.67972 * pmax(0,          0.1489362 -  `1-LeftBposs-Def`)
        -    41.80000 * pmax(0,          0.1439394 -  `2-LeftBposs-Def`)
        -    29.29008 * pmax(0,          0.1794171 -  `2-MiddBposs-Def`)
        +    3.418717 * pmax(0,           0.564728 - `2-RightBposs-Def`)
        """

        first_half_passes=self.statistics.passes(half=1)
        first_half_defensive_passes=self.statistics.passes(half=1,offensive=False)
        first_half_offensive_passes=self.statistics.passes(half=1,offensive=True)
        second_half_defensive_passes=self.statistics.passes(half=2,offensive=False)
        first_half_passmisses=self.statistics.passmisses(half=1)
        second_half_passmisses=self.statistics.passmisses(half=2)
        second_half_defensive_passmisses=self.statistics.passmisses(half=2,offensive=False)
        second_half_goalmiss_intercepted=self.statistics.goalmisses(half=2,misstype="GOALIE_CATCHED")
        total_goals=self.statistics.goals()
        first_half_kicksin=self.statistics.kicks_in(half=1)
        first_half_passmisses_offside=self.statistics.passmisses(half=1,receiver_offside=True)
        fast_attacks=self.statistics.attacks(attacktype="FAST")
        total_attacks=self.statistics.attacks()
        leftwing_1stquarter_possession=self.statistics.ballpossession(zone="leftwing_1stquarter")
        leftwing_2ndquarter_possession=self.statistics.ballpossession(zone="leftwing_2ndquarter")
        middlewing_2ndquarter_possession=self.statistics.ballpossession(zone="middlewing_2ndquarter")
        rightwing_2ndquarter_possession=self.statistics.ballpossession(zone="rightwing_2ndquarter")

        return (8.38818
                - 0.3069785 * max(0, first_half_passes - 28)
                + 0.5093597 * max(0, first_half_defensive_passes - 45)
                - 0.2746145 * max(0, 45 - first_half_defensive_passes)
                + 0.4722099 * max(0, first_half_offensive_passes - 13)
                - 0.1705543 * max(0, first_half_offensive_passes - 21)
                - 0.05941206 * max(0, second_half_defensive_passes - 22)
                + 0.1417468 * max(0, 38 - first_half_passmisses)
                + 0.5710572 * max(0, second_half_passmisses - 38)
                - 0.5607114 * max(0, second_half_defensive_passmisses - 23)
                - 0.7210807 * max(0, 1 - second_half_goalmiss_intercepted)
                + 1.087935 * max(0, total_goals - 2)
                - 1.516579 * max(0, 2 - total_goals)
                - 0.2664058 * max(0, 6 - first_half_kicksin)
                - 1.154919 * max(0, 1 - first_half_passmisses_offside)
                - 1.654616 * max(0, 2 - fast_attacks)
                - 0.6579848 * max(0, 6 - total_attacks)
                - 11.67972 * max(0, 0.1489362 - leftwing_1stquarter_possession)
                - 41.80000 * max(0, 0.1439394 - leftwing_2ndquarter_possession)
                - 29.29008 * max(0, 0.1794171 - middlewing_2ndquarter_possession)
                + 3.418717 * max(0, 0.564728 - rightwing_2ndquarter_possession) )

class MixedEvalutator(BaseEvaluator):
    """ evaluates a team performance in a match using by mixing the results of
    other evaluators
    """
    def __init__(self, myteam):
        BaseEvaluator.__init__(self,myteam)
        # the evaluation function was calculated with a strange zone definition
        self.statistics.strange_zones=True

