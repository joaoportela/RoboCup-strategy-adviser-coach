/*
 * $Log: PassVelAnalyzer.java,v $
 * Revision 1.2  2003/01/14 05:53:15  koji
 * �?�ե�������ײ����ѽ���
 *
 * Revision 1.1  2003/01/04 08:35:37  koji
 * �ѥ���Ϣ�����ѥ��ߥ����ɥ�֥롢���줾�����ײ��Ϥ��ɲ�
 *
 *
 */

package soccerscope.util.analyze;

import java.text.NumberFormat;
import java.util.Iterator;

import soccerscope.model.GameEvent;
import soccerscope.model.Scene;
import soccerscope.model.WorldModel;
import soccerscope.util.analyze.PassAnalyzer.Pass;

public class PassVelAnalyzer extends SceneAnalyzer {

	public static String NAME = "Pass Vel";

	private float leftAve;
	private float leftMin;
	private float leftMax;
	private float rightAve;
	private float rightMin;
	private float rightMax;

	public void init() {
		super.init();
	}

	public String getName() {
		return NAME;
	}

	public Object getValueAt(int col, int fromTime, int toTime) {
		count(fromTime, toTime);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumIntegerDigits(2);
		nf.setMaximumFractionDigits(2);
		switch (col) {
		case ROW_NAME:
			return getName();
		case LEFT:
			if (lcount == -1)
				return "--";
			// return "(" + nf.format(leftMin) + " : " + nf.format(leftAve /
			// lcount) +
			// " : " + nf.format(leftMax) + ")";
			return nf.format(leftAve / lcount);
		case RIGHT:
			if (rcount == -1)
				return "--";
			// return "(" + nf.format(rightMin) + " : " + nf.format(rightAve /
			// rcount) +
			// " : " + nf.format(rightMax) + ")";
			return nf.format(rightAve / rcount);
		default:
			return " ";
		}
	}

	public void count(int fromTime, int toTime) {
		Iterator<Pass> it = PassAnalyzer.getPassLeftList().iterator();
		lcount = 0;
		leftAve = 0;
		leftMin = 100;
		leftMax = -100;
		while (it.hasNext()) {
			Pass pass = it.next();
			if (fromTime <= pass.sender.time && pass.receiver.time <= toTime) {
				Scene scene = WorldModel.getInstance().getSceneSet().getScene(
						pass.sender.time);
				float vel = scene.ball.vel.r();
				leftAve += vel;
				lcount++;
				if (leftMin > vel)
					leftMin = vel;
				if (leftMax < vel)
					leftMax = vel;
			}
		}
		if (lcount == 0)
			lcount = -1;

		it = PassAnalyzer.getPassRightList().iterator();
		rcount = 0;
		rightAve = 0;
		rightMin = 100;
		rightMax = -100;
		while (it.hasNext()) {
			Pass pass = it.next();
			if (fromTime <= pass.sender.time && pass.receiver.time <= toTime) {
				Scene scene = WorldModel.getInstance().getSceneSet().getScene(
						pass.sender.time);
				float vel = scene.ball.vel.r();
				rightAve += vel;
				rcount++;
				if (rightMin > vel)
					rightMin = vel;
				if (rightMax < vel)
					rightMax = vel;
			}
		}
		if (rcount == 0)
			rcount = -1;
	}

	public GameEvent analyze(Scene scene, Scene prev) {
		return null;
	}
}
