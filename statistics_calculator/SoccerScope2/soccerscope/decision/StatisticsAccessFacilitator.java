package soccerscope.decision;

import java.util.List;

import soccerscope.model.Team;
import soccerscope.util.analyze.AnalyzeNow;
import soccerscope.util.analyze.AttackAnalyzer;
import soccerscope.util.analyze.BallPossessionByZonesAnalyzer;
import soccerscope.util.analyze.GoalKickAnalyzer;
import soccerscope.util.analyze.PassChainAnalyzer;
import soccerscope.util.analyze.PassMissAnalyzer;
import soccerscope.util.analyze.SceneAnalyzer;

public class StatisticsAccessFacilitator {
	public static final int GAMETIME = 6000;

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
		for (final SceneAnalyzer analyzer : this.analyzers) {
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

	private int passChains() {
		PassChainAnalyzer pc = (PassChainAnalyzer) this
		.getAnalyzer(PassChainAnalyzer.class);
		return pc.nChains(this.side);
	}

	public float goalkicksScaled() {
		return StatisticsAccessFacilitator.scale(this.goalkicks(), this.time);
	}

	private int goalkicks() {
		GoalKickAnalyzer gc = (GoalKickAnalyzer) this
		.getAnalyzer(GoalKickAnalyzer.class);
		return gc.getCount(this.side);
	}

	public float attacksScaled() {
		return StatisticsAccessFacilitator.scale(this.attacks(), this.time);
	}

	private int attacks() {
		AttackAnalyzer at = (AttackAnalyzer) this
		.getAnalyzer(AttackAnalyzer.class);
		return at.getCount(this.side);
	}

	public float ballPossessionScaled(ZoneEnum zone) {
		return StatisticsAccessFacilitator.scale(this.ballPossession(zone),
				this.time);
	}

	private int ballPossession(ZoneEnum zone) {
		BallPossessionByZonesAnalyzer bpbz = (BallPossessionByZonesAnalyzer) this
		.getAnalyzer(BallPossessionByZonesAnalyzer.class);
		BallPossessionByZonesAnalyzer.Zone z = bpbz.getZone(zone
				.toAbsoluteNamingString(this.side));
		if (this.side == Team.LEFT_SIDE) {
			return z.countLeft;
		} else {
			// RIGHT_SIDE
			return z.countRight;
		}
	}

	public float passMissesScaled() {
		return StatisticsAccessFacilitator.scale(this.passMisses(), this.time);
	}

	private int passMisses() {
		PassMissAnalyzer pm = (PassMissAnalyzer) this
		.getAnalyzer(PassMissAnalyzer.class);
		return pm.nPassMiss(this.side);
	}

	public float passMissesOffsideScaled() {
		return StatisticsAccessFacilitator.scale(this.passMissesOffside(), this.time);
	}

	private int passMissesOffside() {
		PassMissAnalyzer pm = (PassMissAnalyzer) this
		.getAnalyzer(PassMissAnalyzer.class);
		return pm.nPassMissOffside(this.side);
	}

}
