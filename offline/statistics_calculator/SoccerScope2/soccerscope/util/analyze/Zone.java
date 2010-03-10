package soccerscope.util.analyze;

import java.util.EnumMap;

import soccerscope.model.Team;
import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Rectangle2f;

enum DangerArea {
	LEFT, RIGHT, NONE;

	public int side() {
		switch (this) {
		case LEFT:
			return Team.LEFT_SIDE;
		case RIGHT:
			return Team.RIGHT_SIDE;
		}
		return Team.NEUTRAL;
	}
}

enum DangerRadius {
	LEFT, RIGHT, NONE;

	public int side() {
		switch (this) {
		case LEFT:
			return Team.LEFT_SIDE;
		case RIGHT:
			return Team.RIGHT_SIDE;
		}
		return Team.NEUTRAL;
	}
};

enum Wing {
	TOP, MIDDLE, BOTTOM, NONE
};

enum GoalZone {
	LEFTFIELD_GOALAREA, LEFTFIELD_PENALTYAREA, RIGHTFIELD_GOALAREA, RIGHTFIELD_PENALTYAREA, FAR_SHOT;

	public int side() {
		switch (this) {
		case LEFTFIELD_GOALAREA:
		case LEFTFIELD_PENALTYAREA:
			return Team.LEFT_SIDE;
		case RIGHTFIELD_GOALAREA:
		case RIGHTFIELD_PENALTYAREA:
			return Team.RIGHT_SIDE;
		}
		return Team.NEUTRAL;
	}
};

enum VerticalQuarter {
	LEFTFIELD_LEFTQUARTER, LEFTFIELD_RIGHTQUARTER, RIGHTFIELD_LEFTQUARTER, RIGHTFIELD_RIGHTQUARTER, NONE;

	public int side() {
		switch (this) {
		case LEFTFIELD_LEFTQUARTER:
		case LEFTFIELD_RIGHTQUARTER:
			return Team.LEFT_SIDE;
		case RIGHTFIELD_LEFTQUARTER:
		case RIGHTFIELD_RIGHTQUARTER:
			return Team.RIGHT_SIDE;
		}
		return Team.NEUTRAL;
	}

}

enum WingAndVerticalQuarter {
	TOP_LEFTFIELD_LEFTQUARTER, TOP_LEFTFIELD_RIGHTQUARTER, TOP_RIGHTFIELD_LEFTQUARTER, TOP_RIGHTFIELD_RIGHTQUARTER, MIDDLE_LEFTFIELD_LEFTQUARTER, MIDDLE_LEFTFIELD_RIGHTQUARTER, MIDDLE_RIGHTFIELD_LEFTQUARTER, MIDDLE_RIGHTFIELD_RIGHTQUARTER, BOTTOM_LEFTFIELD_LEFTQUARTER, BOTTOM_LEFTFIELD_RIGHTQUARTER, BOTTOM_RIGHTFIELD_LEFTQUARTER, BOTTOM_RIGHTFIELD_RIGHTQUARTER, NONE;

	public int side() {
		switch (this) {
		case TOP_LEFTFIELD_LEFTQUARTER:
		case TOP_LEFTFIELD_RIGHTQUARTER:
		case MIDDLE_LEFTFIELD_LEFTQUARTER:
		case MIDDLE_LEFTFIELD_RIGHTQUARTER:
		case BOTTOM_LEFTFIELD_LEFTQUARTER:
		case BOTTOM_LEFTFIELD_RIGHTQUARTER:
			return Team.LEFT_SIDE;
		case TOP_RIGHTFIELD_LEFTQUARTER:
		case TOP_RIGHTFIELD_RIGHTQUARTER:
		case MIDDLE_RIGHTFIELD_LEFTQUARTER:
		case MIDDLE_RIGHTFIELD_RIGHTQUARTER:
		case BOTTOM_RIGHTFIELD_LEFTQUARTER:
		case BOTTOM_RIGHTFIELD_RIGHTQUARTER:
			return Team.RIGHT_SIDE;
		}
		return Team.NEUTRAL;
	}

	public VerticalQuarter verticalQuarter() {
		switch (this) {
		case TOP_LEFTFIELD_LEFTQUARTER:
		case MIDDLE_LEFTFIELD_LEFTQUARTER:
		case BOTTOM_LEFTFIELD_LEFTQUARTER:
			return VerticalQuarter.LEFTFIELD_LEFTQUARTER;
		case TOP_LEFTFIELD_RIGHTQUARTER:
		case MIDDLE_LEFTFIELD_RIGHTQUARTER:
		case BOTTOM_LEFTFIELD_RIGHTQUARTER:
			return VerticalQuarter.LEFTFIELD_RIGHTQUARTER;
		case TOP_RIGHTFIELD_LEFTQUARTER:
		case MIDDLE_RIGHTFIELD_LEFTQUARTER:
		case BOTTOM_RIGHTFIELD_LEFTQUARTER:
			return VerticalQuarter.RIGHTFIELD_LEFTQUARTER;
		case TOP_RIGHTFIELD_RIGHTQUARTER:
		case MIDDLE_RIGHTFIELD_RIGHTQUARTER:
		case BOTTOM_RIGHTFIELD_RIGHTQUARTER:
			return VerticalQuarter.RIGHTFIELD_RIGHTQUARTER;

		}
		return VerticalQuarter.NONE;
	}

	public Wing wing() {
		switch (this) {
		case TOP_LEFTFIELD_LEFTQUARTER:
		case TOP_LEFTFIELD_RIGHTQUARTER:
		case TOP_RIGHTFIELD_LEFTQUARTER:
		case TOP_RIGHTFIELD_RIGHTQUARTER:
			return Wing.TOP;
		case MIDDLE_LEFTFIELD_LEFTQUARTER:
		case MIDDLE_LEFTFIELD_RIGHTQUARTER:
		case MIDDLE_RIGHTFIELD_LEFTQUARTER:
		case MIDDLE_RIGHTFIELD_RIGHTQUARTER:
			return Wing.MIDDLE;
		case BOTTOM_LEFTFIELD_LEFTQUARTER:
		case BOTTOM_LEFTFIELD_RIGHTQUARTER:
		case BOTTOM_RIGHTFIELD_LEFTQUARTER:
		case BOTTOM_RIGHTFIELD_RIGHTQUARTER:
			return Wing.BOTTOM;
		}
		return Wing.NONE;
	}
}

public abstract class Zone {

	private static class AreaZone extends Zone {
		private Rectangle2f area;

		public AreaZone(Rectangle2f area) {
			this.area = area;
		}

		@Override
		public boolean contains(Point2f pos) {
			return this.area.contains(pos);
		}
	}

	private static class RadiusZone {
		public float radius;
		public Point2f center;

		public RadiusZone(Point2f center, float radius) {
			this.radius = radius;
			this.center = center;
		}

		public boolean contains(Point2f p) {
			float radius_squared = radius * radius;
			if (center.distanceSquared(p) < radius_squared) {
				return Zone.inField(p);
			}
			return false;
		}
	}

	private static EnumMap<DangerArea, Zone> dangerAreas;
	static {
		dangerAreas = new EnumMap<DangerArea, Zone>(DangerArea.class);
		// TODO
	}

	private static EnumMap<DangerRadius, Zone> dangerRadius;
	static {
		dangerRadius = new EnumMap<DangerRadius, Zone>(DangerRadius.class);
		// TODO
	}
	private static EnumMap<GoalZone, Zone> goalZones;
	static {
		goalZones = new EnumMap<GoalZone, Zone>(GoalZone.class);
		// TODO
	}
	private static EnumMap<Wing, Zone> wings;
	static {
		wings = new EnumMap<Wing, Zone>(Wing.class);
		// TODO
	}
	private static EnumMap<VerticalQuarter, Zone> verticalQuarters;
	static {
		verticalQuarters = new EnumMap<VerticalQuarter, Zone>(
				VerticalQuarter.class);
		// TODO
	}
	private static EnumMap<WingAndVerticalQuarter, Zone> wingsAndVerticalQuarters;
	static {
		wingsAndVerticalQuarters = new EnumMap<WingAndVerticalQuarter, Zone>(
				WingAndVerticalQuarter.class);
		// example:
		//wingsAndVerticalQuarters.put(
		//		WingAndVerticalQuarter.BOTTOM_LEFTFIELD_LEFTQUARTER,
		//		new AreaZone(new Rectangle2f(new Point2f(1f, 1f), 1f, 1f)));
		// TODO
	}

	protected Zone() {
	}

	public abstract boolean contains(Point2f pos);

	public static boolean inField(Point2f pos) {
		return -52.5f <= pos.x && pos.x <= 52.5f && -34.0f <= pos.y
				&& pos.y <= 34.0;
	}

	public static void main(String[] args) {
		System.out.println("TEST.");
	}
}
