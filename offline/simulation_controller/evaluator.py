#! /usr/bin/env python
"""
file where the base evaluator is defined and several other
evaluators are also defined. the evaluators are not to be used
intermixedly(?) (due to the return values not beeing in the same scale)
"""

import xml.dom.minidom as minidom
import statistics as st

class BaseEvaluator(object):
    def __init__(self, statistics_file, myside="left"):
        assert myside == "left" or myside == "right", "%s is invalid" % (myside,)
        self._myside=myside
        self._dom = minidom.parse(statistics_file)

    def value():
        raise NotImplementedError( "Must implement the 'value' method")

    def change_side(self,side):
        self._myside=myside

    def switch_match(self, statistics_file):
        self._dom = minidom.parse(statistics_file)

    def myside(self):
        myside=self._myside
        assert myside == "left" or myside == "right", "%s is invalid" % (myside,)
        return myside

    def opponent(self):
        myside = self.myside()
        assert myside == "left" or myside == "right", "%s is invalid" % (myside,)
        if myside == "left":
            return "right"
        if myside == "right":
            return "left"

    @staticmethod
    def sideid(side):
        assert side == "left" or side == "right", "%s is invalid" % (side,)
        if side == "left":
            return "LEFT_SIDE"
        if side == "right":
            return "RIGHT_SIDE"

    def mysideid(self):
        BaseEvaluator.sideid(self.myside())

    def opponentid(self):
        BaseEvaluator.sideid(self.opponent())

    # most functions from here on are just wrappers
    # the functions in the 'statistics' module
    def goals(self):
        return st.goals(self.myside(),self._dom)[0][1]

    def goalssuffered(self):
        return goalssuffered(self.myside(),self._dom)[0][1]


class BasicEvaluator(BaseEvaluator):
    """ evaluates a team performance in a match similar to the soccer rules (0
    - loss, 1 - draw, 3 - victory) """
    def __init__(self, statistics_file, myside="left"):
        BaseEvaluator.__init__(self, statistics_file, myside)

    def value():
        diff = self.goals-self.goalssuffered()
        if diff > 0: # victory
            return 3
        elif diff < 0: # loss
            return 0
        else: # draw
            return 1

class GoalDifferenceEvaluator(BaseEvaluator):
    """ evaluates a team performance in a match based on the goal difference
    """
    def __init__(self, statistics_file, myside="left"):
        BaseEvaluator.__init__(self, statistics_file, myside)

    def value():
        return self.goals()-self.goalssuffered()

class MARSEvaluator(BaseEvaluator):
    """ evaluates a team performance in a match according to the rules
    discoreved using the MARS algorithm
    """
    def __init__(self, myside="left"):
        BaseEvaluator.__init__(self,myside)

class ReliefEvaluator(BaseEvaluator):
    """ evaluates a team performance in a match according to the rules
    discoreved using the Relief algorithm
    """
    def __init__(self, myside="left"):
        BaseEvaluator.__init__(self,myside)

class MixedEvalutator(BaseEvaluator):
    """ evaluates a team performance in a match using by mixing the results of
    other evaluators
    """
    def __init__(self, myside="left"):
        BaseEvaluator.__init__(self,myside)

