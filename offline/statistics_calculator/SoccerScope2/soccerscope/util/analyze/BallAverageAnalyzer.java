/*
 * $Log: BallAverageAnalyzer.java,v $
 * Revision 1.3  2003/01/16 08:06:33  koji
 * positionList$B$,(Bnull$B$K$J$k$N$r=$@5(B
 *
 * Revision 1.2  2003/01/14 05:53:15  koji
 * �?�ե�������ײ����ѽ���
 *
 * Revision 1.1  2003/01/04 08:35:35  koji
 * �ѥ���Ϣ�����ѥ��ߥ����ɥ�֥롢���줾�����ײ��Ϥ��ɲ�
 *
 *
 */

package soccerscope.util.analyze;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;

import soccerscope.model.GameEvent;
import soccerscope.model.PlayMode;
import soccerscope.model.Scene;
import soccerscope.util.geom.Point2f;

public class BallAverageAnalyzer extends SceneAnalyzer {

	public static String NAME = "Ball Ave";

	private ArrayList<Position> positionList;
	private double x;
	private double y;

	public BallAverageAnalyzer() {
		positionList = new ArrayList<Position>();
	}

	public void init() {
		super.init();
		positionList = new ArrayList<Position>();
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
			return nf.format(x) + ", " + nf.format(y);
		case RIGHT:
		default:
			return " ";
		}
	}

	public void count(int fromTime, int toTime) {
		int count = 0;
		x = 0;
		y = 0;
		Iterator<Position> it = positionList.iterator();
		while (it.hasNext()) {
			Position p = it.next();
			if (fromTime <= p.time && p.time <= toTime) {
				count++;
				x += p.pos.x;
				y += p.pos.y;
			}
		}
		if (count != 0) {
			x /= count;
			y /= count;
		}
	}

	public class Position {
		public int time;
		public Point2f pos;

		Position(int time, Point2f pos) {
			this.time = time;
			this.pos = pos;
		}
	}

	public GameEvent analyze(Scene scene, Scene prev) {
		if (scene.pmode.pmode != PlayMode.PM_PlayOn) {
			return null;
		}

		positionList.add(new Position(scene.time, scene.ball.pos));

		return null;
	}
}
