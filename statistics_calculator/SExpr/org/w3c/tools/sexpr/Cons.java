/*
 *  Cons.java
 *
 *  Copyright 1997 Massachusetts Institute of Technology.
 *  All Rights Reserved.
 *
 *  Author: Ora Lassila
 *
 *  $Id: Cons.java,v 1.2 1998/01/22 13:08:38 bmahe Exp $
 */

package org.w3c.tools.sexpr;

import java.io.PrintStream;
import java.util.Enumeration;

/**
 * Basic class for implementing linked lists a la Lisp.
 */
public class Cons implements SExpr {

	private final Object car;
	private final Object cdr;

	/**
	 * Initializes a Cons cell with the left and right "subtrees".
	 */
	public Cons(final Object left, final Object right) {
		this.car = left;
		this.cdr = right;
	}

	/**
	 * Initializes a Cons cell with a left subtree only. Right subtree will be
	 * set to <tt>null</tt>.
	 */
	public Cons(final Object left) {
		this.car = left;
		this.cdr = null;
	}

	/**
	 * Returns the left subtree (i.e. the head) of a cons cell.
	 */
	public Object left() {
		return this.car;
	}

	/**
	 * Returns the right subtree (i.e. the tail) of a cons cell.
	 */
	public Object right() {
		return this.cdr;
	}

	/**
	 * Returns the tail of a cons cell if it is a list. Signals an error
	 * otherwise (no dotted pairs allowed).
	 * 
	 * @exception SExprParserException
	 *                if the tail is not a Cons or <tt>null<tt>
	 */
	public Cons rest() throws SExprParserException {
		final Object r = this.right();
		if (r == null) {
			return null;
		} else if (r instanceof Cons) {
			return (Cons) r;
		} else {
			throw new SExprParserException("No dotted pairs allowed");
		}
	}

	/*
	 * Returns an enumeration of the elements of the list.
	 */
	public Enumeration elements() {
		return new ConsEnumeration(this);
	}

	public void printExpr(final PrintStream stream) {
		this.printList(stream, true);
	}

	private void printList(final PrintStream out, final boolean first) {
		out.print(first ? "(" : " ");
		SimpleSExprStream.printExpr(this.left(), out);
		final Object r = this.right();
		if (r == null) {
			out.print(")");
		} else if (r instanceof Cons) {
			((Cons) r).printList(out, false);
		} else {
			out.print(". ");
			SimpleSExprStream.printExpr(r, out);
			out.print(")");
		}
	}

}

class ConsEnumeration implements Enumeration {

	private Cons current;

	public ConsEnumeration(final Cons head) {
		this.current = head;
	}

	public boolean hasMoreElements() {
		return this.current != null;
	}

	public Object nextElement() {
		Object element = null;
		try {
			element = this.current.left();
			this.current = this.current.rest();
		} catch (final SExprParserException e) {
			// current is a dotted pair
			this.current = null;
		}
		return element;
	}

}
