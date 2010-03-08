/*
 * $Log: Geometry.java,v $
 * Revision 1.2  2002/10/07 07:35:56  koji
 * Circle2f,Line2f,Rectangle2f�ǥե���ȥ��󥹥ȥ饯���ɲ�
 *
 * Revision 1.1  2002/10/04 10:39:16  koji
 * Tuple,Point,Vector�Υѥå������ѹ���Geometry,Circle,Line,Rectangle�ɲ�
 *
 *
 */

package soccerscope.util.geom;

import java.util.Comparator;

public class Geometry {

	/**
	 * ���٤�[-180.180]������������
	 * 
	 * @param angle
	 *            �������������
	 * @return ���������줿����
	 */
	static public float normalizeAngle(float angle) {
		while (true) {
			if (angle < Math.toDegrees(-Math.PI))
				angle += 2 * Math.toDegrees(Math.PI);
			else if (angle > Math.toDegrees(Math.PI))
				angle -= 2 * Math.toDegrees(Math.PI);
			else
				break;
		}
		return angle;
	}

	/**
	 * ���ꤷ��2�Ĥ�Tuple2f���֥�������t1,t2���¤�Tuple2f���֥������Ȥ��֤�.
	 * 
	 * @param t1
	 *            Tuple2f���֥�������
	 * @param t2
	 *            Tuple2f���֥�������
	 * @return Tuple2f���֥�������
	 */
	public static Tuple2f add(Tuple2f t1, Tuple2f t2) {
		return new Tuple2f(t1.x + t2.x, t1.y + t2.y);
	}

	/**
	 * ���ꤷ��2�Ĥ�Tuple2f���֥�������t1,t2�κ���Tuple2f���֥������Ȥ��֤�.
	 * 
	 * @param t1
	 *            Tuple2f���֥�������
	 * @param t2
	 *            Tuple2f���֥�������
	 * @return Tuple2f���֥�������
	 */
	public static Tuple2f sub(Tuple2f t1, Tuple2f t2) {
		return new Tuple2f(t1.x - t2.x, t1.y - t2.y);
	}

	/**
	 * Tuple2f���֥�������t��������ž����Tuple2f���֥������Ȥ��֤�.
	 * 
	 * @param t
	 *            Tuple2f���֥�������
	 * @return Tuple2f���֥�������
	 */
	public static Tuple2f negate(Tuple2f t) {
		return new Tuple2f(-t.x, -t.y);
	}

	/**
	 * ���ꤷ��Tuple2f���֥�������t��a�Ǽ¿��ܤ�����̤�Tuple2f���֥������Ȥ��֤�.
	 * 
	 * @param a
	 *            ��������
	 * @param t
	 *            Tuple2f���֥�������
	 * @return Tuple2f���֥�������
	 */
	public static Tuple2f scale(float a, Tuple2f t) {
		return new Tuple2f(a * t.x, a * t.y);
	}

	/**
	 * Tuple2f���֥�������t1��a�Ǽ¿��ܤ�����Τ�,t2���¤�Tuple2f���֥������Ȥ��֤�. (=
	 * a*t1 + t2)
	 * 
	 * @param a
	 *            ��������
	 * @param t1
	 *            �������ܤ����Tuple2f���֥�������
	 * @param t2
	 *            �û������Tuple2f���֥�������
	 * @return Tuple2f���֥�������
	 */
	public static Tuple2f scaleAdd(float a, Tuple2f t1, Tuple2f t2) {
		return new Tuple2f(a * t1.x + t2.x, a * t1.y + t2.y);
	}

	class SortX implements Comparator<Tuple2f> {
		public int compare(Tuple2f o1, Tuple2f o2) {
			return Float.compare(o1.x, o2.x);
		}
	}

	class SortY implements Comparator<Tuple2f> {
		public int compare(Tuple2f o1, Tuple2f o2) {
			return Float.compare(o1.y, o2.y);
		}
	}
}
