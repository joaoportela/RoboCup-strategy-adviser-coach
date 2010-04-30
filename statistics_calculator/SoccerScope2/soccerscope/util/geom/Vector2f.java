/*
 * $Log: Vector2f.java,v $
 * Revision 1.1  2002/10/04 10:39:17  koji
 * Tuple,Point,Vector�Υѥå������ѹ���Geometry,Circle,Line,Rectangle�ɲ�
 *
 * Revision 1.1  2002/09/02 07:07:39  taku-ako
 * Geometry��Ϣ�򼫺�
 *
 */

package soccerscope.util.geom;

/**
 * 2�����Υ٥��ȥ륯�饹
 */
public class Vector2f extends Tuple2f {

	/**
	 * x=0, y=0�ǿ�����Vector2f���������.
	 */
	public Vector2f() {
		super();
	}

	/**
	 * x,y���ͤ���ꤷ��Vector2f���������.
	 * 
	 * @param x
	 *            x����
	 * @param y
	 *            y����
	 */
	public Vector2f(float x, float y) {
		super(x, y);
	}

	/**
	 * ���ꤷ��Vector2f���֥�������v��Ʊ���ͤ���Vector2f���������.
	 * 
	 * @param v
	 *            Vector2f���֥�������
	 */
	public Vector2f(Vector2f v) {
		super(v);
	}

	/**
	 * ���ꤷ��Tuple2f���֥�������t��Ʊ���ͤ���Vecor2f���������.
	 * 
	 * @param t
	 *            Tuple2f���֥�������
	 */
	public Vector2f(Tuple2f t) {
		super(t);
	}

	/**
	 * s�����,e�����Ȥ���٥��ȥ���������.
	 * 
	 * @param s
	 *            ����
	 * @param e
	 *            ����
	 */
	public Vector2f(Point2f s, Point2f e) {
		super(e.x - s.x, e.y - s.y);
	}

	/**
	 * ����Vector2f��v�Ȥ����Ѥ��֤�.
	 * 
	 * @param v
	 *            Vector2f���֥�������
	 * @return ����
	 */
	public float dot(Vector2f v) {
		return x * v.x + y * v.y;
	}

	public float crossProduct(Vector2f v) {
		return (this.x * v.y) - (this.y * v.x);
	}

	/**
	 * ���Υ٥��ȥ���礭�����֤�.
	 * 
	 * @return �礭��
	 */
	public float r() {
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * ���Υ٥��ȥ���礭�����֤�.
	 * 
	 * @return �礭��
	 */
	public float length() {
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * ���Υ٥��ȥ���礭����ʿ����֤�.
	 * 
	 * @return �礭����ʿ��
	 */
	public float lengthSquared() {
		return x * x + y * y;
	}

	/**
	 * ���Υ٥��ȥ��������������Τ��֤�.
	 * 
	 * @return �������٥��ȥ�
	 */
	public Vector2f normalize() {
		float d = r();
		return new Vector2f(x / d, y / d);
	}

	/**
	 * ���Υ٥��ȥ�γ��٤��֤�.
	 * 
	 * @return ���� (��)
	 */
	public float th() {
		return (float) Math.toDegrees(Math.atan2(y, x));
	}

	/**
	 * ���Υ٥��ȥ�ȥ٥��ȥ�v�Ȥγ��٤��֤�
	 * 
	 * @param v
	 *            Vector2f���֥�������
	 * @return ���� (��)
	 */
	public float angle(Vector2f v) {
		return (float) Math.abs(Math.toDegrees(Math.atan2(x * v.y - y * v.x,
				dot(v))));
	}

	/**
	 * ���Υ٥��ȥ��th�ٲ�ž�������٥��ȥ���֤�.
	 * 
	 * @param th
	 *            ��ž����
	 * @return ��ž�������٥��ȥ�
	 */
	public Vector2f rotate(float th) {
		double d = Math.toRadians(th);
		return new Vector2f((float) (x * Math.cos(d) - y * Math.sin(d)),
				(float) (x * Math.sin(d) + y * Math.cos(d)));
	}

	/**
	 * �˺�ɸ������ʸ������֤�
	 * 
	 * @return �˺�ɸ������ʸ����
	 */
	public String toPolar() {
		return "(" + r() + ", " + th() + ")";
	}

	/**
	 * �˺�ɸ����٥��ȥ���������.
	 * 
	 * @param r
	 *            �˺�ɸ��Ⱦ����ʬ
	 * @param th
	 *            �˺�ɸ�γ�����ʬ
	 * @return Vector2f���֥�������
	 */
	public static Vector2f polar2vector(float r, float th) {
		double d = Math.toRadians(th);
		return new Vector2f((float) (r * Math.cos(d)),
				(float) (r * Math.sin(d)));
	}
}
