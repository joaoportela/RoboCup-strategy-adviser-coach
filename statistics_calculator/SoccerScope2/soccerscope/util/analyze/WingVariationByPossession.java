package soccerscope.util.analyze;

import java.util.LinkedList;
import java.util.List;

import soccerscope.model.GameEvent;
import soccerscope.model.Scene;
import soccerscope.model.SceneSet;
import soccerscope.model.Team;
import soccerscope.model.WorldModel;
import soccerscope.util.analyze.WingVariationByPass.WING;

import com.jamesmurty.utils.XMLBuilder;

/*
 * calculates the wing variation according to the ball
 * position change.
 */
public class WingVariationByPossession extends SceneAnalyzer implements Xmling, AnalyzeNow {
	public static final String NAME = "Wing Variation (by possession)";

	@Override
	public GameEvent analyze(Scene scene, Scene prev) {
		// nothing to see here...
		return null;
	}

	@Override
	public String getName() {
		return NAME;
	}

	List<Variation> variations = new LinkedList<Variation>();

	@Override
	public void init() {
		this.variations.clear();
	}

	public static class Variation implements Xmling {
		int side;
		WING start;
		WING end;
		int stime;
		int etime;

		Variation(int side, WING start, WING end, int stime, int etime) {
			this.side = side;
			this.start = start;
			this.end = end;
			this.stime = stime;
			this.etime = etime;
		}

		public int middleTime() {
			return (int) ((this.stime) * 0.5f + (this.etime) * 0.5f);
		}

		public boolean valid() {
			if (this.start != null && this.end != null) {
				if (this.start != this.end) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void xmlElement(XMLBuilder builder) {
			builder.elem("wingchange")
			.attr("from", this.start.toString())
			.attr("to", this.end.toString())
			.attr("time",String.valueOf(this.stime))
			.attr("team", Team.name(this.side));
		}
	}

	@Override
	public Object getValueAt(int col, int fromTime, int toTime) {
		this.lcount = 0;
		this.rcount = 0;
		this.count(fromTime, toTime);
		switch (col) {
		case ROW_NAME:
			return this.getName();
		case LEFT:
			if (this.lcount == -1) {
				return "--";
			}
			return new Integer(this.lcount);
		case RIGHT:
			if (this.rcount == -1) {
				return "--";
			}
			return new Integer(this.rcount);
		default:
			return " ";
		}
	}

	@Override
	public void count(int fromTime, int toTime) {
		this.lcount = 0;
		this.rcount = 0;
		WING startWing = null;
		WING endWing = null;
		Scene prev_scene = null;
		Scene curr_scene = null;
		SceneSet scene_set = WorldModel.getInstance().getSceneSet();
		prev_scene = scene_set.getScene(fromTime);
		for (int sceneit = (fromTime + 1); sceneit < toTime; sceneit++) {
			curr_scene = WorldModel.getInstance().getSceneSet().getScene(
					sceneit);
			int prevside = PassAnalyzer.sideToStandardSide(PassAnalyzer
					.getPossessionTeam(prev_scene.time));
			int side = PassAnalyzer.sideToStandardSide(PassAnalyzer
					.getPossessionTeam(curr_scene.time));
			// if the ball possession is the same...
			if (prevside == side
					&& (side == Team.LEFT_SIDE || side == Team.RIGHT_SIDE)) {
				startWing = WingVariationByPass.whichWing(prev_scene.ball.pos);
				endWing = WingVariationByPass.whichWing(curr_scene.ball.pos);
				Variation v = new Variation(side, startWing, endWing,
						prev_scene.time, curr_scene.time);

				if (v.valid()) {
					// update the counts and stuff...
					this.countUp(v);
					String sideName = Team.name(side);
					System.out.println("WING_CHANGE (ball possesion) "
							+ sideName + " " + startWing + "("
							+ prev_scene.time + ") " + endWing + "("
							+ curr_scene.time + ")");
				}
			}
			// update the previous scene...
			prev_scene = curr_scene;
		}
	}

	private void countUp(Variation v) {
		super.countUp(v.side, v.etime);
		this.variations.add(v);
	}

	@Override
	public void analyzeNow() {
		this.variations.clear();
		int stime = 0;
		int etime = WorldModel.getInstance().getSceneSet().getLimitTime();
		this.count(stime, etime);
	}

	@Override
	public void xmlElement(XMLBuilder builder) {
		XMLBuilder wv = builder.elem("wingchange_bypossession").attr("left",
				String.valueOf(this.lcount)).attr("right", String.valueOf(this.rcount));

		for (Variation v : this.variations) {
			v.xmlElement(wv);
		}

	}

}
