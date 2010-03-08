/*
 * $Log: PlayerButton.java,v $
 * Revision 1.6  2002/10/15 10:09:51  koji
 * ��������Pass������ȡ�Team/�����̥ơ��֥��ɲ�
 *
 * Revision 1.5  2002/10/11 10:41:40  koji
 * �Х�����(HeteroParam),GoalȽ������
 *
 * Revision 1.4  2002/10/10 09:43:38  koji
 * ͥ���ΰ�η׻��β��� (Direction-Distance Table)
 *
 * Revision 1.3  2002/09/05 10:21:08  taku-ako
 * �إƥ�ץ쥤�䡼���б�
 *
 * Revision 1.2  2002/08/30 10:47:34  koji
 * bmpviewer����Jimi�б�
 *
 * Revision 1.1.1.1  2002/03/01 14:12:52  koji
 * CVS�����
 *
 */

package soccerscope.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.border.EtchedBorder;

import soccerscope.model.Param;
import soccerscope.model.Player;
import soccerscope.util.Color2;

public class PlayerButton extends JButton {

	private Player player;
	private FontMetrics fm;
	private int playerR;
	private int player2R;

	public PlayerButton(Player player) {
		super();
		this.player = player;
		playerR = (int) (player.getPlayerSize() * 3.6 * 8.0);
		player2R = playerR * 2;
		setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		setBorderPainted(true);
	}

	public void setPlayer(Player player) {
		this.player = player;
		repaint();
	}

	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	public Dimension getMinimumSize() {
		return new Dimension(70, 40);
	}

	public void paintComponent(Graphics g) {
		if (fm == null) {
			fm = this.getFontMetrics(this.getFont());
		}
		Dimension d = getSize();

		g.setColor(Color2.forestGreen);
		g.fillRect(0, 0, d.width, d.height);

		if (!player.isEnable())
			return;

		Font f = g.getFont();
		if (f.isBold()) {
			g.setFont(f.deriveFont(Font.PLAIN));
		}

		int cx = d.width / 2;
		int cy = d.height / 2;
		int x = cx - playerR;
		int y = cy - playerR;

		// fill back
		g.setColor(Color.black);
		g.fillOval(x, y, player2R, player2R);

		Color drawColor = player.getColor();
		if (player.isKicking()) {
			g.setColor(drawColor.darker());
			g.fillOval(x, y, player2R, player2R);
			g.setColor(Color.yellow);
			String s = "KICK";
			g.drawString(s, d.width - (fm.stringWidth(s) + 5), fm.getHeight());
		} else if (player.isCatching()) {
			g.setColor(drawColor.darker());
			g.fillOval(x, y, player2R, player2R);
			g.setColor(Color.yellow);
			String s = "CATCH";
			g.drawString(s, d.width - (fm.stringWidth(s) + 5), fm.getHeight());
		} else if (player.isTackling()) {
			g.setColor(drawColor.darker());
			g.fillOval(x, y, player2R, player2R);
			g.setColor(Color.yellow);
			String s = "TACKLE";
			g.drawString(s, d.width - (fm.stringWidth(s) + 5), fm.getHeight());
		}

		// body
		g.setColor(drawColor);
		g.drawOval(x, y, player2R, player2R);

		// angle_body
		g.fillArc(x, y, player2R, player2R, -((int) player.angle + 90), 180);

		// unum
		g.setColor(Color.white);
		g.drawString(Integer.toString(player.unum), 5, fm.getHeight() + 5);

		// stamina
		if (player.stamina > Param.STAMINA_MAX * Param.EFFORT_INC_THR) {
			g.setColor(Color.green);
		} else if (player.stamina > Param.STAMINA_MAX * Param.EFFORT_DEC_THR) {
			g.setColor(Color.yellow);
		} else {
			g.setColor(Color.red);
		}
		String s = Integer.toString(player.stamina);
		g.drawString(s, d.width - fm.stringWidth(s) - 5, d.height - 5);
	}
}
