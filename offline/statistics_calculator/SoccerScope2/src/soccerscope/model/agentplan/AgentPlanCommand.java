/*
 * $Header: $
 */

package soccerscope.model.agentplan;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;

import soccerscope.model.Scene;
import soccerscope.util.geom.Point2f;
import soccerscope.view.layer.Layer;

public class AgentPlanCommand {

	private int level;
	private int index;
	private int type;

	public AgentPlanCommand(int level, int selfUnum) {
		this.level = level;
		this.index = selfUnum - 1;
	}

	public int getLevel() {
		return level;
	}

	public int getType() {
		return type;
	}

	public String getComment() {
		return comment;
	}

	public final static int LINE = 1;
	public final static int RECT = 2;
	public final static int ELLIPSE = 3;
	public final static int POLYLINE = 4;
	public final static int STATE = 5;
	public final static int TARGET = 6;
	public final static int COMMENT = 7;

	private Point2f start = null;
	private Point2f end = null;
	private Point2f topleft = null;
	private Point2f size;
	private ArrayList points = null;
	private int rotate;
	private Point2f targetPos = null;
	private final static Image targetImage = new ImageIcon(ClassLoader
			.getSystemResource("soccerscope/image/BlueDown16.gif")).getImage();
	private int offsetx;
	private int offsety;
	private Point2f commentPos = null;
	private String comment = null;
	private int state;
	private Point2f statePos = null;
	private int stateUnum;
	private final static Image stateImage1 = new ImageIcon(ClassLoader
			.getSystemResource("soccerscope/image/comeon.gif")).getImage();
	private final static Image stateImage2 = new ImageIcon(ClassLoader
			.getSystemResource("soccerscope/image/stop.gif")).getImage();
	private final static Image stateImage3 = new ImageIcon(ClassLoader
			.getSystemResource("soccerscope/image/kaminari.gif")).getImage();
	private final static Image stateImage4 = new ImageIcon(ClassLoader
			.getSystemResource("soccerscope/image/light.gif")).getImage();
	private final static Image stateImage5 = new ImageIcon(ClassLoader
			.getSystemResource("soccerscope/image/star.gif")).getImage();
	private final static Image stateImage6 = new ImageIcon(ClassLoader
			.getSystemResource("soccerscope/image/heart.gif")).getImage();
	private Color color = Color.white;

	public void parse(Context context, Scene scene) throws ParseException {
		// <Line-Command>:="line" <Point> <Point> [<Color>] [<Comment>]
		if (context.currentToken().equals("line")) {
			context.nextToken();
			start = context.currentPoint();
			context.nextToken();
			end = context.currentPoint();

			context.nextToken();
			if (context.isColorToken())
				color = context.currentColor();
			comment = context.currentComment();

			commentPos = end;
			type = LINE;
		}
		// <Rect-Command>:="rect" <Point> <Width> <Height> [<Rotate>] [<Color>]
		// [<Comment>]
		else if (context.currentToken().equals("rect")) {
			context.nextToken();
			topleft = context.currentPoint();
			context.nextToken();
			size = new Point2f();
			size.x = context.currentFloat();
			context.nextToken();
			size.y = context.currentFloat();

			context.nextToken();
			if (context.isColorToken())
				color = context.currentColor();
			comment = context.currentComment();

			commentPos = topleft;
			type = RECT;

		}
		// <Ellipse-Command>:="ellipse" <Point> <Width> <Height> [<Rotate>]
		// [<Color>] [<Comment>]
		else if (context.currentToken().equals("ellipse")) {
			context.nextToken();
			topleft = context.currentPoint();
			context.nextToken();
			size = new Point2f();
			size.x = context.currentFloat();
			context.nextToken();
			size.y = context.currentFloat();

			context.nextToken();
			if (context.isColorToken())
				color = context.currentColor();
			comment = context.currentComment();

			commentPos = topleft;
			type = ELLIPSE;

		}
		// <Polyline-Command>:="polyline" <Point> <Point> <Point>* [<Color>]
		// [<Comment>]
		else if (context.currentToken().equals("polyline")) {
			context.nextToken();
			points = new ArrayList();
			points.add(context.currentPoint());
			context.nextToken();
			Point2f last;
			do {
				last = context.currentPoint();
				points.add(last);
				context.nextToken();
			} while (context.isPointToken());

			if (context.isColorToken())
				color = context.currentColor();
			comment = context.currentComment();

			commentPos = last;
			type = POLYLINE;

		}
		// <rLine-Command>:="rline" <Vetor> [<Color>] [<Comment>]
		else if (context.currentToken().equals("rline")) {
			context.nextToken();
			start = scene.player[index].pos;
			end = context.currentPoint();
			end.add(start);

			context.nextToken();
			if (context.isColorToken())
				color = context.currentColor();
			comment = context.currentComment();

			commentPos = end;
			type = LINE;

		}
		// <rRect-Command>:="rrect" <Vector> <Width> <Height> [<Rotate>]
		// [<Color>] [<Comment>]
		else if (context.currentToken().equals("rrect")) {
			context.nextToken();
			topleft = new Point2f(scene.player[index].pos);
			size = new Point2f();
			size.x = context.currentFloat();
			context.nextToken();
			size.y = context.currentFloat();
			topleft.sub(size);
			size.scale(2);

			context.nextToken();
			if (context.isColorToken())
				color = context.currentColor();
			comment = context.currentComment();

			commentPos = topleft;
			type = RECT;

		}
		// <rEllipse-Command>:="rellipse" <Vector> <Width> <Height> [<Rotate>]
		// [<Comment>]
		else if (context.currentToken().equals("rellipse")) {
			context.nextToken();
			topleft = new Point2f(scene.player[index].pos);
			size = new Point2f();
			size.x = context.currentFloat();
			context.nextToken();
			size.y = context.currentFloat();
			topleft.sub(size);
			size.scale(2);

			context.nextToken();
			if (context.isColorToken())
				color = context.currentColor();
			comment = context.currentComment();

			commentPos = topleft;
			type = ELLIPSE;

		}
		// <rPolyline-Command>:="rpolyline" <Vector> <Vector>* [<Color>]
		// [<Comment>]
		else if (context.currentToken().equals("rpolyline")) {
			context.nextToken();
			points = new ArrayList();
			points.add(scene.player[index].pos);
			Point2f last = new Point2f(scene.player[index].pos);
			do {
				Point2f end = context.currentPoint();
				end.add(last);
				points.add(end);
				last.set(end);
				context.nextToken();
			} while (context.isPointToken());

			if (context.isColorToken())
				color = context.currentColor();
			comment = context.currentComment();

			commentPos = last;
			type = POLYLINE;

		}
		// <sLine-Command>:="sline" <Point> [<Color>] [<Comment>]
		else if (context.currentToken().equals("sline")) {
			context.nextToken();
			start = scene.player[index].pos;
			end = context.currentPoint();

			context.nextToken();
			if (context.isColorToken())
				color = context.currentColor();
			comment = context.currentComment();

			commentPos = end;
			type = LINE;

		}
		// <sPolyline-Command>:="spolyline" <Point> <Point>* [<Color>]
		// [<Comment>]
		else if (context.currentToken().equals("spolyline")) {
			context.nextToken();
			points = new ArrayList();
			points.add(scene.player[index].pos);
			context.nextToken();
			Point2f last;
			do {
				last = context.currentPoint();
				points.add(last);
				context.nextToken();
			} while (context.isPointToken());

			if (context.isColorToken())
				color = context.currentColor();
			comment = context.currentComment();

			commentPos = last;
			type = POLYLINE;

		}
		// <State-Command>:="state" <Number> [<Unum>]
		else if (context.currentToken().equals("state")) {
			context.nextToken();
			state = context.currentNumber();
			context.nextToken();
			if (context.currentToken() != null) {
				stateUnum = context.currentNumber();
			} else {
				// unum���ʤ����ϼ�ʬ
				stateUnum = index + 1;
			}
			statePos = new Point2f(scene.player[stateUnum - 1].pos);
			type = STATE;

		}
		// <Target-Command>:="target" <Unum>| "target" <Point>
		else if (context.currentToken().equals("target")) {
			context.nextToken();
			if (context.isPointToken()) {
				targetPos = context.currentPoint();
				offsetx = -targetImage.getWidth(null) / 2;
				offsety = -targetImage.getHeight(null);
			} else {
				targetPos = scene.player[context.currentNumber() - 1].pos;
				offsetx = -targetImage.getWidth(null) / 2;
				offsety = -targetImage.getHeight(null);
			}
			type = TARGET;

		}
		// <AgentAction-Command>:="(dash" <Number>")"
		else if (context.currentToken().equals("(dash")) {
			// ���ޥ�ɰ���ν���
			String arg1 = context.nextToken();
			arg1 = arg1.replace(')', ' ');
			arg1 = arg1.trim();
			try {
				scene.player[index].acc = scene.player[index]
						.getDashAccelerate(Float.parseFloat(arg1));
			} catch (NumberFormatException ne) {
				return;
			}

		}
		// <AgentAction-Command>:="(kick" <Number> <Number>")"
		else if (context.currentToken().equals("(kick")) {
			// ���ޥ�ɰ���ν���
			String arg1 = context.nextToken();
			String arg2 = context.nextToken();
			if (arg2 == null) {
				throw new ParseException("kick command: no direction");
			}
			arg1 = arg1.replace(')', ' ');
			arg1 = arg1.trim();
			arg2 = arg2.replace(')', ' ');
			arg2 = arg2.trim();

			// ���å��ˤ���®�٤η׻�
			if (scene.player[index].isKickable(scene.ball.pos)) {
				try {
					scene.ball.acc = scene.player[index].getKickAccelerate(
							scene.ball.pos, Float.parseFloat(arg1), Float
									.parseFloat(arg2));
				} catch (NumberFormatException ne) {
					return;
				}
			}

		}
		// <AgentAction-Command>:="(catch" <Number> ")"
		else if (context.currentToken().equals("(catch")) {
			// ���ޥ�ɰ���ν���
			String arg1 = context.nextToken();
			arg1 = arg1.replace(')', ' ');
			arg1 = arg1.trim();
			try {
				scene.player[index].catchDir = Float.parseFloat(arg1);
			} catch (NumberFormatException ne) {
				return;
			}
		}

		// <AgentAction-Command>:="(say" <Comment>")"
		else if (context.currentToken().equals("(say")) {
			scene.player[index].sayMessage = context.nextToken("\0");

		}
		// <AgentAction-Command>:="(turn" <Number>")"
		else if (context.currentToken().equals("(turn")) {

		}
		// <AgentAction-Command>:="(turn_neck" <Number>")"
		else if (context.currentToken().equals("(turn_neck")) {

		}
		// <Comment>
		else {
			comment = context.currentComment();
			commentPos = null;
			// commentPos = scene.player[index].pos;
			type = COMMENT;
		}
	}

	public void execute(Graphics g, Layer layer) {
		g.setColor(color);

		if (type == LINE) {
			layer.drawLine(g, start, end);
		} else if (type == RECT) {
			if (rotate == 0)
				layer.drawRect(g, topleft, size);
			else
				layer.drawRect(g, topleft, size, rotate);
		} else if (type == ELLIPSE) {
			if (rotate == 0)
				layer.drawOval(g, topleft, size);
			else
				layer.drawOval(g, topleft, size, rotate);
		} else if (type == POLYLINE) {
			Iterator it = points.iterator();
			Point2f start = new Point2f((Point2f) it.next());
			while (it.hasNext()) {
				Point2f end = (Point2f) it.next();
				layer.drawLine(g, start, end);
				start.set(end);
			}
		} else if (type == STATE) {
			switch (state) {
			case 1:
				layer.drawImage(g, stateImage1, statePos, -stateImage1
						.getWidth(null) / 2, -stateImage1.getHeight(null) / 2);
				break;
			case 2:
				layer.drawImage(g, stateImage2, statePos, -stateImage2
						.getWidth(null) / 2, -stateImage2.getHeight(null) / 2);
				break;
			case 3:
				layer.drawImage(g, stateImage3, statePos, -stateImage3
						.getWidth(null), -stateImage3.getHeight(null));
				break;
			default:
			}

		} else if (type == TARGET) {
			layer.drawImage(g, targetImage, targetPos, offsetx, offsety);
		}
		if (comment != null && commentPos != null)
			layer.drawString(g, comment, commentPos);
	}
}
