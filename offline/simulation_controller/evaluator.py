#! /usr/bin/env python

__all__ = ["Evaluator"]

class Evaluator(object):
    def __init__(self, myside="left"):
        assert myside == "left" or myside == "right", "%s is invalid" % (myside,)
        self._myside=myside

    def myside(self):
        myside=self._myside
        assert myside == "left" or myside == "right", "%s is invalid" % (myside,)
        return myside

    def opponent(self):
        side = self.myside() 
        assert side == "left" or side == "right", "%s is invalid" % (side,)
        if side == "left":
            return "right"
        if side == "right":
            return "left"

class BasicEvaluator(Evaluator):
    pass

class MARSEvaluator(Evaluator):
    pass

class ReliefEvaluator(Evaluator):
    pass
