/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.Graphics;

import soccerscope.model.ColorDB;
import soccerscope.model.Scene;
import soccerscope.model.SceneSet;
import soccerscope.model.WorldModel;
import soccerscope.util.analyze.BallPossessionAnalyzer;
import soccerscope.view.FieldPane;

public class BallPossessionLayer extends Layer {

	public BallPossessionLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
	}

	public String getLayerName() {
		return "Ball Possession";
	}

	public void draw(Graphics g) {
		if (!enable)
			return;

		Scene scene = fieldPane.getScene();
		SceneSet set = WorldModel.getInstance().getSceneSet();

		int team = BallPossessionAnalyzer.getPossessionTeam(scene.time);
		if (team == BallPossessionAnalyzer.PLAY_OFF
				|| team == BallPossessionAnalyzer.PLAY_ON)
			return;

		int startTime = scene.time;
		while (true) {
			startTime--;
			int t = BallPossessionAnalyzer.getPossessionTeam(startTime);
			if (t != team)
				break;
		}

		if (team == BallPossessionAnalyzer.LEFT_SIDE) {
			g.setColor(ColorDB.getColor("team_l_color"));
		} else if (team == BallPossessionAnalyzer.RIGHT_SIDE) {
			g.setColor(ColorDB.getColor("team_r_color"));
		}

		for (int time = startTime; time < scene.time; time++) {
			drawLine(g, set.getScene(time).ball.pos,
					set.getScene(time + 1).ball.pos);
		}
	}
}
