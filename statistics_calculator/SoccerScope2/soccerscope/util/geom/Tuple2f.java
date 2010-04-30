/*
 * $Log: Tuple2f.java,v $
 * Revision 1.1  2002/10/04 10:39:17  koji
 * Tuple,Point,Vector�Υѥå������ѹ���Geometry,Circle,Line,Rectangle�ɲ�
 *
 * Revision 1.2  2002/10/01 14:11:06  koji
 * support Preference, gif format
 *
 * Revision 1.1  2002/09/02 07:07:38  taku-ako
 * Geometry��Ϣ�򼫺�
 *
 */

package soccerscope.util.geom;

import java.awt.geom.Point2D;

/**
 * 2�Ĥ��ͤ��Ȥ�ɽ�����饹
 */
public class Tuple2f extends Point2D {
	/**
	 * Tuple2f��x��ʬ
	 */
	public float x;
	/**
	 * Tuple2f��y��ʬ
	 */
	public float y;

	/**
	 * x=0, y=0�ǿ�����Tuple2f���������.
	 */
	public Tuple2f() {
		x = 0.0f;
		y = 0.0f;
	}

	/**
	 * x,y���ͤ���ꤷ��Tuple2f���������.
	 * 
	 * @param x
	 *            x����
	 * @param y
	 *            y����
	 */
	public Tuple2f(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * ���ꤷ��Tuple2f���֥�������t��Ʊ���ͤ���Tuple2f���������.
	 * 
	 * @param t
	 *            Tuple2f���֥�������
	 */
	public Tuple2f(Tuple2f t) {
		x = t.x;
		y = t.y;
	}

	/**
	 * Tuple2f��x,y���Ǥ��ͤ����ꤹ��.
	 * 
	 * @param x
	 *            x����
	 * @param y
	 *            y����
	 */
	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Tuple2f��x,y���Ǥ��ͤ����ꤹ��.
	 * 
	 * @param x
	 *            x����
	 * @param y
	 *            y����
	 */
	public void setLocation(double x, double y) {
		set((float) x, (float) y);
	}

	/**
	 * Tuple2f��x���Ǥ��ͤ����ꤹ��.
	 * 
	 * @param x
	 *            x����
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * Tuple2f��y���Ǥ��ͤ����ꤹ��.
	 * 
	 * @param y
	 *            y����
	 */
	public void setY(float y) {
		this.y = y;
	}

	/**
	 * x,y���Ǥ��ͤ�,���ꤵ�줿Tuple2f���֥�������t��x,y���Ǥ�Ʊ���ͤ����ꤹ��.
	 * 
	 * @param t
	 *            Tuple2f���֥�������
	 */
	public void set(Tuple2f t) {
		x = t.x;
		y = t.y;
	}

	/**
	 * x,y���Ǥ��ͤ���ꤷ�������1,2���ܤ˳�Ǽ����.
	 * 
	 * @param t
	 *            float������
	 */
	public void get(float t[]) {
		t[0] = x;
		t[1] = y;
	}

	/**
	 * x���Ǥ��֤�.
	 * 
	 * @return x����
	 */
	public double getX() {
		return x;
	}

	/**
	 * y���Ǥ��֤�.
	 * 
	 * @return y����
	 */
	public double getY() {
		return y;
	}

	/**
	 * ����Tuple2f��,���ꤷ��Tuple2f���֥�������t���ͤ�ä���.
	 * 
	 * @param t
	 *            Tuple2f���֥�������
	 */
	public void add(Tuple2f t) {
		x += t.x;
		y += t.y;
	}

	/**
	 * ����Tuple2f���ͤ�,���ꤷ��2�Ĥ�Tuple2f���֥�������t1,t2���¤����ꤹ��.
	 * 
	 * @param t1
	 *            Tuple2f���֥�������
	 * @param t2
	 *            Tuple2f���֥�������
	 */
	public void add(Tuple2f t1, Tuple2f t2) {
		x = t1.x + t2.x;
		y = t1.y + t2.y;
	}

	/**
	 * ����Tuple2f����,���ꤷ��Tuple2f���֥�������t���ͤ��.
	 * 
	 * @param t
	 *            Tuple2f���֥�������
	 */
	public void sub(Tuple2f t) {
		x -= t.x;
		y -= t.y;
	}

	/**
	 * ����Tuple2f���ͤ�,���ꤷ��2�Ĥ�Tuple2f���֥�������t1,t2�κ������ꤹ��.
	 * 
	 * @param t1
	 *            Tuple2f���֥�������
	 * @param t2
	 *            Tuple2f���֥�������
	 */
	public void sub(Tuple2f t1, Tuple2f t2) {
		x = t1.x - t2.x;
		y = t1.y - t2.y;
	}

	/**
	 * ����Tuple2f���ͤ�������ž������.
	 */
	public void negate() {
		x = -x;
		y = -y;
	}

	/**
	 * ����Tuple2f���ͤ�,Tuple2f���֥�������t��������ž�����ͤ����ꤹ��.
	 * 
	 * @param t
	 *            Tuple2f���֥�������
	 */
	public void negate(Tuple2f t) {
		x = -t.x;
		y = -t.y;
	}

	/**
	 * ����Tuple2f���֥������Ȥ�a�Ǽ¿��ܤ���.
	 * 
	 * @param a
	 *            ��������
	 */
	public void scale(float a) {
		x *= a;
		y *= a;
	}

	/**
	 * ����Tuple2f���ͤ�,���ꤷ��Tuple2f���֥�������t��a�Ǽ¿��ܤ�����̤����ꤹ��.
	 * 
	 * @param a
	 *            ��������
	 * @param t
	 *            Tuple2f���֥�������
	 */
	public void scale(float a, Tuple2f t) {
		x = a * t.x;
		y = a * t.y;
	}

	/**
	 * ����Tuple2f���֥������Ȥ�a�Ǽ¿��ܤ�,t��ä���. (= a*this + t)
	 * 
	 * @param a
	 *            ��������
	 * @param t
	 *            �û������Tuple2f���֥�������
	 */
	public void scaleAdd(float a, Tuple2f t) {
		x = a * x + t.x;
		y = a * y + t.y;
	}

	/**
	 * ����Tuple2f���ͤ�,Tuple2f���֥�������t1��a�Ǽ¿��ܤ�����Τ�,t2���¤� ���ꤹ��. (=
	 * a*t1 + t2)
	 * 
	 * @param a
	 *            ��������
	 * @param t1
	 *            �������ܤ����Tuple2f���֥�������
	 * @param t2
	 *            �û������Tuple2f���֥�������
	 */
	public void scaleAdd(float a, Tuple2f t1, Tuple2f t2) {
		x = a * t1.x + t2.x;
		y = a * t1.y + t2.y;
	}

	/**
	 * Tuple2f���֥�������t�Ȥ���Tuple2f���֥������Ȥγ����Ǥ��ͤ������,�����֤�.
	 * 
	 * @param t
	 *            ����оݤȤʤ�Tuple2f���֥�������
	 */
	public boolean equals(Tuple2f t) {
		return x == t.x && y == t.y;
	}

	/**
	 * ���֥�������t��Tuple2f���֥������ȤǤ���,���Ĥ���Tuple2f���֥������Ȥγ����Ǥ�
	 * �ͤ������,�����֤�.
	 * 
	 * @param o
	 *            ����оݤȤʤ륪�֥�������
	 */
	public boolean equals(Object o) {
		return (o instanceof Tuple2f) && equals((Tuple2f) o);
	}

	/**
	 * ����Tuple2f��,Tuple2f���֥�������t�λԳ���Υ���ѥ�᡼��epsilon�ʲ�
	 * �Ǥ��ä����,�����֤�.�Գ���Υ=MAX[abs(x1-x2), abs(y1-y2)]
	 * 
	 * @param t
	 *            ����оݤȤʤ�Tuple2f���֥�������
	 * @param epsilon
	 *            ����
	 */
	public boolean epsilonEquals(Tuple2f t, float epsilon) {
		return (Math.abs(t.x - this.x) <= epsilon)
				&& (Math.abs(t.y - this.y) <= epsilon);
	}

	/**
	 * ����Tuple2f���ͤ�,[min , max]���ϰϤ˥����פ���.
	 * 
	 * @param min
	 *            ����
	 * @param max
	 *            ���
	 */
	public void clamp(float min, float max) {
		clampMin(min);
		clampMax(max);
	}

	/**
	 * ����Tuple2f���ͤ�,[min , ��]���ϰϤ˥����פ���.
	 * 
	 * @param min
	 *            ����
	 */
	public void clampMin(float min) {
		if (x < min)
			x = min;
		if (y < min)
			y = min;
	}

	/**
	 * ����Tuple2f���ͤ�,[-�� , max]���ϰϤ˥����פ���.
	 * 
	 * @param max
	 *            ���
	 */
	public void clampMax(float max) {
		if (x > max)
			x = max;
		if (y > max)
			y = max;
	}

	/**
	 * ����Tuple2f��x���Ǥ��ͤ�,[min , max]���ϰϤ˥����פ���.
	 * 
	 * @param min
	 *            ����
	 * @param max
	 *            ���
	 */
	public void clampX(float min, float max) {
		clampMinX(min);
		clampMaxX(max);
	}

	/**
	 * ����Tuple2f��x���Ǥ��ͤ�,[min , ��]���ϰϤ˥����פ���.
	 * 
	 * @param min
	 *            ����
	 */
	public void clampMinX(float min) {
		if (x < min)
			x = min;
	}

	/**
	 * ����Tuple2f��x���Ǥ��ͤ�,[-�� , max]���ϰϤ˥����פ���.
	 * 
	 * @param max
	 *            ���
	 */
	public void clampMaxX(float max) {
		if (x > max)
			x = max;
	}

	/**
	 * ����Tuple2f��y���Ǥ��ͤ�,[min , max]���ϰϤ˥����פ���.
	 * 
	 * @param min
	 *            ����
	 * @param max
	 *            ���
	 */
	public void clampY(float min, float max) {
		clampMinY(min);
		clampMaxY(max);
	}

	/**
	 * ����Tuple2f��y���Ǥ��ͤ�,[min , ��]���ϰϤ˥����פ���.
	 * 
	 * @param min
	 *            ����
	 */
	public void clampMinY(float min) {
		if (y < min)
			y = min;
	}

	/**
	 * ����Tuple2f��y���Ǥ��ͤ�,[-�� , max]���ϰϤ˥����פ���.
	 * 
	 * @param max
	 *            ���
	 */
	public void clampMaxY(float max) {
		if (y > max)
			y = max;
	}

	/**
	 * ����Tuple2f�γ����Ǥ��ͤ��Ȥ��ͤ������ͤ����ꤹ��.
	 */
	public void absolute() {
		if (x < 0.0)
			x = -x;
		if (y < 0.0)
			y = -y;
	}

	/**
	 * Tuple2f��ʸ����ɽ�����֤�
	 */
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
