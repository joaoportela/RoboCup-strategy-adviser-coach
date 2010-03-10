/*
 * $Log: Line2f.java,v $
 * Revision 1.4  2002/10/11 10:41:38  koji
 * �Х�����(HeteroParam),GoalȽ������
 *
 * Revision 1.3  2002/10/10 09:43:36  koji
 * ͥ���ΰ�η׻��β��� (Direction-Distance Table)
 *
 * Revision 1.2  2002/10/07 07:35:56  koji
 * Circle2f,Line2f,Rectangle2f�ǥե���ȥ��󥹥ȥ饯���ɲ�
 *
 * Revision 1.1  2002/10/04 10:39:16  koji
 * Tuple,Point,Vector�Υѥå������ѹ���Geometry,Circle,Line,Rectangle�ɲ�
 *
 * Revision 1.4  2002/09/02 07:18:22  koji
 * *** empty log message ***
 *
 * Revision 1.2  2002/09/02 07:06:11  taku-ako
 * Geometry��Ϣ�Υ饤�֥������
 *
 * Revision 1.1.1.1  2002/03/01 14:12:53  koji
 * CVS�����
 *
 */

package soccerscope.util.geom;

public class Line2f {

	public final static float EPS = 1.0e-3f;

	/* Ax + By + C = 0 */
	public float A;
	/** < ľ������η��� */
	public float B;
	/** < ľ������η��� */
	public float C;

	/** < ľ������η��� */

	// public static void main(String args[]) {
	// Point2f o = new Point2f();
	// Point2f a = new Point2f(1,1);
	// Point2f b = new Point2f(1,0);
	// Point2f c = new Point2f(0,1);
	// Point2f d = new Point2f(-1,1);
	// Line2f l1 = new Line2f(o, a);
	// Line2f l2 = new Line2f(o, d);
	// Line2f l3 = new Line2f(o, b);
	// Line2f l4 = new Line2f(o, c);
	// Line2f l5 = new Line2f(o, 45);
	// Line2f l6 = new Line2f(o, -45);
	// System.out.println(l1);
	// System.out.println(l2);
	// System.out.println(l1.intersection(l2));
	// System.out.println(l1.projectpoint(b));
	// System.out.println(l1.projectpoint(c));
	// System.out.println(l3);
	// System.out.println(l4);
	// System.out.println(l5);
	// System.out.println(l6);
	// }

	/**
	 * ���󥹥ȥ饯��
	 */
	public Line2f() {
		this.A = 0;
		this.B = 0;
		this.C = 0;
	}

	/**
	 * ���󥹥ȥ饯�� ����A,B,C��ľ�����������
	 * 
	 * @param A
	 *            ľ������η���
	 * @param B
	 *            ľ������η���
	 * @param C
	 *            ľ������η���
	 */
	public Line2f(float A, float B, float C) {
		this.A = A;
		this.B = B;
		this.C = C;
	}

	/**
	 * ���󥹥ȥ饯�� ��p1,p2���̤�ľ�����������
	 * 
	 * @param p1
	 *            ľ�������������
	 * @param p2
	 *            ľ�������������
	 */
	public Line2f(Point2f p1, Point2f p2) {
		A = p1.y - p2.y;
		B = p2.x - p1.x;
		C = -B * p1.y - A * p1.x;
	}

	/**
	 * ���󥹥ȥ饯�� ��p���̤�,angle�٤η�����ľ����������� angle��+/-90�����������ʤ�
	 * 
	 * @param p
	 *            ľ�������������
	 * @param angle
	 *            ���� (��)
	 */
	public Line2f(Point2f p, float angle) {
		float m = (float) Math.tan(Math.toRadians(angle));
		A = -m;
		B = 1;
		C = -p.y + m * p.x;
	}

	/**
	 * ���ԡ����󥹥ȥ饯��
	 * 
	 * @param l
	 *            ���ԡ���
	 */
	public Line2f(Line2f l) {
		this.A = l.A;
		this.B = l.B;
		this.C = l.C;
	}

	/**
	 * y���б�����x���֤�
	 * 
	 * @param y
	 *            y����
	 * @return y�ˤ�äƷ�ޤ�x����
	 */
	public float getX(float y) {
		return -B / A * y - C / A;
	}

	/**
	 * x���б�����y���֤�
	 * 
	 * @param x
	 *            ����
	 * @return x�ˤ�äƷ�ޤ�y����
	 */
	public float getY(float x) {
		return -A / B * x - C / B;
	}

	/**
	 * x���Ҥ��֤�
	 * 
	 * @return x����
	 */
	public float xIntercept() {
		return -C / A;
	}

	/**
	 * y���Ҥ��֤�
	 * 
	 * @return y����
	 */
	public float yIntercept() {
		return -C / B;
	}

	/**
	 * ���٤��֤�
	 * 
	 * @return �����γ���(��)
	 */
	public float angle() {
		return (float) Math.toDegrees(Math.atan(-A / B));
	}

	/**
	 * ľ��l�Ȥγ��٤��֤�
	 * 
	 * @param l
	 *            ľ��
	 * @return ľ��l�Ȥγ���
	 */
	public float angle(Line2f l) {
		return (float) Math.toDegrees(Math.atan((A * l.B - l.A * B)
				/ (A * l.A + B * l.B)));
	}

	/**
	 * ��p����ε�Υ���֤�
	 * 
	 * @param p
	 *            ��
	 * @return ��p����ε�Υ
	 */
	public float dist(Point2f p) {
		return (float) (Math.abs(A * p.x + B * p.y + C) / Math.sqrt(A * A + B
				* B));
	}

	/**
	 * ����ľ����ˤ��뤫�ɤ�����Ƚ�ꤹ�롣ľ����ˤ����硢true���֤�
	 * 
	 * @param p
	 *            ��
	 * @return ľ����ˤ��뤫�ɤ���
	 */
	public boolean online(Point2f p) {
		return Math.abs(A * p.x + B * p.y + C) < EPS;
	}

	/**
	 * ��p����������(�����򲼤?���Ȥ��θ���)���֤�
	 * 
	 * @param p
	 *            ��
	 * @return �����
	 */
	public Point2f projectpoint(Point2f p) {
		return intersection(perpendicular(p));
	}

	/**
	 * ľ��l�ȸ��뤫�ɤ������֤�
	 * 
	 * @param l
	 *            ľ��
	 * @return ľ��l�ȸ��뤫�ɤ���
	 */
	public boolean intersect(Line2f l) {
		float slope = A * l.B - l.A * B;
		return slope != 0;
	}

	/**
	 * ľ��l�Ȥθ������֤�
	 * 
	 * @param l
	 *            ľ��
	 * @return ����
	 */
	public Point2f intersection(Line2f l) {
		float slope = A * l.B - l.A * B;
		// ʿ�ԤǤ���
		if (slope == 0) {
			// ���פ��Ƥ���
			if (B * l.C - l.B * C == 0 && l.A * C - A * l.C == 0) {
				return randomSample();
			} else {
				return new Point2f();
			}
		}

		return new Point2f((B * l.C - l.B * C) / slope, (l.A * C - A * l.C)
				/ slope);
	}

	/**
	 * ��p���̤��ľ�����֤�
	 * 
	 * @param p
	 *            ��
	 * @return ��p���̤��ľ��
	 */
	public Line2f perpendicular(Point2f p) {
		return new Line2f(B, -A, A * p.y - B * p.x);
	}

	/**
	 * ľ����Υ��������(0<=x<1)���֤�
	 */
	public Point2f randomSample() {
		if (A == 0)
			return new Point2f((float) Math.random(), -C / B);
		else if (B == 0)
			return new Point2f(-C / A, (float) Math.random());
		float random_x = (float) Math.random();
		return new Point2f(random_x, getY(random_x));
	}

	/**
	 * ʸ����ɽ�����֤�
	 * 
	 * @return ʸ����
	 */
	public String toString() {
		if (A == 0)
			return "y = " + Float.toString(-C / B);
		else if (B == 0)
			return "x = " + Float.toString(-C / A);
		return A + "x + " + B + "y + " + C + " = 0";
	}
}
