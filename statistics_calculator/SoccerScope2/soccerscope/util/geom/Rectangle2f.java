/*
 * $Log: Rectangle2f.java,v $
 * Revision 1.2  2002/10/07 07:35:56  koji
 * Circle2f,Line2f,Rectangle2f�ǥե���ȥ��󥹥ȥ饯���ɲ�
 *
 * Revision 1.1  2002/10/04 10:39:17  koji
 * Tuple,Point,Vector�Υѥå������ѹ���Geometry,Circle,Line,Rectangle�ɲ�
 *
 *
 */

package soccerscope.util.geom;

public class Rectangle2f {

	public Point2f topleft;
	/** < ����κ����ɸ */
	public float width;
	/** < �� */
	public float height;

	/** < �⤵ */

	/**
	 * ���󥹥ȥ饯��
	 */
	public Rectangle2f() {
		topleft = new Point2f();
		this.width = 0;
		this.height = 0;
	}

	/**
	 * ���󥹥ȥ饯��
	 * 
	 * @param p
	 *            ����κ����ɸ
	 * @param width
	 *            ��
	 * @param height
	 *            �⤵
	 */
	public Rectangle2f(Point2f p, float width, float height) {
		topleft = new Point2f(p);
		this.width = width;
		this.height = height;
	}

	/**
	 * ���󥹥ȥ饯��
	 * 
	 * @param topleftx
	 *            ����κ���x��ɸ
	 * @param toplefty
	 *            ����κ���y��ɸ
	 * @param width
	 *            ��
	 * @param height
	 *            �⤵
	 */
	public Rectangle2f(float topleftx, float toplefty, float width, float height) {
		topleft = new Point2f(topleftx, toplefty);
		this.width = width;
		this.height = height;
	}

	/**
	 * ���ԡ����󥹥ȥ饯��
	 * 
	 * @param r
	 *            ���
	 */
	public Rectangle2f(Rectangle2f r) {
		topleft = new Point2f(r.topleft);
		width = r.width;
		height = r.height;
	}

	/**
	 * ��������ꤹ��
	 * 
	 * @param width
	 *            ��
	 * @param height
	 *            �⤵
	 */
	public void setWidth(float width, float height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * ��������ꤹ��
	 * 
	 * @param p
	 *            ����κ����ɸ
	 * @param width
	 *            ��
	 * @param height
	 *            �⤵
	 */
	public void set(Point2f p, float width, float height) {
		topleft.set(p);
		this.width = width;
		this.height = height;
	}

	/**
	 * ��������ꤹ��
	 * 
	 * @param topleftx
	 *            ����κ���x��ɸ
	 * @param toplefty
	 *            ����κ���y��ɸ
	 * @param width
	 *            ��
	 * @param height
	 *            �⤵
	 */
	public void set(float topleftx, float toplefty, float width, float height) {
		topleft.x = topleftx;
		topleft.y = toplefty;
		this.width = width;
		this.height = height;
	}

	/**
	 * ���r�����ꤹ��
	 * 
	 * @param r
	 *            ���
	 */
	public void set(Rectangle2f r) {
		topleft.set(r.topleft);
		width = r.width;
		height = r.height;
	}

	/**
	 * ��(x,y)��ޤफ�ɤ�����Ƚ�ꤹ�롣�ޤ��硢true���֤�
	 * 
	 * @param x
	 *            ����x��ɸ
	 * @param y
	 *            ����y��ɸ
	 * @return ��(x,y)��ޤफ�ɤ���
	 */
	public boolean contains(float x, float y) {
		return (topleft.x <= x && x <= topleft.x + width)
				&& (topleft.y <= y && y <= topleft.y + height);
	}

	/**
	 * ��p��ޤफ�ɤ�����Ƚ�ꤹ�롣�ޤ��硢true���֤�
	 * 
	 * @param p
	 *            Ǥ�դ���
	 * @return ��(x,y)��ޤफ�ɤ���
	 */
	public boolean contains(Point2f p) {
		return contains(p.x, p.y);
	}

	/**
	 * ���r��ޤफ�ɤ�����Ƚ�ꤹ�롣�ޤ��硢true���֤�
	 * 
	 * @param r
	 *            ���
	 * @return ���r��ޤफ�ɤ���
	 */
	public boolean contains(Rectangle2f r) {
		return contains(r.topleft.x, r.topleft.y)
				&& contains(r.topleft.x, r.topleft.y + height)
				&& contains(r.topleft.x + width, r.topleft.y)
				&& contains(r.topleft.x + width, r.topleft.y + height);
	}

	/*
	 * public Point2f topleft() { return Point2f(topleft); }
	 */

	/**
	 * ����������֤�
	 * 
	 * @return �������
	 */
	public Point2f topright() {
		return new Point2f(topleft.x + width, topleft.y);
	}

	/**
	 * �����������֤�
	 * 
	 * @return ��������
	 */
	public Point2f bottomleft() {
		return new Point2f(topleft.x, topleft.y + height);
	}

	/**
	 * �����������֤�
	 * 
	 * @return ��������
	 */
	public Point2f bottomright() {
		return new Point2f(topleft.x + width, topleft.y + height);
	}

	/**
	 * ʸ����ɽ�����֤�
	 * 
	 * @return ʸ����
	 */
	public String toString() {
		return topleft + " " + width + " " + height;
	}
}
