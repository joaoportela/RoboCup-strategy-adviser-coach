package soccerscope.util.analyze;

import java.text.NumberFormat;
import java.util.ArrayList;

import soccerscope.model.GameEvent;
import soccerscope.model.Scene;
import soccerscope.model.Team;
import soccerscope.model.WorldModel;
import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Rectangle2f;

import com.jamesmurty.utils.XMLBuilder;

public class BallPossessionByZonesAnalyzer extends SceneAnalyzer implements
AnalyzeNow, Xmling {

	public static String NAME = "Ball Possession By Zone";

	private int totalTime;
	private ArrayList<Zone> zones;
	// private ArrayList<Zone> validationzones;

	public class Zone implements Xmling {
		Rectangle2f area;
		public int countLeft;
		public int countRight;
		public int countNeutral;
		int totalTime;

		String name;

		public Zone(String name, Rectangle2f a) {
			this.area = a;
			this.name = name;
			this.countLeft = 0;
			this.countRight = 0;
			this.countNeutral = 0;
			this.totalTime = 0;
		}

		public boolean contains(Point2f p) {
			return this.area.contains(p);
		}

		public float leftPercent() {
			if (this.totalCount() <= 0) {
				return 0f;
			}
			return ((float) this.countLeft / (float) this.totalCount()) * 100f;
		}

		public float rightPercent() {
			if (this.totalCount() <= 0) {
				return 0f;
			}
			return ((float) this.countRight / (float) this.totalCount()) * 100f;
		}

		public float neutralPercent() {
			if (this.totalCount() <= 0) {
				return 0f;
			}
			return ((float) this.countNeutral / (float) this.totalCount()) * 100f;
		}

		public float totalPercent() {
			if (this.totalTime <= 0) {
				System.err.println("totalTime is zero!!");
				return 0f;
			}
			return ((float) this.totalCount() / (float) this.totalTime) * 100f;
		}

		public int totalCount() {
			return (this.countLeft + this.countRight + this.countNeutral);
		}

		@Override
		public void xmlElement(XMLBuilder builder) {
			XMLBuilder zoneElem = builder.elem("zone").attr("name", this.name)
			.attr("time", String.valueOf(this.totalCount())).attr(
					"percent", String.valueOf(this.totalPercent()));
			zoneElem.elem("possession").attr("team", Team.name(Team.LEFT_SIDE))
			.attr("percent", String.valueOf(this.leftPercent())).attr(
					"time", String.valueOf(this.countLeft));
			zoneElem.elem("possession")
			.attr("team", Team.name(Team.RIGHT_SIDE)).attr("percent",
					String.valueOf(this.rightPercent())).attr("time",
							String.valueOf(this.countRight));
			zoneElem.elem("possession").attr("team", Team.name(Team.NEUTRAL))
			.attr("percent", String.valueOf(this.neutralPercent()))
			.attr("time", String.valueOf(this.countNeutral));
		}

	}

	@Override
	public void init() {
		super.init();
		this.initZones();
		//this.initValidationZones();
	}

	//	private void initValidationZones(){
	//		this.validationzones = new ArrayList<Zone>(12);
	//
	//		// LEFTFIELD
	//		this.validationzones.add(new Zone("TopLeftleft", new Rectangle2f(new Point2f(
	//				-52.5f, -34.00f), 26.25f, 22.67f)));
	//		this.validationzones.add(new Zone("TopLeftright", new Rectangle2f(new Point2f(
	//				-26.25f, -34.00f), 26.25f, 22.67f)));
	//		this.validationzones.add(new Zone("MiddleLeftleft", new Rectangle2f(new Point2f(
	//				-52.5f, -11.33f), 26.25f, 22.67f)));
	//		this.validationzones.add(new Zone("MiddleLeftright", new Rectangle2f(new Point2f(
	//				-26.25f, -11.33f), 26.25f, 22.67f)));
	//		this.validationzones.add(new Zone("BottomLeftleft", new Rectangle2f(new Point2f(
	//				-52.5f, 11.33f), 26.25f, 22.67f)));
	//		this.validationzones.add(new Zone("BottomLeftright", new Rectangle2f(new Point2f(
	//				-26.25f, 11.33f), 26.25f, 22.67f)));
	//
	//		// RIGHTFIELD
	//		this.validationzones.add(new Zone("TopRightleft", new Rectangle2f(new Point2f(
	//				0.0f, -34.00f), 26.25f, 22.67f)));
	//		this.validationzones.add(new Zone("TopRightright", new Rectangle2f(new Point2f(
	//				26.25f, -34.00f), 26.25f, 22.67f)));
	//		this.validationzones.add(new Zone("MiddleRightleft", new Rectangle2f(new Point2f(
	//				0.0f, -11.33f), 26.25f, 22.67f)));
	//		this.validationzones.add(new Zone("MiddleRightright", new Rectangle2f(
	//				new Point2f(26.25f, -11.33f), 26.25f, 22.67f)));
	//		this.validationzones.add(new Zone("BottomRightleft", new Rectangle2f(new Point2f(
	//				0.0f, 11.33f), 26.25f, 22.67f)));
	//		this.validationzones.add(new Zone("BottomRightright", new Rectangle2f(
	//				new Point2f(26.25f, 11.33f), 26.25f, 22.67f)));
	//
	//		// OUTSIDES
	//		this.validationzones.add(new Zone("OutsideLeft", new Rectangle2f(new Point2f(
	//				-54.00f, -35.5f), 1.49f, 68.00f)));
	//		this.validationzones.add(new Zone("OutsideRight", new Rectangle2f(new Point2f(
	//				52.51f, -35.5f), 1.49f, 68f)));
	//		this.validationzones.add(new Zone("OutsideTop", new Rectangle2f(new Point2f(
	//				-52.5f, -35.5f), 105.00f, 1.49f)));
	//		this.validationzones.add(new Zone("OutsideBottom", new Rectangle2f(new Point2f(
	//				-52.5f, 35.5f), 105.00f, -1.49f)));
	//	}

	private void initZones() {
		this.zones = new ArrayList<Zone>(12);

		// LEFTFIELD
		this.zones.add(new Zone("TopLeftleft", new Rectangle2f(new Point2f(
				-52.5f, -34.00f), 26.25f, 22.67f)));
		this.zones.add(new Zone("TopLeftright", new Rectangle2f(new Point2f(
				-26.25f, -34.00f), 26.25f, 22.67f)));
		this.zones.add(new Zone("MiddleLeftleft", new Rectangle2f(new Point2f(
				-52.5f, -11.33f), 26.25f, 22.67f)));
		this.zones.add(new Zone("MiddleLeftright", new Rectangle2f(new Point2f(
				-26.25f, -11.33f), 26.25f, 22.67f)));
		this.zones.add(new Zone("BottomLeftleft", new Rectangle2f(new Point2f(
				-52.5f, 11.33f), 26.25f, 22.67f)));
		this.zones.add(new Zone("BottomLeftright", new Rectangle2f(new Point2f(
				-26.25f, 11.33f), 26.25f, 22.67f)));

		// RIGHTFIELD
		this.zones.add(new Zone("TopRightleft", new Rectangle2f(new Point2f(
				0.0f, -34.00f), 26.25f, 22.67f)));
		this.zones.add(new Zone("TopRightright", new Rectangle2f(new Point2f(
				26.25f, -34.00f), 26.25f, 22.67f)));
		this.zones.add(new Zone("MiddleRightleft", new Rectangle2f(new Point2f(
				0.0f, -11.33f), 26.25f, 22.67f)));
		this.zones.add(new Zone("MiddleRightright", new Rectangle2f(
				new Point2f(26.25f, -11.33f), 26.25f, 22.67f)));
		this.zones.add(new Zone("BottomRightleft", new Rectangle2f(new Point2f(
				0.0f, 11.33f), 26.25f, 22.67f)));
		this.zones.add(new Zone("BottomRightright", new Rectangle2f(
				new Point2f(26.25f, 11.33f), 26.25f, 22.67f)));

		// OUTSIDES
		this.zones.add(new Zone("OutsideLeft", new Rectangle2f(new Point2f(
				-54.00f, -35.5f), 1.49f, 68.00f)));
		this.zones.add(new Zone("OutsideRight", new Rectangle2f(new Point2f(
				52.51f, -35.5f), 1.49f, 68f)));
		this.zones.add(new Zone("OutsideTop", new Rectangle2f(new Point2f(
				-52.5f, -35.5f), 105.00f, 1.49f)));
		this.zones.add(new Zone("OutsideBottom", new Rectangle2f(new Point2f(
				-52.5f, 35.5f), 105.00f, -1.49f)));
	}

	/* START - cenas relacionadas com a tabela que não me importam para já. */
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Object getValueAt(int col, int fromTime, int toTime) {
		// ballPossesionbyRegion(fromTime, toTime);
		this.count(fromTime, toTime);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumIntegerDigits(2);
		nf.setMaximumFractionDigits(2);
		switch (col) {
		case ROW_NAME:
			return this.getName();
		case LEFT:
			if (this.totalTime == 0) {
				return "0";
			}
			return nf.format(this.lcount * 100.0 / this.totalTime);
		case RIGHT:
			if (this.totalTime == 0) {
				return "0";
			}
			return nf.format(this.rcount * 100.0 / this.totalTime);
		default:
			return " ";
		}
	}

	/* Ball possession count */
	@Override
	public void count(int fromTime, int toTime) {
		this.totalTime = 0;
		this.lcount = 0;
		this.rcount = 0;
		int pTable[] = PassAnalyzer.getPossessionTable();
		for (int i = fromTime; i <= toTime; i++) {
			if (i >= pTable.length) {
				continue;
			}
			switch (pTable[i]) {
			case PassAnalyzer.PLAY_OFF:
				break;
			case PassAnalyzer.PLAY_ON:
				this.totalTime++;
				break;
			case PassAnalyzer.LEFT_SIDE:
				this.totalTime++;
				this.lcount++;
				break;
			case PassAnalyzer.RIGHT_SIDE:
				this.totalTime++;
				this.rcount++;
				break;
			}
		}
	}

	/* END - cenas relacionadas com a tabela (...) */

	/* Calculation done by zone */
	public void ballPossesionbyRegion(int fromTime, int toTime) {
		int totalTime = 0;
		for (Zone z : this.zones) {
			z.countLeft = 0;
			z.countRight = 0;
		}
		boolean inzone = false;
		for (int i = fromTime; i <= toTime; i++) {
			Scene scene = WorldModel.getInstance().getSceneSet().getScene(i);
			Point2f b = scene.ball.pos;
			inzone = false;
			for (Zone z : this.zones) {
				if (z.contains(b)) {
					inzone = true;
					switch (PassAnalyzer.getPossessionTeam(i)) {
					case PassAnalyzer.LEFT_SIDE:
						//System.out.println("CICLE(" +i+ ") Team(LEFT_TEAM); zone(" + z.name + ")");
						z.countLeft++;
						totalTime++;
						break;
					case PassAnalyzer.RIGHT_SIDE:
						//System.out.println("CICLE(" +i+ ") Team(RIGHT_TEAM); zone(" + z.name + ")");
						z.countRight++;
						totalTime++;
						break;
					case PassAnalyzer.PLAY_ON:
						z.countNeutral++;
						totalTime++;
						//System.out.println("CICLE(" + i + ") Team(NONE); zone("+ z.name + ")");
						break;
					default:
						break;
					}
				}
			}
			//if (!inzone)
			//System.out.println("CICLE(" + i + ") Team(?); zone(UNKNOW)"+ "posicao bola:" + b);
		}

		for (Zone z : this.zones) {
			z.totalTime = totalTime;
		}
	}

	//	public void ballPossesionbyRegionValidation(int fromTime, int toTime) {
	//		int totalTime = 0;
	//		for (Zone z : this.validationzones) {
	//			z.countLeft = 0;
	//			z.countRight = 0;
	//		}
	//		boolean inzone = false;
	//		for (int i = fromTime; i <= toTime; i++) {
	//			Scene scene = WorldModel.getInstance().getSceneSet().getScene(i);
	//			Point2f b = scene.ball.pos;
	//			inzone = false;
	//			for (Zone z : this.validationzones) {
	//				if (z.contains(b)) {
	//					inzone = true;
	//					switch (PassAnalyzer.getPossessionTeam(i)) {
	//					case PassAnalyzer.LEFT_SIDE:
	//						//System.out.println("CICLE(" +i+ ") Team(LEFT_TEAM); zone(" + z.name + ")");
	//						z.countLeft++;
	//						totalTime++;
	//						break;
	//					case PassAnalyzer.RIGHT_SIDE:
	//						//System.out.println("CICLE(" +i+ ") Team(RIGHT_TEAM); zone(" + z.name + ")");
	//						z.countRight++;
	//						totalTime++;
	//						break;
	//					case PassAnalyzer.PLAY_ON:
	//						z.countNeutral++;
	//						totalTime++;
	//						//System.out.println("CICLE(" + i + ") Team(NONE); zone("+ z.name + ")");
	//						break;
	//					default:
	//						break;
	//					}
	//				}
	//			}
	//			//if (!inzone)
	//			//System.out.println("CICLE(" + i + ") Team(?); zone(UNKNOW)"+ "posicao bola:" + b);
	//		}
	//
	//		for (Zone z : this.validationzones) {
	//			z.totalTime = totalTime;
	//		}
	//	}

	public Zone getZone(String zone) {
		for (Zone z : this.zones) {
			if(z.name.equalsIgnoreCase(zone)) {
				return z;
			}
		}
		throw new AssertionError("invalid zone:" + zone);
	}

	// DUNNO, LOL!
	@Override
	public GameEvent analyze(Scene scene, Scene prev) {
		return null;
	}

	@Override
	public void analyzeNow() {
		int start = 0;
		int end = WorldModel.getInstance().getSceneSet().getLimitTime();

		// update the ball possession data
		this.ballPossesionbyRegion(start, end);
		// this.ballPossesionbyRegionValidation(746, 1346);
		this.count(start,end);
	}

	@Override
	public void xmlElement(XMLBuilder builder) {
		this.analyzeNow();

		builder = builder.elem("ballpossession")
		.attr("total", String.valueOf(this.totalTime))
		.attr("left", String.valueOf(this.lcount))
		.attr("right", String.valueOf(this.rcount))
		.attr("neutral", String.valueOf(this.totalTime-(this.rcount+this.lcount)));
		for (Zone z : this.zones) {
			z.xmlElement(builder);
		}

		//		builder = builder.elem("ballpossession_validation")
		//		.attr("total", String.valueOf(this.totalTime));
		//		for (Zone z : this.validationzones) {
		//			z.xmlElement(builder);
		//		}
	}

}
