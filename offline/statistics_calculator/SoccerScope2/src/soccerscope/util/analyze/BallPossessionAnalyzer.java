/*
 * $Log: BallPossessionAnalyzer.java,v $
 * Revision 1.12  2003/01/04 08:35:35  koji
 * �ѥ���Ϣ�����ѥ��ߥ����ɥ�֥롢���줾�����ײ��Ϥ��ɲ�
 *
 * Revision 1.11  2002/12/23 05:32:11  koji
 * PassDistAnalyzer�ɲ�
 *
 * Revision 1.10  2002/12/02 08:48:01  koji
 * �ܡ���򼺤ä�������ѥ��μ������ʿ��X��ɸ�ͤ��ɲ�
 *
 * Revision 1.9  2002/10/22 05:14:41  koji
 * totalTime��0�ΤȤ��ˡ��Ѥ�ʸ����֤��Ƥ����Τ���
 *
 * Revision 1.8  2002/10/18 11:24:27  koji
 * init����ʤ���ˡ�getValueAt�����NullPointerExep�ˤʤ�Τ���
 *
 * Revision 1.7  2002/10/17 09:33:10  koji
 * ���祭�å��ΤȤ��ν������
 *
 * Revision 1.6  2002/10/16 12:04:33  koji
 * PlayON�ʳ��ΤȤ��ν�����ɲá����å������礷���Ȥ��ϡ��ɤ���λ��ۤǤ�
 * �ʤ����Ȥˤ���
 *
 * Revision 1.5  2002/10/15 10:09:48  koji
 * ��������Pass������ȡ�Team/�����̥ơ��֥��ɲ�
 *
 * Revision 1.4  2002/10/11 10:41:36  koji
 * �Х�����(HeteroParam),GoalȽ������
 *
 * Revision 1.3  2002/10/04 10:39:14  koji
 * Tuple,Point,Vector�Υѥå������ѹ���Geometry,Circle,Line,Rectangle�ɲ�
 *
 * Revision 1.2  2002/09/17 07:46:24  koji
 * WatchBall��ZoomBall�Υ��������ѹ���Publish�ѤΥѥͥ��ɲá�������ν���
 * �Υ�����򥫥���Ȥ��Ƥ��ʤ��ä��Τ���
 *
 * Revision 1.1  2002/09/12 11:25:41  koji
 * ��ײ��ϡ�������ǽ���ɲá���������ե�����졼�����ե������б�
 *
 *
 */

package soccerscope.util.analyze;

import java.text.NumberFormat;

import soccerscope.model.GameEvent;
import soccerscope.model.Scene;

public class BallPossessionAnalyzer extends SceneAnalyzer {

	public static String NAME = "Ball Possession";
	
	// copied from PassAnalyzer :p
	public final static int PLAY_OFF = PassAnalyzer.PLAY_OFF;
	public final static int PLAY_ON = PassAnalyzer.PLAY_ON;
	public final static int LEFT_SIDE = PassAnalyzer.LEFT_SIDE;
	public final static int RIGHT_SIDE = PassAnalyzer.RIGHT_SIDE;

	private int totalTime;

	public void init() {
		totalTime = 0;
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
			if (totalTime == 0)
				return "0";
			return nf.format(lcount * 100.0 / totalTime);
		case RIGHT:
			if (totalTime == 0)
				return "0";
			return nf.format(rcount * 100.0 / totalTime);
		default:
			return " ";
		}
	}

	public void count(int fromTime, int toTime) {
		totalTime = 0;
		lcount = 0;
		rcount = 0;
		int pTable[] = PassAnalyzer.getPossessionTable();
		for (int i = fromTime; i <= toTime; i++) {
			if (i >= pTable.length)
				continue;
			switch (pTable[i]) {
			case PassAnalyzer.PLAY_OFF:
				break;
			case PassAnalyzer.PLAY_ON:
				totalTime++;
				break;
			case PassAnalyzer.LEFT_SIDE:
				totalTime++;
				lcount++;
				break;
			case PassAnalyzer.RIGHT_SIDE:
				totalTime++;
				rcount++;
				break;
			}
		}
	}

	public GameEvent analyze(Scene scene, Scene prev) {
		return null;
	}
	
	public static int getPossessionTeam(int time) {
		return PassAnalyzer.getPossessionTeam(time);
	}
	

	/**
	 * checks if the ball possession was always of the Team 'side'
	 * in the period 'stime' 'etime'
	 * @param side
	 * @return
	 */
	public static boolean checkPossessionForTeam(int side, int stime, int etime) {
		for(int iter = 0; iter < etime; iter++) {
			int pside = PassAnalyzer.sideToStandardSide(PassAnalyzer.getPossessionTeam(iter));
			if(pside != side) {
				return false;
			}
		}		
		return true;
	}
}
