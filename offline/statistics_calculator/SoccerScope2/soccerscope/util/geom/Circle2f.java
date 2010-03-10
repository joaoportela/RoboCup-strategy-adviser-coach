/*
 * $Log: Circle2f.java,v $
 * Revision 1.3  2002/10/10 09:43:35  koji
 * ͥ���ΰ�η׻��β��� (Direction-Distance Table)
 *
 * Revision 1.2  2002/10/07 07:35:56  koji
 * Circle2f,Line2f,Rectangle2f�ǥե���ȥ��󥹥ȥ饯���ɲ�
 *
 * Revision 1.1  2002/10/04 10:39:16  koji
 * Tuple,Point,Vector�Υѥå������ѹ���Geometry,Circle,Line,Rectangle�ɲ�
 *
 *
 */

package soccerscope.util.geom;

public class Circle2f {

	public final static float EPS = 1.0e-3f;

	/* (x - a)^2 + (y - b)^2 = r^2 */
	private float a;
	/** < �ߤ��濴��x��ɸ */
	private float b;
	/** < �ߤ��濴��y��ɸ */
	private float r;

	/** < �ߤ�Ⱦ�� */

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
	// Circle2f c1 = new Circle2f(o, 1);
	// Circle2f c2 = new Circle2f(new Point2f(0, 2), 1);
	// Point2f p[] = c2.intersection(l4);
	// if (p.length >= 1)
	// System.out.println(p[0]);
	// if (p.length >= 2)
	// System.out.println(p[1]);
	// Line2f l[] = c1.tangentLine(a);
	// if (l.length >= 1)
	// System.out.println(l[0]);
	// if (l.length >= 2)
	// System.out.println(l[1]);
	// Circle2f c3 = new Circle2f(a, 1);
	// Line2f l5 = new Line2f(new Point2f(1, -1), 90.1f);
	// System.out.println(l5);
	// Point2f p2[] = c3.intersection(l5);
	// if (p2.length == 0)
	// System.out.println(l5.projectpoint(a));
	// if (p2.length >= 1)
	// System.out.println(p2[0]);
	// if (p2.length >= 2)
	// System.out.println(p2[1]);
	// }

	/**
	 * �ߥ��֥������Ȥ���
	 */
	public Circle2f() {
		a = 0;
		b = 0;
		r = 0;
	}

	/**
	 * Ⱦ��radius,��c���濴�Ȥ����ߥ��֥������Ȥ���
	 * 
	 * @param c
	 *            �ߤ��濴
	 * @param radius
	 *            �ߤ�Ⱦ��
	 */
	public Circle2f(Point2f c, float radius) {
		a = c.x;
		b = c.y;
		r = radius;
	}

	/**
	 * �ߤ��濴�����֤�
	 * 
	 * @return �ߤ��濴��
	 */
	public Point2f getCenterPoint() {
		return new Point2f(a, b);
	}

	/**
	 * �ߤ�Ⱦ�¤��֤�
	 */
	public float getRadius() {
		return r;
	}

	/**
	 * �ߤ��濴�������ꤹ��
	 * 
	 * @param c
	 *            �ߤ��濴��
	 */
	public void setCenterPoint(Point2f c) {
		a = c.x;
		b = c.y;
	}

	/**
	 * �ߤ�Ⱦ�¤����ꤹ��
	 * 
	 * @param r
	 *            �ߤ�Ⱦ��
	 */
	public void setRadius(float r) {
		this.r = r;
	}

	/**
	 * ���α߼������p�����뤫�ɤ�����Ƚ�ꤹ�롣�߼���ˤ����硢true���֤�
	 * 
	 * @param p
	 *            Ǥ�դ���
	 * @return �߼������p�����뤫�ɤ���
	 */
	public boolean online(Point2f p) {
		float pxa = (p.x - a);
		float pyb = (p.y - b);
		return ((float) Math.abs(pxa * pxa + pyb * pyb - r * r) <= EPS);
	}

	/**
	 * �������p�����뤫�ɤ�����Ƚ�ꤹ�롣����ˤ����硢true���֤�
	 * 
	 * @param p
	 *            Ǥ�դ���
	 * @return �������p�����뤫�ɤ���
	 */
	public boolean contains(Point2f p) {
		float pxa = (p.x - a);
		float pyb = (p.y - b);
		return (pxa * pxa + pyb * pyb - r * r < 0);
	}

	/**
	 * ��p�����������׻�����
	 * 
	 * @param p
	 *            Ǥ�դ���
	 * @return ��p���������
	 */
	public Line2f[] tangentLine(Point2f p) {
		Line2f l[];
		// ����ξ�硢�����Ϥʤ�
		if (contains(p))
			return l = new Line2f[0];
		// �߼���ξ�硢������1��
		if (online(p)) {
			l = new Line2f[1];
			l[0] = new Line2f();
			l[0].A = p.x - a;
			l[0].B = p.y - b;
			l[0].C = -l[0].A * a - l[0].B * b - r * r;
			return l;
		}
		// �߳��ξ�硢������2��
		float A, B, C, D, E, F;
		A = p.x - a;
		B = p.y - b;
		C = -p.x * a - p.y * b + a * a + b * b - r * r;
		D = -A / B;
		E = -C / B;
		F = E - b;

		Point2f p1 = new Point2f();
		Point2f p2 = new Point2f();
		if (B == 0 || Math.abs(B) <= EPS) {
			p1.x = -C / A;
			p1.y = b + (float) Math.sqrt(r * r - (p1.x - a) * (p1.x - a));
			p2.x = -C / A;
			p2.y = b - (float) Math.sqrt(r * r - (p2.x - a) * (p2.x - a));
		} else {
			double d = (D * F - a) * (D * F - a) - (1 + D * D)
					* (F * F + a * a - r * r);
			if (d < 0)
				return l = new Line2f[0];
			p1.x = (a - D * F + (float) Math.sqrt(d)) / (1 + D * D);
			p1.y = D * p1.x + E;
			p2.x = (a - D * F - (float) Math.sqrt(d)) / (1 + D * D);
			p2.y = D * p2.x + E;
		}

		l = new Line2f[2];
		l[0] = new Line2f();
		l[1] = new Line2f();
		l[0].A = p1.x - a;
		l[0].B = p1.y - b;
		l[0].C = -l[0].A * a - l[0].B * b - r * r;
		l[1].A = p2.x - a;
		l[1].B = p2.y - b;
		l[1].C = -l[0].A * a - l[0].B * b - r * r;
		return l;
	}

	/**
	 * (�߼����)��p�����ˡ�����֤�
	 * 
	 * @param p
	 *            Ǥ�դ���
	 * @return ��p�����ˡ��
	 */
	public Line2f normalLine(Point2f p) {
		return new Line2f(new Point2f(a, b), p);
	}

	/**
	 * ľ��l�ȸ򺹤��뤫�ɤ������֤�
	 * 
	 * @param l
	 *            Ǥ�դ�ľ��
	 * @return �򺹤��뤫�ɤ���
	 */
	public boolean intersect(Line2f l) {
		return l.dist(getCenterPoint()) <= r;
	}

	/**
	 * ľ��l�Ȥθ�����׻�����
	 * 
	 * @param l
	 *            Ǥ�դ�ľ��
	 * @return ����
	 */
	public Point2f[] intersection(Line2f l) {
		Point2f p[];
		// �ߤȸ���ʤ�
		if (l.dist(getCenterPoint()) > r)
			return p = new Point2f[0];

		float D, E, F;
		D = -l.A / l.B;
		E = -l.C / l.B;
		F = E - b;

		if (l.B == 0 || Math.abs(l.B) <= EPS) {
			p = new Point2f[2];
			p[0] = new Point2f();
			p[1] = new Point2f();
			p[0].x = -l.C / l.A;
			p[0].y = b + (float) Math.sqrt(r * r - (p[0].x - a) * (p[0].x - a));
			p[1].x = -l.C / l.A;
			p[1].y = b - (float) Math.sqrt(r * r - (p[1].x - a) * (p[1].x - a));
			return p;
		} else {
			float d = (D * F - a) * (D * F - a) - (1 + D * D)
					* (F * F + a * a - r * r);
			if (Math.abs(d) < EPS) {
				p = new Point2f[1];
				p[0] = new Point2f();
				p[0].x = (a - D * F) / (1 + D * D);
				p[0].y = D * p[0].x + E;
				return p;
			}
			if (d < 0)
				return new Point2f[0];
			p = new Point2f[2];
			p[0] = new Point2f();
			p[1] = new Point2f();
			p[0].x = (a - D * F + (float) Math.sqrt(d)) / (1 + D * D);
			p[0].y = D * p[0].x + E;
			p[1].x = (a - D * F - (float) Math.sqrt(d)) / (1 + D * D);
			p[1].y = D * p[1].x + E;
			return p;
		}
	}

	/**
	 * ľ��l���ܤ��뤫
	 * 
	 * @param l
	 *            Ǥ�դ�ľ��
	 * @return ľ��l���ܤ��뤫
	 */
	public boolean tangentTo(Line2f l) {
		float tmp = (l.A * a + l.B * b + l.C);
		return Math.abs(tmp * tmp - r * r * (l.A * l.A + l.B * l.B)) <= EPS;
	}

	/**
	 * ʸ����ɽ�����֤�
	 * 
	 * @return ʸ����
	 */
	public String toString() {
		return "(x-" + a + ")^2 + (y-" + b + ")^2 = " + r + "^2";
	}

}
