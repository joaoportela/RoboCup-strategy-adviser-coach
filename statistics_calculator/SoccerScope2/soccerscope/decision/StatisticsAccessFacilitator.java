package soccerscope.decision;

import java.util.List;

import soccerscope.model.Team;
import soccerscope.util.GameAnalyzer;
import soccerscope.util.analyze.AnalyzeNow;
import soccerscope.util.analyze.BallPossessionByZonesAnalyzer;
import soccerscope.util.analyze.GoalKickAnalyzer;
import soccerscope.util.analyze.PassChainAnalyzer;
import soccerscope.util.analyze.SceneAnalyzer;

public class StatisticsAccessFacilitator {
	public static final int GAMETIME = 6000;

	public static enum ZoneEnum {
		leftwing_1stquarter, leftwing_2ndquarter, middlewing_1stquarter, middlewing_2ndquarter, rightwing_1stquarter, rightwing_2ndquarter, leftwing_3rdquarter, leftwing_4thquarter, middlewing_3rdquarter, middlewing_4thquarter, rightwing_3rdquarter, rightwing_4thquarter;

		public String toAbsoluteNamingString(int side) {
			assert (side == Team.LEFT_SIDE || side == Team.RIGHT_SIDE);
			if (side == Team.LEFT_SIDE) {
				switch (this) {
				case leftwing_1stquarter:
					return "TopLeftleft";
				case leftwing_2ndquarter:
					return "TopLeftright";
				case middlewing_1stquarter:
					return "MiddleLeftleft";
				case middlewing_2ndquarter:
					return "MiddleLeftright";
				case rightwing_1stquarter:
					return "BottomLeftleft";
				case rightwing_2ndquarter:
					return "BottomLeftright";
				case leftwing_3rdquarter:
					return "TopRightleft";
				case leftwing_4thquarter:
					return "TopRightright";
				case middlewing_3rdquarter:
					return "MiddleRightleft";
				case middlewing_4thquarter:
					return "MiddleRightright";
				case rightwing_3rdquarter:
					return "BottomRightleft";
				case rightwing_4thquarter:
					return "BottomRightright";
				}

			} else {
				// RIGHT_SIDE
				switch (this) {
				case rightwing_4thquarter:
					return "TopLeftleft";
				case rightwing_3rdquarter:
					return "TopLeftright";
				case middlewing_4thquarter:
					return "MiddleLeftleft";
				case middlewing_3rdquarter:
					return "MiddleLeftright";
				case leftwing_4thquarter:
					return "BottomLeftleft";
				case leftwing_3rdquarter:
					return "BottomLeftright";
				case rightwing_2ndquarter:
					return "TopRightleft";
				case rightwing_1stquarter:
					return "TopRightright";
				case middlewing_2ndquarter:
					return "MiddleRightleft";
				case middlewing_1stquarter:
					return "MiddleRightright";
				case leftwing_2ndquarter:
					return "BottomRightleft";
				case leftwing_1stquarter:
					return "BottomRightright";
				}
			}
			return null;
		}
	}

	// private static class AnalyzerMissing extends Exception {
	// /**
	// *
	// */
	// private static final long serialVersionUID = 1L;
	//
	// public AnalyzerMissing(Class<?> c) {
	// super("Analyzer " + c.getCanonicalName() + " not found");
	// }
	// }

	private List<SceneAnalyzer> analyzers;
	private int side;
	private int time;

	public StatisticsAccessFacilitator(List<SceneAnalyzer> analyzerList,
			int side, int time) {
		this.analyzers = analyzerList;
		this.side = side;
		assert (side == Team.RIGHT_SIDE || side == Team.LEFT_SIDE);
		this.time = time;
		for (final SceneAnalyzer analyzer : analyzerList) {
			// inform the analyzers that analysis will occur, now!
			if (AnalyzeNow.class.isInstance(analyzer)) {
				((AnalyzeNow) analyzer).analyzeNow();
			}
		}
	}

	private SceneAnalyzer getAnalyzer(Class<?> c) {
		for (final SceneAnalyzer analyzer : GameAnalyzer.analyzerList) {
			if (c.isInstance(analyzer)) {
				return analyzer;
			}
		}
		assert false : "analyzer" + c.getCanonicalName()
		+ " missing, this should never happen!";
		return null;
	}

	private static float scale(int n, int time) {
		return (((float) n / (float) time) * GAMETIME);
	}

	public float passChainsScaled() {
		return StatisticsAccessFacilitator.scale(this.passChains(), this.time);
	}

	public int passChains() {
		PassChainAnalyzer pc = (PassChainAnalyzer) this
		.getAnalyzer(PassChainAnalyzer.class);
		return pc.nChains(this.side);
	}

	public float goalkicksScaled() {
		return StatisticsAccessFacilitator.scale(this.goalkicks(), this.time);
	}

	public int goalkicks() {
		GoalKickAnalyzer gc = (GoalKickAnalyzer) this
		.getAnalyzer(GoalKickAnalyzer.class);
		return gc.getCount(this.side);
	}

	// TODO - ballpossessionbyzones (hard work!)
	public int ballPossession(ZoneEnum zone) {
		BallPossessionByZonesAnalyzer bpbz = (BallPossessionByZonesAnalyzer) this
		.getAnalyzer(BallPossessionByZonesAnalyzer.class);
		BallPossessionByZonesAnalyzer.Zone z = bpbz.getZone(zone.toAbsoluteNamingString(this.side));
		if(this.side == Team.LEFT_SIDE) {
			return z.countLeft;
		} else {
			// RIGHT_SIDE
			return z.countRight;
		}

	}
}
