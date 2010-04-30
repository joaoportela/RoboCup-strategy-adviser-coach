/*
 * $Header: $
 */

package soccerscope.model.agentplan;

import java.util.Hashtable;

public class CommentDB {
	private static Hashtable<Integer, String> commentdb;

	private CommentDB() {
	}

	static {
		commentdb = new Hashtable<Integer, String>();

	}

	public static void clear() {
		commentdb.clear();
	}

	public static boolean contains(int time, int unum_index) {
		return commentdb.containsKey(new Integer(time * 100 + unum_index));
	}

	public static void putComment(String comment, int time, int unum_index) {
		Integer key = new Integer(time * 100 + unum_index);
		if (commentdb.containsKey(key)) {
			String data = commentdb.get(key);
			commentdb.put(key, new StringBuffer().append(data).append("\n")
					.append(comment).toString());
		} else {
			commentdb.put(key, comment);
		}
	}

	public static String getComment(int time, int unum_index) {
		return commentdb.get(new Integer(time * 100 + unum_index));
	}
}
