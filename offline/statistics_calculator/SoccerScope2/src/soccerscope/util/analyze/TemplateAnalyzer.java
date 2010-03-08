/*
 * $Log: TemplateAnalyzer.java,v $
 * Revision 1.1  2002/09/12 11:25:43  koji
 * ��ײ��ϡ�������ǽ���ɲá���������ե�����졼�����ե������б�
 *
 *
 */

package soccerscope.util.analyze;

import soccerscope.model.GameEvent;
import soccerscope.model.Scene;

public class TemplateAnalyzer extends SceneAnalyzer {

	public static String NAME = "TEMPLATE";

	public String getName() {
		return NAME;
	}

	public GameEvent analyze(Scene scene, Scene prev) {
		return null;
	}
}
