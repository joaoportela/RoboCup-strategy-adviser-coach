/*
 * $Log: TopXAnalyzer.java,v $
 * Revision 1.2  2003/01/14 05:53:15  koji
 * �?�ե�������ײ����ѽ���
 *
 * Revision 1.1  2003/01/05 04:10:39  koji
 * ����3�ͤΥѥ��������֤��ɲ�
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

public class TopXAnalyzer extends SceneAnalyzer {

	public static String NAME = "Top3 Recv X";

	private float receiverLeftX;
	private float receiverLeftMinX;
	private float receiverLeftMaxX;
	private float receiverRightX;
	private float receiverRightMinX;
	private float receiverRightMaxX;

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
			// return "(" + nf.format(receiverLeftMinX) + " : " +
			// nf.format(receiverLeftX / lcount) +
			// " : " + nf.format(receiverLeftMaxX) + ")";
			return nf.format(receiverLeftX / lcount);
		case RIGHT:
			if (rcount == -1)
				return "--";
			// return "(" + nf.format(-receiverRightMaxX) + " : " +
			// nf.format(-receiverRightX / rcount) +
			// " : " + nf.format(-receiverRightMinX) + ")";
			return nf.format(-receiverRightX / rcount);
		default:
			return " ";
		}
	}

	public void count(int fromTime, int toTime) {
		Iterator<Pass> it = PassAnalyzer.getPassLeftList().iterator();
		lcount = 0;
		receiverLeftX = 0;
		receiverLeftMinX = 100;
		receiverLeftMaxX = -100;
		while (it.hasNext()) {
			Pass pass = it.next();
			if (fromTime <= pass.sender.time && pass.receiver.time <= toTime) {
				Scene scene = WorldModel.getInstance().getSceneSet().getScene(
						pass.receiver.time);
				float x = scene.player[pass.receiver.unum - 1].pos.x;
				int n_front = 0;
				for (int i = 0; i < Param.MAX_PLAYER; i++) {
					if (i != pass.receiver.unum - 1
							&& scene.player[i].pos.x > x)
						n_front++;
				}
				if (n_front >= 3) {
					scene = WorldModel.getInstance().getSceneSet().getScene(
							pass.sender.time);
					x = scene.player[pass.receiver.unum - 1].pos.x;
					n_front = 0;
					for (int i = 0; i < Param.MAX_PLAYER; i++) {
						if (i != pass.receiver.unum - 1
								&& scene.player[i].pos.x > x)
							n_front++;
					}
				}
				if (n_front < 3) {
					receiverLeftX += x;
					lcount++;
					if (receiverLeftMinX > x)
						receiverLeftMinX = x;
					if (receiverLeftMaxX < x)
						receiverLeftMaxX = x;
				}
			}
		}
		if (lcount == 0)
			lcount = -1;

		it = PassAnalyzer.getPassRightList().iterator();
		rcount = 0;
		receiverRightX = 0;
		receiverRightMinX = 100;
		receiverRightMaxX = -100;
		while (it.hasNext()) {
			Pass pass = it.next();
			if (fromTime <= pass.sender.time && pass.receiver.time <= toTime) {
				Scene scene = WorldModel.getInstance().getSceneSet().getScene(
						pass.receiver.time);
				float x = scene.player[pass.receiver.unum + Param.MAX_PLAYER
						- 1].pos.x;
				int n_front = 0;
				for (int i = Param.MAX_PLAYER; i < Param.MAX_PLAYER * 2; i++) {
					if (i != pass.receiver.unum - 1
							&& scene.player[i].pos.x < x)
						n_front++;
				}
				if (n_front >= 3) {
					scene = WorldModel.getInstance().getSceneSet().getScene(
							pass.sender.time);
					x = scene.player[pass.receiver.unum + Param.MAX_PLAYER - 1].pos.x;
					n_front = 0;
					for (int i = Param.MAX_PLAYER; i < Param.MAX_PLAYER * 2; i++) {
						if (i != pass.receiver.unum - 1
								&& scene.player[i].pos.x < x)
							n_front++;
					}
				}
				if (n_front < 3) {
					receiverRightX += x;
					rcount++;
					if (receiverRightMinX > x)
						receiverRightMinX = x;
					if (receiverRightMaxX < x)
						receiverRightMaxX = x;
				}
			}
		}
		if (rcount == 0)
			rcount = -1;
	}

	public GameEvent analyze(Scene scene, Scene prev) {
		return null;
	}
}
