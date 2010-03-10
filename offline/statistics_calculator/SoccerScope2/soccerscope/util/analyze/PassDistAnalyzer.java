/*
 * $Log: PassDistAnalyzer.java,v $
 * Revision 1.3  2003/01/14 05:53:15  koji
 * �?�ե�������ײ����ѽ���
 *
 * Revision 1.2  2003/01/04 08:35:37  koji
 * �ѥ���Ϣ�����ѥ��ߥ����ɥ�֥롢���줾�����ײ��Ϥ��ɲ�
 *
 * Revision 1.1  2002/12/23 05:32:11  koji
 * PassDistAnalyzer�ɲ�
 *
 *
 */

package soccerscope.util.analyze;

import java.text.NumberFormat;
import java.util.Iterator;

import soccerscope.model.GameEvent;
import soccerscope.model.Param;
import soccerscope.model.Scene;
import soccerscope.model.WorldModel;
import soccerscope.util.analyze.PassAnalyzer.Pass;
import soccerscope.util.geom.Point2f;

public class PassDistAnalyzer extends SceneAnalyzer {

	public static String NAME = "Pass Dist";

	private float leftMin;
	private float leftAve;
	private float leftMax;
	private float rightMin;
	private float rightAve;
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
				Scene rscene = WorldModel.getInstance().getSceneSet().getScene(
						pass.receiver.time);
				Scene sscene = WorldModel.getInstance().getSceneSet().getScene(
						pass.sender.time);
				Point2f spos = sscene.player[pass.sender.unum - 1].pos;
				Point2f rpos = rscene.player[pass.receiver.unum - 1].pos;
				float dist = spos.dist(rpos);
				leftAve += dist;
				lcount++;
				if (leftMin > dist)
					leftMin = dist;
				if (leftMax < dist)
					leftMax = dist;
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
				Scene rscene = WorldModel.getInstance().getSceneSet().getScene(
						pass.receiver.time);
				Scene sscene = WorldModel.getInstance().getSceneSet().getScene(
						pass.sender.time);
				Point2f spos = sscene.player[pass.sender.unum
						+ Param.MAX_PLAYER - 1].pos;
				Point2f rpos = rscene.player[pass.receiver.unum
						+ Param.MAX_PLAYER - 1].pos;
				float dist = spos.dist(rpos);
				rightAve += dist;
				rcount++;
				if (rightMin > dist)
					rightMin = dist;
				if (rightMax < dist)
					rightMax = dist;
			}
		}
		if (rcount == 0)
			rcount = -1;
	}

	public GameEvent analyze(Scene scene, Scene prev) {
		return null;
	}
}
