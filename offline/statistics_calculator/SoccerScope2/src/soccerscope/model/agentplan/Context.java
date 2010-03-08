/*
 * $Header: $
 */

package soccerscope.model.agentplan;

import java.awt.Color;
import java.util.StringTokenizer;

import soccerscope.util.geom.Point2f;

public class Context {

	private StringTokenizer tokenizer;
	private String currentToken;

	public Context(String text) {
		tokenizer = new StringTokenizer(text);
		nextToken();
	}

	public String nextToken() {
		if (tokenizer.hasMoreTokens()) {
			currentToken = tokenizer.nextToken();
		} else {
			currentToken = null;
		}
		return currentToken;
	}

	public String nextToken(String delim) {
		if (tokenizer.hasMoreTokens()) {
			currentToken = tokenizer.nextToken(delim);
		} else {
			currentToken = null;
		}
		return currentToken;
	}

	public String currentToken() {
		return currentToken;
	}

	public void skipToken(String token) throws ParseException {
		if (!token.equals(currentToken)) {
			throw new ParseException("Warning: " + token + " is expected, but "
					+ currentToken + " is found.");
		}
		nextToken();
	}

	public int currentNumber() throws ParseException {
		int number = 0;
		try {
			number = Integer.parseInt(currentToken);
		} catch (NumberFormatException e) {
			throw new ParseException("Warning: " + e);
		}
		return number;
	}

	public float currentFloat() throws ParseException {
		float number = 0;
		try {
			number = Float.parseFloat(currentToken);
		} catch (NumberFormatException e) {
			throw new ParseException("Warning: " + e);
		}
		return number;
	}

	public Point2f currentPoint() throws ParseException {
		float x, y;
		String point = currentToken.replace('(', ' ');
		point = point.replace(')', ' ');
		point = point.replace(',', ' ');
		point = point.trim();
		try {
			x = Float.parseFloat(point.substring(0, point.lastIndexOf(' ')));
			y = Float.parseFloat(point.substring(point.lastIndexOf(' ') + 1));
		} catch (NumberFormatException e) {
			throw new ParseException("Warning: " + e);
		}
		return new Point2f(x, y);
	}

	public boolean isPointToken() {
		if (currentToken != null && currentToken.charAt(0) == '('
				&& currentToken.charAt(currentToken.length() - 1) == ')')
			return true;
		return false;
	}

	public boolean isColorToken() {
		if (currentToken() != null && currentToken.charAt(0) == '#')
			return true;
		return false;
	}

	public Color currentColor() throws ParseException {
		int color;
		try {
			color = Integer.parseInt(currentToken.substring(1), 16);
			nextToken();
		} catch (NumberFormatException e) {
			throw new ParseException("Warning: " + e);
		}
		return new Color(color);
	}

	public String currentComment() {
		String comment = currentToken();
		if (comment != null && nextToken("\0") != null)
			comment = comment.concat(currentToken());
		return comment;
	}
}
