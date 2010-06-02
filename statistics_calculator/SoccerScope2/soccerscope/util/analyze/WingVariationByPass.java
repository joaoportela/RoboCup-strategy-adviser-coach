package soccerscope.util.analyze;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import soccerscope.model.GameEvent;
import soccerscope.model.Scene;
import soccerscope.model.Team;
import soccerscope.model.WorldModel;
import soccerscope.util.analyze.PassAnalyzer.Kicker;
import soccerscope.util.analyze.PassAnalyzer.Pass;
import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Rectangle2f;

import com.jamesmurty.utils.XMLBuilder;

public class WingVariationByPass extends SceneAnalyzer implements Xmling, AnalyzeNow {

	public static final String NAME = "Wing variation By Pass";

	/*
	 * Zone definition
	 */
	public static class Zone {
		Rectangle2f area;

		public Zone(Rectangle2f a) {
			this.area = a;
		}

		public boolean contains(Point2f p) {
			return this.area.contains(p);
		}
	}

	public static class WingChangePass implements Xmling {
		WING senderWing;
		WING receiverWing;
		Pass pass;
		WING senderWing2;
		WING receiverWing2;
		Pass pass2;
		boolean valid = false;
		int side;
		int time;

		public WingChangePass(Pass pass) {
			this.pass = pass;
			this.side = pass.side;
			this.time = pass.receiver.time;
			Point2f sender_pos = WingVariationByPass.kickerPosition(
					pass.sender, this.side);
			Point2f receiver_pos = WingVariationByPass.kickerPosition(
					pass.receiver, this.side);
			// check in which wings is each of the players...
			this.senderWing = WingVariationByPass.whichWing(sender_pos);
			this.receiverWing = WingVariationByPass.whichWing(receiver_pos);

			if (this.senderWing != null && this.receiverWing != null) {
				if (this.senderWing != this.receiverWing) {
					this.valid = true;
				}
			}
		}

		public void extend(WingChangePass wing) {
			this.pass2 = wing.pass;
			this.senderWing2 = wing.senderWing;
			this.receiverWing2 = wing.receiverWing;
		}

		public boolean valid() {
			return this.valid;
		}

		@Override
		public void xmlElement(XMLBuilder builder) {
			boolean offensive = false;
			if (this.pass2 != null) {
				offensive = this.pass2.inOffensiveField;
			} else {
				offensive = this.pass.inOffensiveField;
			}

			XMLBuilder wcXml = builder.elem("wingchange")
			.attr("offensive", String.valueOf(offensive))
			.attr("totalvariation",	String.valueOf(this.pass2 != null));
			XMLBuilder pass = wcXml.elem("pass").attr("team", Team.name(this.side));
			this.pass.sender.xmlElement(pass.elem("kick").attr("wing",
					this.senderWing.toString()));
			this.pass.receiver.xmlElement(pass.elem("reception").attr("wing",
					this.receiverWing.toString()));
			if (this.pass2 != null) {
				XMLBuilder pass2 = wcXml.elem("pass").attr("team",
						Team.name(this.side));
				this.pass2.sender.xmlElement(pass2.elem("kick").attr("wing",
						this.senderWing2.toString()));
				this.pass2.receiver.xmlElement(pass2.elem("reception").attr(
						"wing", this.receiverWing2.toString()));
			}
		}
	}

	// stores partial and complete wing variations.
	public List<WingChangePass> wingChanges = new LinkedList<WingChangePass>();

	private static final float WING_HEIGHT = 22.5f;

	private static final int MAX_INTERMISSION = 40;

	public static enum WING {
		TOP, BOTTOM, MIDDLE
	};

	public static EnumMap<WING, Zone> wings = WingVariationByPass.initWings();

	public static EnumMap<WING, Zone> initWings() {
		EnumMap<WING, Zone> wings = new EnumMap<WING, Zone>(WING.class);
		float middle_height = (34f * 2f) - (WING_HEIGHT * 2f);
		wings.put(WING.TOP, new Zone(new Rectangle2f(new Point2f(-52.5f, -34f),
				105f, WING_HEIGHT)));
		wings.put(WING.MIDDLE, new Zone(new Rectangle2f(new Point2f(-52.5f,
				-34f + WING_HEIGHT), 105f, middle_height)));
		wings.put(WING.BOTTOM, new Zone(new Rectangle2f(new Point2f(-52.5f,
				0f + middle_height), 105f, WING_HEIGHT)));
		return wings;
	}

	@Override
	public void init() {
		super.init();
		this.wingChanges.clear();
	}

	@Override
	public GameEvent analyze(Scene scene, Scene prev) {
		// DO NOTHING!
		return null;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Object getValueAt(int col, int fromTime, int toTime) {
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
		this.initCounts();
		this.countPartialVariation(fromTime, toTime);
		// total variation depends on partial variation.
		// does not need time interval because it will only use the
		// already analyzed variation.
		this.countTotalVariation();
	}

	private void initCounts() {
		this.lcount = 0;
		this.rcount = 0;
		this.wingChanges.clear();
	}

	public void countPartialVariation(int fromTime, int toTime) {
		WING senderWing = null;
		WING receiverWing = null;
		for (Pass pass : PassAnalyzer.getPassList()) {
			int stime = pass.sender.time;
			int etime = pass.receiver.time;
			// validate time frame
			if (fromTime <= stime && etime <= toTime) {
				WingChangePass wingChange = new WingChangePass(pass);

				if (wingChange.valid()) {
					// this pass corresponded to a change on the wing...
					this.countUp(wingChange);
					String sideName = Team.name(wingChange.side);
					System.out.println("WING_CHANGE (pass)" + sideName + " "
							+ senderWing + "(" + stime + ") " + receiverWing
							+ "(" + etime + ")");
				}
			} else {
				System.err.println("(1/2)players are not on the wings!"
						+ "stime(" + stime + ") etime(" + etime + ")");
			}
		}
	}

	private void countTotalVariation() {
		WingChangePass prev = null;
		ListIterator<WingChangePass> iter = this.wingChanges.listIterator();
		while (iter.hasNext()) {
			WingChangePass wing = iter.next();
			if (prev != null) {
				if (prev.pass2 != null) {
					System.err.println("this should never happen!!");
				} else if (wing.side == prev.side) {
					if (prev.receiverWing == WING.MIDDLE
							&& wing.senderWing == WING.MIDDLE) {
						if (wing.receiverWing != prev.senderWing) {
							// verificar que a posse de bola.
							int stime = prev.pass.receiver.time;
							int etime = wing.pass.sender.time;
							// the time between both passes was not too long.
							if ((etime - stime) <= WingVariationByPass.MAX_INTERMISSION) {
								// ball possession did not change.
								if (BallPossessionAnalyzer
										.checkPossessionForTeam(wing.side,
												stime, etime)) {
									prev.extend(wing);
									iter.remove();
								}
							}
						}
					}
				}
			}
			prev = wing;
		}
	}

	private void countUp(WingChangePass wingChangePass) {
		super.countUp(wingChangePass.side, wingChangePass.time);
		this.wingChanges.add(wingChangePass);
	}

	public static Point2f kickerPosition(Kicker k, int side) {
		Scene scene = WorldModel.getInstance().getSceneSet().getScene(k.time);
		int[] indexes = Team.firstAndLastPlayerIndexes(side);

		for (int i = indexes[0]; i < indexes[1]; i++) {
			if (k.unum == scene.player[i].unum) {
				return scene.player[i].pos;
			}
		}
		return null;
	}

	/**
	 * check to which wing corresponds the position pos
	 * 
	 * @param pos
	 *            position to test
	 * @return the corresponding WING, null on failure(probably outside the
	 *         field).
	 */
	public static WING whichWing(Point2f pos) {
		// check in which wings is each of the players...
		for (EnumMap.Entry<WING, Zone> entry : WingVariationByPass.wings
				.entrySet()) {
			if (entry.getValue().contains(pos)) {
				return entry.getKey();
			}
		}
		return null;
	}

	@Override
	public void analyzeNow() {
		this.wingChanges.clear();
		int stime = 0;
		int etime = WorldModel.getInstance().getSceneSet().getLimitTime();
		// update the data...
		this.count(stime, etime);
	}

	@Override
	public void xmlElement(XMLBuilder builder) {
		this.analyzeNow();

		XMLBuilder wv = builder.elem("wingchanges").attr("left",
				String.valueOf(this.lcount)).attr("right", String.valueOf(this.rcount));
		for (WingChangePass wc : this.wingChanges) {
			wc.xmlElement(wv);
		}
	}

}
