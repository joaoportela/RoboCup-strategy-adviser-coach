package soccerscope.decision;

import soccerscope.model.Team;

public enum ZoneEnum {
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