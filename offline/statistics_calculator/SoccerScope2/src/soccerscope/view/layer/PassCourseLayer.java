/*
 * $Log: PassCourseLayer.java,v $
 * Revision 1.4  2002/10/04 10:39:24  koji
 * Tuple,Point,Vector�Υѥå������ѹ���Geometry,Circle,Line,Rectangle�ɲ�
 *
 * Revision 1.3  2002/09/12 15:28:32  koji
 * ������������?���ɲ�,ColorTool��Color2�����
 *
 * Revision 1.2  2002/09/02 07:06:28  taku-ako
 * Geometry��Ϣ�Υ饤�֥������
 *
 * Revision 1.1.1.1  2002/03/01 14:12:53  koji
 * CVS�����
 *
 */

package soccerscope.view.layer;

import java.awt.Graphics;
import java.util.ArrayList;

import soccerscope.model.Param;
import soccerscope.model.PlayMode;
import soccerscope.model.Player;
import soccerscope.model.Scene;
import soccerscope.model.SceneSet;
import soccerscope.model.WorldModel;
import soccerscope.util.geom.Point2f;
import soccerscope.view.FieldPane;

public class PassCourseLayer extends Layer {
	private static Player nowKicker, nextKicker;
	private static ArrayList kickPointList;

	// private static int nowTime, nextTime;

	public PassCourseLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
		kickPointList = new ArrayList();
		nowKicker = null;
		nextKicker = null;
		// nowTime = 0;
		// nextTime = 0;
	}

	public String getLayerName() {
		return "PassCourse";
	}

	public void draw(Graphics g) {
		if (!enable)
			return;
		Scene scene = fieldPane.getScene();
		float ballR = Param.BALL_SIZE * fieldPane.getBallMagnify();
		WorldModel wm = WorldModel.getInstance();
		SceneSet sceneSet = wm.getSceneSet();

		int time = scene.time;
		if (sceneSet.hasScene(time + 1)) {
			/* check now kicker */
			for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
				if (!scene.player[i].isEnable())
					continue;
				if (scene.player[i].isKicking() || scene.player[i].isCatching()) {
					nowKicker = scene.player[i];
					// nowTime = time;
				}
			}
			/* check next kicker */
			boolean isKicked = false;
			while (!isKicked) {
				time++;
				Scene tmpScene = sceneSet.getScene(time);
				for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
					if (tmpScene.player[i].isKicking()
							|| tmpScene.player[i].isCatching()) {
						isKicked = true;
						nextKicker = tmpScene.player[i];
						// nextTime = time;
					}
				}
			}
		}

		/*
		 * Point2f p1 = sceneSet.getScene(nowTime).ball.pos; for (int i =
		 * nowTime; i < nextTime; i++) { Point2f p2 =
		 * sceneSet.getScene(i).ball.pos; g.setColor(nowKicker.getColor());
		 * drawLine(g, p1, p2); p1 = p2; }
		 */

		if ((nowKicker != null && nextKicker != null)
				&& (scene.pmode.pmode == PlayMode.PM_PlayOn)) {
			if (nowKicker.side == nextKicker.side) {
				kickPointList.add(new TwoPoints(nowKicker.pos, nextKicker.pos));
				g.setColor(nowKicker.getColor());
				for (int i = 0; i < kickPointList.size(); i++) {
					drawLine(g, ((TwoPoints) (kickPointList.get(i))).p1,
							((TwoPoints) (kickPointList.get(i))).p2);
				}
			} else {
				kickPointList.clear();
			}
		} else
			kickPointList.clear();
	}

	class TwoPoints {
		Point2f p1, p2;

		TwoPoints(Point2f p1, Point2f p2) {
			this.p1 = p1;
			this.p2 = p2;
		}
	}
}
