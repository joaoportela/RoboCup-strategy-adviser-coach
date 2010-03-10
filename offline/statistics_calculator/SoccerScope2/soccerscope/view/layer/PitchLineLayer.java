/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.Color;
import java.awt.Graphics;

import soccerscope.model.Param;
import soccerscope.util.Color2;
import soccerscope.util.geom.Point2f;
import soccerscope.view.FieldPane;

// ���å����ե�����ɤ��������褹��쥤�䡼
public class PitchLineLayer extends Layer {

	public PitchLineLayer(FieldPane fieldPane) {
		super(fieldPane, true);
	}

	public String getLayerName() {
		return "PitchLine";
	}

	private Point2f drawPoint = new Point2f();
	private Point2f drawOffset = new Point2f(-Param.CORNER_ARC_R,
			-Param.CORNER_ARC_R);

	public void draw(Graphics g) {

		/* ���ե����Ѵ���Ȥä����� */
		/* ���ä��ꤹ�뤱�ɡ��Ť� */
		// Graphics2D g2 = (Graphics2D) g;
		// AffineTransform aforg = g2.getTransform();
		// AffineTransform af = new AffineTransform(aforg);
		// af.concatenate(fieldPane.getAffineTransform());
		// g2.setTransform(af);
		// g2.setStroke(new BasicStroke(1.0f / fieldPane.getMagnify()));

		// // left and right goal
		// g.setColor(Color.black);
		// g2.fill(leftGoalRect);
		// g2.fill(rightGoalRect);

		// // touch lines
		// g.setColor(ColorTool.mix(Color2.snow, Color2.forestGreen, 1, 1));
		// g2.draw(PitchRect);

		// // center line and center circle
		// g2.draw(centerLine);
		// g2.draw(centerCircle);
		// g2.fill(centerSpot);

		// // corner arc
		// g2.draw(topLeftArc);
		// g2.draw(topRightArc);
		// g2.draw(bottomLeftArc);
		// g2.draw(bottomRightArc);

		// // left goal-area penalty-area penalty-arc
		// g2.draw(leftPenaltyArc);
		// g2.draw(leftPenaltySpot);
		// g2.draw(leftPenaltyAreaRect);
		// g2.draw(leftGoalAreaRect);
		// g2.draw(leftGoalRect);

		// // right goal-area penalty-area penalty-arc
		// g2.draw(rightPenaltyArc);
		// g2.draw(rightPenaltySpot);
		// g2.draw(rightPenaltyAreaRect);
		// g2.draw(rightGoalAreaRect);
		// g2.draw(rightGoalRect);

		// g2.setTransform(aforg);

		// left and right goal
		g.setColor(Color.black);
		fillRect(g, leftGoal, goalSize);
		fillRect(g, rightGoal, goalSize);

		// touch lines
		g.setColor(Color2.mix(Color2.snow, Color2.forestGreen, 1, 1));
		drawRect(g, topLeftCorner, pitchSize);

		// left goal-area penalty-area penalty-arc
		drawArc(g, leftPenaltyArcTopLeft, circleSize,
				(int) leftPenaltyArcStartAngle, (int) penaltyArcAngle);
		drawCircle(g, leftPenaltyArcCenter, Param.SPOT_R);
		drawRect(g, leftPenaltyArea, penaltyAreaSize);
		drawRect(g, leftGoalArea, goalAreaSize);
		drawRect(g, leftGoal, goalSize);

		// center line and center circle
		drawLine(g, topHalfLine, bottomHalfLine);
		drawOval(g, centerCircleTopLeft, circleSize);
		fillCircle(g, centerCircleSpot, Param.SPOT_R);

		// right goal-area penalty-area penalty-arc
		drawArc(g, rightPenaltyArcTopLeft, circleSize,
				(int) rightPenaltyArcStartAngle, (int) penaltyArcAngle);
		drawCircle(g, rightPenaltyArcCenter, Param.SPOT_R);
		drawRect(g, rightPenaltyArea, penaltyAreaSize);
		drawRect(g, rightGoalArea, goalAreaSize);
		drawRect(g, rightGoal, goalSize);

		// corner arc
		drawPoint.add(topLeftCorner, drawOffset);
		drawArc(g, drawPoint, cornerSize, 270, 90);
		drawPoint.add(topRightCorner, drawOffset);
		drawArc(g, drawPoint, cornerSize, 180, 90);
		drawPoint.add(bottomLeftCorner, drawOffset);
		drawArc(g, drawPoint, cornerSize, 0, 90);
		drawPoint.add(bottomRightCorner, drawOffset);
		drawArc(g, drawPoint, cornerSize, 90, 90);

	}
}
