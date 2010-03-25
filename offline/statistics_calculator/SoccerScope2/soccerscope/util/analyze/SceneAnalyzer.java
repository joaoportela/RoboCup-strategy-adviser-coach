/*
 * $Log: SceneAnalyzer.java,v $
 * Revision 1.4  2003/01/04 08:35:37  koji
 * �ѥ���Ϣ�����ѥ��ߥ����ɥ�֥롢���줾�����ײ��Ϥ��ɲ�
 *
 * Revision 1.3  2002/10/18 11:24:27  koji
 * init����ʤ���ˡ�getValueAt�����NullPointerExep�ˤʤ�Τ���
 *
 * Revision 1.2  2002/10/15 10:09:49  koji
 * ��������Pass������ȡ�Team/�����̥ơ��֥��ɲ�
 *
 * Revision 1.1  2002/09/12 11:25:43  koji
 * ��ײ��ϡ�������ǽ���ɲá���������ե�����졼�����ե������б�
 *
 *
 */

package soccerscope.util.analyze;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import soccerscope.model.GameEvent;
import soccerscope.model.Player;
import soccerscope.model.Scene;
import soccerscope.model.Team;
import soccerscope.model.WorldModel;

public abstract class SceneAnalyzer {

	public final static int ROW_NAME = 0;
	public final static int LEFT = 1;
	public final static int RIGHT = 2;
	public final static int COL_MAX = 3;

	protected int lcount;
	protected int rcount;
	protected int lastTime;
	private ArrayList<Integer> lcountList = new ArrayList<Integer>();
	private ArrayList<Integer> rcountList = new ArrayList<Integer>();

	public abstract GameEvent analyze(Scene scene, Scene prev);

	public abstract String getName();

	public void init() {
		lcountList = new ArrayList<Integer>();
		rcountList = new ArrayList<Integer>();
		lcount = 0;
		rcount = 0;
		lastTime = WorldModel.getInstance().getSceneSet().lastScene().time;
	}

	public void countUp(int side, int time) {
		if (side == Team.LEFT_SIDE) {
			countUpLeft(time);
			return;
		}
		if (side == Team.RIGHT_SIDE) {
			countUpRight(time);
			return;
		}
	}

	public void countUpLeft(int time) {
		lcountList.add(new Integer(time));
		lcount++;
	}

	public void countUpRight(int time) {
		rcountList.add(new Integer(time));
		rcount++;
	}

	public Object getValueAt(int col) {
		return getValueAt(col, 0, lastTime);
	}

	public Object getLeftValueAt(int fromTime, int toTime) {
		return getValueAt(LEFT, fromTime, toTime);
	}

	public Object getRightValueAt(int fromTime, int toTime) {
		return getValueAt(RIGHT, fromTime, toTime);
	}

	public Object getValueAt(int col, int fromTime, int toTime) {
		count(fromTime, toTime);
		switch (col) {
		case ROW_NAME:
			return getName();
		case LEFT:
			if (lcount == -1)
				return "--";
			return new Integer(lcount);
		case RIGHT:
			if (rcount == -1)
				return "--";
			return new Integer(rcount);
		default:
			return " ";
		}
	}

	public void count(int fromTime, int toTime) {
		lcount = 0;
		Iterator<Integer> it = lcountList.iterator();
		while (it.hasNext()) {
			int time = it.next().intValue();
			if (fromTime <= time && time <= toTime) {
				lcount++;
			}
		}
		rcount = 0;
		it = rcountList.iterator();
		while (it.hasNext()) {
			int time = it.next().intValue();
			if (fromTime <= time && time <= toTime) {
				rcount++;
			}
		}
	}

	public TableModel getTableModel() {
		count(0, lastTime);
		TableModel dataModel = new AbstractTableModel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public String getColumnName(int col) {
				switch (col) {
				case ROW_NAME:
					return " ";
				case LEFT:
					return "Left";
				case RIGHT:
					return "Right";
				default:
					return " ";
				}
			}

			public int getColumnCount() {
				return COL_MAX;
			}

			public int getRowCount() {
				return 1;
			}

			public Object getValueAt(int row, int col) {
				switch (col) {
				case ROW_NAME:
					return getName();
				case LEFT:
					if (lcount == -1)
						return "--";
					return new Integer(lcount);
				case RIGHT:
					if (rcount == -1)
						return "--";
					return new Integer(rcount);
				default:
					return " ";
				}
			}
		};

		return dataModel;
	}

	public boolean isPlayModeChanged(Scene scene, Scene prev, int pmode) {
		return scene.pmode.pmode == pmode && prev.pmode.pmode != pmode;
	}

	public boolean doKicked(Player player, Player prev) {
		return player.kickCount > prev.kickCount;
	}

	public boolean doDashed(Player player, Player prev) {
		return player.dashCount > prev.dashCount;
	}

	public boolean doTurned(Player player, Player prev) {
		return player.turnCount > prev.turnCount;
	}

	// v0��v��ã����ޤǤ˰�ư�����Υ
	protected float getFinalDist(float v0, float d, float v) {
		return getTotalDist(v0, d, getTimeByVel(v0, d, v));
	}

	// v0��t��ΰ�ư��Υ
	protected float getTotalDist(float v0, float d, int t) {
		if (t == 0)
			return 0;
		float b, e; // ,c
		b = 1 - d;
		// c = 1 - (float) Math.pow(d, t + 1);
		e = 1 - (float) Math.pow(d, t);
		return v0 * (e / b);
	}

	// v0��v��ã����ޤǤˤ��������
	protected int getTimeByVel(float v0, float d, float v) {
		// double b = 1 - d;
		return (int) Math.ceil((Math.log(v / (v0 / d))) / (Math.log(d)) - 1);
	}

	// [-180,180] -> [0,360]
	protected float normalize(float angle) {
		while (angle < 0)
			angle += 2 * Math.toDegrees(Math.PI);
		return angle;
	}
}
