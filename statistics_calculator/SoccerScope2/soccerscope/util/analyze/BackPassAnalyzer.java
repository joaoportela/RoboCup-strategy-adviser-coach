/*
 * $Log: BackPassAnalyzer.java,v $
 * Revision 1.3  2003/01/04 08:35:35  koji
 * �ѥ���Ϣ�����ѥ��ߥ����ɥ�֥롢���줾�����ײ��Ϥ��ɲ�
 *
 * Revision 1.2  2002/10/15 10:09:48  koji
 * ��������Pass������ȡ�Team/�����̥ơ��֥��ɲ�
 *
 * Revision 1.1  2002/09/12 11:25:41  koji
 * ��ײ��ϡ�������ǽ���ɲá���������ե�����졼�����ե������б�
 *
 *
 */

package soccerscope.util.analyze;

import soccerscope.model.GameEvent;
import soccerscope.model.PlayMode;
import soccerscope.model.Scene;

public class BackPassAnalyzer extends SceneAnalyzer {

	public static String NAME = "BackPass";

	public String getName() {
		return NAME;
	}

	public GameEvent analyze(Scene scene, Scene prev) {
		GameEvent ge = null;
		/* �Хå��ѥ��ϻ��郎�ߤޤ뤿�ᡢprev�򸫤ơ��Хå��ѥ��ˤʤä��ִ֤����򡢼����� */
		if (prev != null) {
			if (isPlayModeChanged(scene, prev, PlayMode.PM_Back_Pass_Left)) {
				countUpLeft(scene.time);
				ge = new GameEvent(scene.time, GameEvent.BACKPASS);
			}
			if (isPlayModeChanged(scene, prev, PlayMode.PM_Back_Pass_Right)) {
				countUpRight(scene.time);
				ge = new GameEvent(scene.time, GameEvent.BACKPASS);
			}
		}
		return ge;
	}
}
