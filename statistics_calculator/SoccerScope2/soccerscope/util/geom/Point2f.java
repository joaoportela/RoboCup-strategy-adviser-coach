/*
 * $Log: Point2f.java,v $
 * Revision 1.1  2002/10/04 10:39:17  koji
 * Tuple,Point,Vector�Υѥå������ѹ���Geometry,Circle,Line,Rectangle�ɲ�
 *
 * Revision 1.1  2002/09/02 07:07:38  taku-ako
 * Geometry��Ϣ�򼫺�
 *
 */

package soccerscope.util.geom;

import java.awt.geom.Point2D;

/**
 * 2����ʿ�̾������ɽ�����饹
 */
public class Point2f extends Tuple2f {

	/**
	 * x=0, y=0�ǿ�����Point2f���������.
	 */
	public Point2f() {
		super();
	}

	/**
	 * x,y���ͤ���ꤷ��Point2f���������.
	 * 
	 * @param x
	 *            x����
	 * @param y
	 *            y����
	 */
	public Point2f(float x, float y) {
		super(x, y);
	}

	/**
	 * ���ꤷ��Point2f���֥�������p��Ʊ���ͤ���Point2f���������.
	 * 
	 * @param p
	 *            Point2f���֥�������
	 */
	public Point2f(Point2f p) {
		super(p);
	}

	/**
	 * ���ꤷ��Tuple2f���֥�������t��Ʊ���ͤ���Point2f���������.
	 * 
	 * @param t
	 *            Tuple2f���֥�������
	 */
	public Point2f(Tuple2f t) {
		super(t);
	}

	/**
	 * ����������p�Ȥδ֤ε�Υ���֤�.
	 * 
	 * @param p
	 *            Point2f���֥�������
	 * @return �����֤ε�Υ
	 */
	public float distance(Point2f p) {
		return dist(p);
	}

	/**
	 * ����������p�Ȥδ֤ε�Υ���֤�.
	 * 
	 * @param p
	 *            Point2f���֥�������
	 * @return �����֤ε�Υ
	 */
	public float dist(Point2f p) {
		return (float) Math.sqrt(distanceSquared(p));
	}

	/**
	 * ����������p�Ȥδ֤ε�Υ��ʿ����֤�.
	 * 
	 * @param p
	 *            Point2f���֥�������
	 * @return �����֤ε�Υ��ʿ��
	 */
	public float distanceSquared(Point2f p) {
		float dx = p.x - x;
		float dy = p.y - y;
		return dx * dx + dy * dy;
	}

	/**
	 * ����������p�Ȥδ֤Υޥ�ϥå����Υ (�Գ���Υ)���֤�.
	 * 
	 * @param p
	 *            Point2f���֥�������
	 * @return �����֤Υޥ�ϥå����Υ
	 */
	public float distanceL1(Point2f p) {
		return (float) (Math.abs(x - p.x) + Math.abs(y - p.y));
	}

	/**
	 * ����������p�Ȥδ֤Υ������׵�Υ���֤�.
	 * 
	 * @param p
	 *            Point2f���֥�������
	 * @return �����֤Υ������׵�Υ
	 */
	public float distanceLinf(Point2f p) {
		return (float) Math.max(Math.abs(x - p.x), Math.abs(y - p.y));
	}

	/**
	 * ������������p�ؤ�������֤�.
	 * 
	 * @param p
	 *            Point2f���֥�������
	 * @return ��p�ؤ���� (��)
	 */
	public float direction(Point2f p) {
		return dir(p);
	}

	/**
	 * ������������p�ؤ�������֤�.
	 * 
	 * @param p
	 *            Point2f���֥�������
	 * @return ��p�ؤ���� (��)
	 */
	public float dir(Point2f p) {
		return (float) Math.toDegrees(Math.atan2(p.y - y, p.x - x));
	}

	/**
	 * ���������鸫����p1��p2�δ֤γ��٤��֤�.
	 * 
	 * @param p1
	 *            Point2f���֥�������
	 * @param p2
	 *            Point2f���֥�������
	 * @return �����֤γ��� (��)
	 */
	public float angle(Point2f p1, Point2f p2) {
		return Geometry.normalizeAngle(dir(p2) - dir(p1));
	}

	/**
	 * ��������,��org���濴��th�ٲ�ž�����������֤�.
	 * 
	 * @param p
	 *            ��ž���濴
	 * @param th
	 *            ��ž����
	 * @return ��org���濴��th�ٲ�ž��������
	 */
	public Point2f rotate(Point2f org, float th) {
		Vector2f tmp = new Vector2f(org, this);
		tmp = tmp.rotate(th);

		return new Point2f(tmp.x + org.x, tmp.y + org.y);
	}

	/**
	 * ��p��ꤳ������x���Ǥ��ͤ��礭�����,true���֤�.
	 * 
	 * @param p
	 *            Point2f���֥�������
	 * @return p������ˤ����true
	 */
	public boolean inFrontOf(Point2f p) {
		return x > p.x;
	}

	/**
	 * ��p��ꤳ������x���Ǥ��ͤ����������,true���֤�.
	 * 
	 * @param p
	 *            Point2f���֥�������
	 * @return p����ˤ����true
	 */
	public boolean isBehind(Point2f p) {
		return x < p.x;
	}

	/**
	 * �˺�ɸ������ʸ������֤�
	 * 
	 * @return �˺�ɸ������ʸ����
	 */
	public String toPolar() {
		Point2f p = new Point2f();
		return "(" + p.dist(this) + ", " + p.dir(this) + ")";
	}

	/**
	 * �˺�ɸ�������������������
	 * 
	 * @param r
	 *            �˺�ɸ��Ⱦ����ʬ
	 * @param th
	 *            �˺�ɸ�γ�����ʬ
	 * @return Point2f���֥�������
	 */
	public static Point2f polar2point(float r, float th) {
		double d = Math.toRadians(th);
		return new Point2f((float) (r * Math.cos(d)), (float) (r * Math.sin(d)));
	}

	/**
	 * ����Point2f��Point2D.Float�������֤�
	 */
	public Point2D.Float toPoint2DF() {
		return new Point2D.Float(x, y);
	}
}
