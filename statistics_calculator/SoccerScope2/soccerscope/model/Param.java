/*
 * $Header: $
 */

package soccerscope.model;

import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Tuple2f;

public interface Param {

	// ball parameter
	public final static float BALL_SIZE = 0.085f;
	public final static float BALL_DECAY = 0.94f;
	public final static float BALL_RAND = 0.05f;
	public final static float BALL_WEIGHT = 0.2f;
	public final static float BALL_SPEED_MAX = 2.7f;
	public final static float BALL_ACCEL_MAX = 2.7f;

	// player parameter
	public final static float PLAYER_SIZE = 0.3f;
	public final static float PLAYER_DECAY = 0.4f;
	public final static float PLAYER_RAND = 0.1f;
	public final static float PLAYER_WEIGHT = 60.0f;
	public final static float PLAYER_SPEED_MAX = 1.2f;
	public final static float PLAYER_ACCEL_MAX = 1.0f;
	public final static float STAMINA_MAX = 4000.0f;
	public final static float STAMINA_INC_MAX = 45.0f;
	public final static float RECOVERY_DEC_THR = 0.3f;
	public final static float RECOVERY_DEC = 0.002f;
	public final static float RECOVERY_MIN = 0.5f;
	public final static float EFFORT_DEC_THR = 0.3f;
	public final static float EFFORT_DEC = 0.005f;
	public final static float EFFORT_INC_THR = 0.6f;
	public final static float EFFORT_INC = 0.01f;
	public final static float EFFORT_MAX = 1.0f;
	public final static float EFFORT_MIN = 0.6f;

	public final static float KICK_POWER_RATE = 0.027f;
	public final static float DASH_POWER_RATE = 0.006f;
	public final static float INERTIA_MOMENT = 5.0f;
	public final static float POWER_MAX = 100.0f;

	public final static float GOALIE_CATCHABLE_AREA_LENGTH = 2.0f;
	public final static float GOALIE_CATCHABLE_AREA_WIDTH = 1.0f;

	public final static float VISIBLE_ANGLE = 90.0f;
	public final static float VISIBLE_DISTANCE = 3.0f;

	public final static float KICKABLE_MARGIN = 0.7f;
	public final static float AUDIO_CUT_DIST = 50.0f;

	public final static float TACKLE_DIST = 2.5f;
	public final static float TACKLE_BACK_DIST = 0.5f;
	public final static float TACKLE_WIDTH = 1.25f;
	public final static float TACKLE_EXPONENT = 6.0f;
	public final static int TACKLE_CYCLES = 10;
	public final static float TACKLE_POWER_RATE = 0.027f;

	// field parameter
	public final static float PITCH_LENGTH = 105.0f;
	public final static float PITCH_WIDTH = 68.0f;
	public final static float PITCH_MARGIN = 5.0f;
	public final static float CENTER_CIRCLE_R = 9.15f;
	public final static float PENALTY_AREA_LENGTH = 16.5f;
	public final static float PENALTY_AREA_WIDTH = 40.32f;
	public final static float GOAL_AREA_LENGTH = 5.5f;
	public final static float GOAL_AREA_WIDTH = 18.32f;
	public final static float GOAL_WIDTH = 14.02f; // 7.32
	public final static float GOAL_DEPTH = 2.44f;
	public final static float PENALTY_SPOT_DIST = 11.0f;
	public final static float SPOT_R = 0.1f;
	public final static float CORNER_ARC_R = 1.0f;
	public final static float UNUM_FAR_LENGTH = 20.0f;
	public final static float UNUM_TOO_FAR_LENGTH = 40.0f;
	public final static float TEAM_FAR_LENGTH = 40.0f;
	public final static float TEAM_TOO_FAR_LENGTH = 60.0f;

	// prepare
	public final static float KICKABLE_R = BALL_SIZE + PLAYER_SIZE
			+ KICKABLE_MARGIN;
	public final static float KICKABLE_2R = 2.0f * KICKABLE_R;

	public final static float CENTER_CIRCLE_2R = 2.0f * CENTER_CIRCLE_R;
	public final static float PITCH_HALF_LENGTH = PITCH_LENGTH / 2.0f;
	public final static float PITCH_HALF_WIDTH = PITCH_WIDTH / 2.0f;
	public final static float PENALTY_AREA_HALF_LENGTH = PENALTY_AREA_LENGTH / 2.0f;
	public final static float PENALTY_AREA_HALF_WIDTH = PENALTY_AREA_WIDTH / 2.0f;
	public final static float GOAL_AREA_HALF_LENGTH = GOAL_AREA_LENGTH / 2.0f;
	public final static float GOAL_AREA_HALF_WIDTH = GOAL_AREA_WIDTH / 2.0f;
	public final static float GOAL_HALF_WIDTH = GOAL_WIDTH / 2.0f;

	public final static int MAX_PLAYER = 11;
	public final static int TOTAL_PLAYER = 22;

	// �ե�����ɤκ����ɸ
	public final static Point2f topLeftCorner = new Point2f(-PITCH_HALF_LENGTH,
			-PITCH_HALF_WIDTH);
	// �ե�����ɤα����ɸ
	public final static Point2f topRightCorner = new Point2f(PITCH_HALF_LENGTH,
			-PITCH_HALF_WIDTH);
	// �ե�����ɤκ�����ɸ
	public final static Point2f bottomLeftCorner = new Point2f(
			-PITCH_HALF_LENGTH, PITCH_HALF_WIDTH);
	// �ե�����ɤα�����ɸ
	public final static Point2f bottomRightCorner = new Point2f(
			PITCH_HALF_LENGTH, PITCH_HALF_WIDTH);

	// ��������κ����ɸ
	public final static Point2f leftGoal = new Point2f(-PITCH_HALF_LENGTH
			- GOAL_DEPTH, -GOAL_HALF_WIDTH);
	// �������륨�ꥢ�κ����ɸ
	public final static Point2f leftGoalArea = new Point2f(-PITCH_HALF_LENGTH,
			-GOAL_AREA_WIDTH / 2);
	// ���ڥʥ�ƥ����ꥢ�κ����ɸ
	public final static Point2f leftPenaltyArea = new Point2f(
			-PITCH_HALF_LENGTH, -PENALTY_AREA_WIDTH / 2);
	// ���ڥʥ�ƥ����������濴��ɸ
	public final static Point2f leftPenaltyArcCenter = new Point2f(
			-PITCH_HALF_LENGTH + PENALTY_SPOT_DIST, 0.0f);
	// ���ڥʥ�ƥ��������κ����ɸ
	public final static Point2f leftPenaltyArcTopLeft = new Point2f(
			-PITCH_HALF_LENGTH + PENALTY_SPOT_DIST - CENTER_CIRCLE_R,
			-CENTER_CIRCLE_R);
	// ���ڥʥ�ƥ��������γ��ϳ���
	public final static float leftPenaltyArcStartAngle = (float) (-Math
			.toDegrees(Math.acos((PENALTY_AREA_LENGTH - PENALTY_SPOT_DIST)
					/ CENTER_CIRCLE_R)));

	// �ϡ��ե饤��(���󥿡��饤��)�ξ��ɸ
	public final static Point2f topHalfLine = new Point2f(0, -PITCH_HALF_WIDTH);
	// �ϡ��ե饤��(���󥿡��饤��)�β���ɸ
	public final static Point2f bottomHalfLine = new Point2f(0,
			PITCH_HALF_WIDTH);
	// ���󥿡�����������濴��ɸ
	public final static Point2f centerCircleSpot = new Point2f(0, 0);
	// ���󥿡���������κ����ɸ
	public final static Point2f centerCircleTopLeft = new Point2f(
			-CENTER_CIRCLE_R, -CENTER_CIRCLE_R);

	// ��������κ����ɸ
	public final static Point2f rightGoal = new Point2f(PITCH_HALF_LENGTH,
			-GOAL_HALF_WIDTH);
	// �������륨�ꥢ�κ����ɸ
	public final static Point2f rightGoalArea = new Point2f(PITCH_HALF_LENGTH
			- GOAL_AREA_LENGTH, -GOAL_AREA_WIDTH / 2);
	// ���ڥʥ�ƥ����ꥢ�κ����ɸ
	public final static Point2f rightPenaltyArea = new Point2f(
			PITCH_HALF_LENGTH - PENALTY_AREA_LENGTH, -PENALTY_AREA_WIDTH / 2);
	// ���ڥʥ�ƥ����������濴��ɸ
	public final static Point2f rightPenaltyArcCenter = new Point2f(
			PITCH_HALF_LENGTH - PENALTY_SPOT_DIST, 0.0f);
	// ���ڥʥ�ƥ��������κ����ɸ
	public final static Point2f rightPenaltyArcTopLeft = new Point2f(
			PITCH_HALF_LENGTH - PENALTY_SPOT_DIST - CENTER_CIRCLE_R,
			-CENTER_CIRCLE_R);
	// ���ڥʥ�ƥ��������γ��ϳ���
	public final static float rightPenaltyArcStartAngle = (float) (-Math
			.toDegrees(Math.acos((PENALTY_AREA_LENGTH - PENALTY_SPOT_DIST)
					/ CENTER_CIRCLE_R)) + 180);

	// �ԥå�(�ե����������)�Τ�������
	public final static Tuple2f pitchSize = new Tuple2f(PITCH_LENGTH,
			PITCH_WIDTH);
	// (���󥿡�)����������礭��
	public final static Tuple2f circleSize = new Tuple2f(CENTER_CIRCLE_R * 2,
			CENTER_CIRCLE_R * 2);
	// �����륨�ꥢ���礭��
	public final static Tuple2f goalAreaSize = new Tuple2f(GOAL_AREA_LENGTH,
			GOAL_AREA_WIDTH);
	// �ڥʥ�ƥ����ꥢ���礭��
	public final static Tuple2f penaltyAreaSize = new Tuple2f(
			PENALTY_AREA_LENGTH, PENALTY_AREA_WIDTH);
	// ��������礭��
	public final static Tuple2f goalSize = new Tuple2f(GOAL_DEPTH, GOAL_WIDTH);
	// �����ʡ����������礭��
	public final static Tuple2f cornerSize = new Tuple2f(CORNER_ARC_R * 2,
			CORNER_ARC_R * 2);
	// �ڥʥ�ƥ����ݥåȤ��礭��
	public final static Tuple2f spotSize = new Tuple2f(SPOT_R, SPOT_R);
	// ͭ�볦���礭��
	public final static Tuple2f visibleSize = new Tuple2f(VISIBLE_DISTANCE,
			VISIBLE_DISTANCE);
	// ���2��
	public final static Tuple2f visibleSize2 = new Tuple2f(
			VISIBLE_DISTANCE * 2, VISIBLE_DISTANCE * 2);

	// �ڥʥ�ƥ��������γ���
	public final static float penaltyArcAngle = (float) (Math.toDegrees(Math
			.acos((PENALTY_AREA_LENGTH - PENALTY_SPOT_DIST) / CENTER_CIRCLE_R)) * 2);

	public final static Tuple2f pitchSize1 = new Tuple2f(PITCH_LENGTH,
			PITCH_LENGTH);
	public final static Tuple2f pitchSize2 = new Tuple2f(PITCH_LENGTH * 2,
			PITCH_LENGTH * 2);
	public final static Tuple2f pitchSize4 = new Tuple2f(PITCH_LENGTH * 4,
			PITCH_LENGTH * 4);
	public final static Tuple2f teamTooFarSize = new Tuple2f(
			TEAM_TOO_FAR_LENGTH, TEAM_TOO_FAR_LENGTH);
	public final static Tuple2f teamTooFarSize2 = new Tuple2f(
			TEAM_TOO_FAR_LENGTH * 2, TEAM_TOO_FAR_LENGTH * 2);
	public final static Tuple2f unumTooFarSize = new Tuple2f(
			UNUM_TOO_FAR_LENGTH, UNUM_TOO_FAR_LENGTH);
	public final static Tuple2f unumTooFarSize2 = new Tuple2f(
			UNUM_TOO_FAR_LENGTH * 2, UNUM_TOO_FAR_LENGTH * 2);

	public final static Rectangle2D.Float PitchRect = new Rectangle2D.Float(
			topLeftCorner.x, topLeftCorner.y, pitchSize.x, pitchSize.y);

	public final static Line2D.Float centerLine = new Line2D.Float(
			topHalfLine.x, topHalfLine.y, bottomHalfLine.x, bottomHalfLine.y);

	public final static Ellipse2D.Float centerCircle = new Ellipse2D.Float(
			centerCircleTopLeft.x, centerCircleTopLeft.y, circleSize.x,
			circleSize.y);

	public final static Ellipse2D.Float centerSpot = new Ellipse2D.Float(
			centerCircleSpot.x - SPOT_R, centerCircleSpot.y - SPOT_R,
			SPOT_R * 2, SPOT_R * 2);

	public final static Arc2D.Float topLeftArc = new Arc2D.Float(
			topLeftCorner.x - CORNER_ARC_R, topLeftCorner.y - CORNER_ARC_R,
			cornerSize.x, cornerSize.y, 270, 90, Arc2D.OPEN);

	public final static Arc2D.Float topRightArc = new Arc2D.Float(
			topRightCorner.x - CORNER_ARC_R, topRightCorner.y - CORNER_ARC_R,
			cornerSize.x, cornerSize.y, 180, 90, Arc2D.OPEN);

	public final static Arc2D.Float bottomLeftArc = new Arc2D.Float(
			bottomLeftCorner.x - CORNER_ARC_R, bottomLeftCorner.y
					- CORNER_ARC_R, cornerSize.x, cornerSize.y, 0, 90,
			Arc2D.OPEN);

	public final static Arc2D.Float bottomRightArc = new Arc2D.Float(
			bottomRightCorner.x - CORNER_ARC_R, bottomRightCorner.y
					- CORNER_ARC_R, cornerSize.x, cornerSize.y, 90, 90,
			Arc2D.OPEN);

	public final static Rectangle2D.Float leftGoalRect = new Rectangle2D.Float(
			leftGoal.x, leftGoal.y, goalSize.x, goalSize.y);

	public final static Rectangle2D.Float leftGoalAreaRect = new Rectangle2D.Float(
			leftGoalArea.x, leftGoalArea.y, goalAreaSize.x, goalAreaSize.y);

	public final static Rectangle2D.Float leftPenaltyAreaRect = new Rectangle2D.Float(
			leftPenaltyArea.x, leftPenaltyArea.y, penaltyAreaSize.x,
			penaltyAreaSize.y);

	public final static Ellipse2D.Float leftPenaltySpot = new Ellipse2D.Float(
			leftPenaltyArcCenter.x - SPOT_R, leftPenaltyArcCenter.y - SPOT_R,
			SPOT_R * 2, SPOT_R * 2);

	public final static Arc2D.Float leftPenaltyArc = new Arc2D.Float(
			leftPenaltyArcTopLeft.x, leftPenaltyArcTopLeft.y, circleSize.x,
			circleSize.y, leftPenaltyArcStartAngle, penaltyArcAngle, Arc2D.OPEN);

	public final static Rectangle2D.Float rightGoalRect = new Rectangle2D.Float(
			rightGoal.x, rightGoal.y, goalSize.x, goalSize.y);

	public final static Rectangle2D.Float rightGoalAreaRect = new Rectangle2D.Float(
			rightGoalArea.x, rightGoalArea.y, goalAreaSize.x, goalAreaSize.y);

	public final static Rectangle2D.Float rightPenaltyAreaRect = new Rectangle2D.Float(
			rightPenaltyArea.x, rightPenaltyArea.y, penaltyAreaSize.x,
			penaltyAreaSize.y);

	public final static Ellipse2D.Float rightPenaltySpot = new Ellipse2D.Float(
			rightPenaltyArcCenter.x - SPOT_R, rightPenaltyArcCenter.y - SPOT_R,
			SPOT_R * 2, SPOT_R * 2);

	public final static Arc2D.Float rightPenaltyArc = new Arc2D.Float(
			rightPenaltyArcTopLeft.x, rightPenaltyArcTopLeft.y, circleSize.x,
			circleSize.y, rightPenaltyArcStartAngle, penaltyArcAngle,
			Arc2D.OPEN);
}
