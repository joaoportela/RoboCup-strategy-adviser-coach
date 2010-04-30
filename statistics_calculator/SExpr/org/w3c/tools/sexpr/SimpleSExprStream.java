/*
 *  SimpleSExprStream.java
 *
 *  Copyright 1997 Massachusetts Institute of Technology.
 *  All Rights Reserved.
 *
 *  Author: Ora Lassila
 *
 *  $Id: SimpleSExprStream.java,v 1.2 1998/01/22 13:09:28 bmahe Exp $
 */

package org.w3c.tools.sexpr;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PushbackInputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Basic implementation of the SExprStream parser interface.
 */
public class SimpleSExprStream extends PushbackInputStream implements
SExprStream {

	private final StringBuffer buffer;
	private Dictionary symbols;
	private boolean noSymbols;
	private Readtable readtable;
	private boolean listsAsVectors;

	/**
	 * Initializes the parser with no read table and no symbol table assigned.
	 * Parsed lists will be represented as Cons cells.
	 */
	public SimpleSExprStream(final InputStream input) {
		super(input);
		this.buffer = new StringBuffer();
		this.symbols = null;
		this.noSymbols = false;
		this.readtable = null;
		this.listsAsVectors = false;
	}

	/**
	 * Accesses the symbol table of the parser. If no symbol table has been
	 * assigned, creates an empty table.
	 */
	public Dictionary getSymbols() {
		if (!this.noSymbols && this.symbols == null) {
			this.symbols = new Hashtable();
		}
		return this.symbols;
	}

	/**
	 * Assigns a symbol table to the parser. Assigning <tt>null</tt> will
	 * prevent an empty symbol table to be created in the future.
	 */
	public Dictionary setSymbols(final Dictionary symbols) {
		if (symbols == null) {
			this.noSymbols = true;
		}
		return this.symbols = symbols;
	}

	/**
	 * Accesses the read table of the parser. If no read table has been
	 * assigned, creates an empty table.
	 */
	public Readtable getReadtable() {
		if (this.readtable == null) {
			this.readtable = new SimpleReadtable();
		}
		return this.readtable;
	}

	/**
	 * Assigns a new read table to the parser.
	 */
	public Readtable setReadtable(final Readtable readtable) {
		return this.readtable = readtable;
	}

	/**
	 * Checks whether lists should be parsed as Vectors or Cons cells.
	 */
	public boolean getListsAsVectors() {
		return this.listsAsVectors;
	}

	/**
	 * Controls whether lists are represented as Vectors or Cons cells.
	 */
	public boolean setListsAsVectors(final boolean listsAsVectors) {
		return this.listsAsVectors = listsAsVectors;
	}

	/**
	 * Accesses an empty string buffer available temporary storage. This buffer
	 * can be used by sub-parsers as a scratch area. Please note that the buffer
	 * is not guarded in any way, so multithreaded and reentrant programs must
	 * worry about this themselves.
	 */
	public StringBuffer getScratchBuffer() {
		this.buffer.setLength(0);
		return this.buffer;
	}

	/**
	 * Parses a single object from the underlying input stream.
	 * 
	 * @exception SExprParserException
	 *                if syntax error was detected
	 * @exception IOException
	 *                if any other I/O-related problem occurred
	 */
	public Object parse() throws SExprParserException, IOException {
		return this.parse(this.readSkipWhite(), this);
	}

	/**
	 * Parses a single object started by the character <i>c</i>. Implements the
	 * SExprParser interface.
	 * 
	 * @exception SExprParserException
	 *                if syntax error was detected
	 * @exception IOException
	 *                if any other I/O-related problem occurred
	 */
	public Object parse(final char c, final SExprStream stream)
	throws SExprParserException, IOException {
		final SExprParser parser = this.getReadtable().getParser(c);
		if (parser != null) {
			return parser.parse(c, this);
		} else if (c == '(') {
			if (this.getListsAsVectors()) {
				return this.parseVector(new Vector(), ')');
			} else {
				return this.parseList();
			}
		} else if (c == '"') {
			return this.parseString();
		} else if (this.isAtomChar(c, true)) {
			return this.parseAtom(c);
		} else {
			throw new SExprParserException(c);
		}
	}

	/**
	 * Parses a list (as Cons cells) sans first character.
	 * 
	 * @exception SExprParserException
	 *                if syntax error was detected
	 * @exception IOException
	 *                if any other I/O-related problem occurred
	 */
	protected Cons parseList() throws SExprParserException, IOException {
		final char c = this.readSkipWhite();
		if (c == ')') {
			return null;
		} else {
			this.unread(c);
			return new Cons(this.parse(), this.parseList());
		}
	}

	/**
	 * Parses a list (as a Vector) sans first character. In order to parse
	 * list-like structures delimited by other characters than parentheses, the
	 * delimiting (ending) character has to be provided.
	 * 
	 * @exception SExprParserException
	 *                if syntax error was detected
	 * @exception IOException
	 *                if any other I/O-related problem occurred
	 */
	protected Vector parseVector(final Vector vector, final char delimiter)
	throws SExprParserException, IOException {
		final char c = this.readSkipWhite();
		if (c == delimiter) {
			return vector;
		} else {
			this.unread(c);
			vector.addElement(this.parse());
			return this.parseVector(vector, delimiter);
		}
	}

	/**
	 * Parses an atom (a number or a symbol). Since anything that is not a
	 * number is a symbol, syntax errors are not possible.
	 * 
	 * @exception SExprParserException
	 *                not signalled but useful for the protocol
	 * @exception IOException
	 *                if an I/O problem occurred (e.g. end of file)
	 */
	protected Object parseAtom(char c) throws SExprParserException, IOException {
		final StringBuffer b = this.getScratchBuffer();
		do {
			b.append(c);
		} while (this.isAtomChar(c = (char) this.read(), false));
		this.unread(c);
		final String s = b.toString();
		try {
			return this.makeNumber(s);
		} catch (final NumberFormatException e) {
			return Symbol.makeSymbol(s, this.getSymbols());
		}
	}

	/**
	 * Parses a double-quote -delimited string (sans the first character).
	 * Please note: no escape-character interpretation is performed. Override
	 * this method for any escape character handling.
	 * 
	 * @exception SExprParserException
	 *                not signalled but useful for the protocol
	 * @exception IOException
	 *                any I/O problem (including end of file)
	 */
	public String parseString() throws SExprParserException, IOException {
		int code;
		final StringBuffer b = this.getScratchBuffer();
		while (true) {
			switch (code = this.read()) {
			case '"':
				return new String(b);
			case -1:
				throw new EOFException();
			default:
				b.append((char) code);
				break;
			}
		}
	}

	/**
	 * Predicate function for checking if a chahracter can belong to an atom.
	 * 
	 * @param first
	 *            if true means that c is the first character of the atom
	 */
	protected boolean isAtomChar(final char c, final boolean first) {
		return !(Character.isSpace(c) || c == '(' || c == ')' || c == '"'
			|| c == '}' || c == '{');
	}

	/**
	 * Reads from the stream, skipping whitespace and comments.
	 * 
	 * @exception IOException
	 *                if an I/O problem occurred (including end of file)
	 */
	public char readSkipWhite() throws IOException {
		char c;
		do {
			c = (char) this.read();
			if (c == ';') {
				do {
				} while ((c = (char) this.read()) != '\n' && c != '\r');
			}
			if (c == -1) {
				throw new EOFException();
			}
		} while (Character.isSpace(c));
		return c;
	}

	/**
	 * Attempts to parse a number from the string.
	 * 
	 * @exception NumberFormatException
	 *                the string does not represent a number
	 */
	protected Number makeNumber(final String s) throws NumberFormatException {
		try {
			return Integer.valueOf(s);
		} catch (final NumberFormatException e) {
			return DoubleFix.valueOf(s);
		}
	}

	/**
	 * Associates a dispatch character with a parser in the read table.
	 */
	public SExprParser addParser(final char key, final SExprParser parser) {
		return this.getReadtable().addParser(key, parser);
	}

	/**
	 * Produces a printed representation of an s-expression.
	 */
	public static void printExpr(final Object expr, final PrintStream out) {
		if (expr == null) {
			out.print("nil");
		} else if (expr instanceof Number) {
			out.print(expr);
		} else if (expr instanceof String) {
			out.print('"');
			out.print(expr);
			out.print('"');
		} else if (expr instanceof Vector) {
			out.print("(");
			for (int i = 0; i < ((Vector) expr).size(); i++) {
				if (i != 0) {
					out.print(" ");
				}
				SimpleSExprStream.printExpr(((Vector) expr).elementAt(i), out);
			}
			out.print(")");
		} else if (expr instanceof SExpr) {
			((SExpr) expr).printExpr(out);
		} else {
			out.print("#<unknown " + expr + ">");
		}
	}

	public static void main(final String args[]) throws SExprParserException,
	IOException {
		final InputStream expr = new ByteArrayInputStream(SEE_EXPR
				.getBytes("UTF-8"));
		final SExprStream p = new SimpleSExprStream(expr);
		final Object e = p.parse();
		SimpleSExprStream.printExpr(e, System.out);
		System.out.println("\n--------");
		if (e instanceof Cons && ((Cons) e).left().equals(new Symbol("see_global"))) {
			assert ((Cons) e).right() instanceof Cons;
			final Cons cons = (Cons) ((Cons) e).right();
			assert cons.left() instanceof Integer;
			assert cons.right() instanceof Cons;

			// iterate over the visible game objects
			final ConsEnumeration enumeration = new ConsEnumeration(cons);
			Object item = null;
			while (enumeration.hasMoreElements()) {
				item = enumeration.nextElement();
				SimpleSExprStream.printExpr(item, System.out);
				System.out.println("");
			}
		}
		System.out.println();
	}

	private static final String SEE_EXPR = "(see_global 0 ((g r) 52.5 0) ((g l) -52.5 0) ((b) 0 0 -0 -0) ((p \"FCPortugalD\" 1 goalie) -50 0 0 0 1 -1) ((p \"FCPortugalD\" 2) -14.86 16.23 0 0 1 -49) ((p \"FCPortugalD\" 3) -14.86 4.55 0 0 1 -18) ((p \"FCPortugalD\" 4) -14.86 -4.55 0 0 0 17) ((p \"FCPortugalD\" 5) -14.86 -16.23 0 0 0 47) ((p \"FCPortugalD\" 6) -9.11 -1.3 0 0 0 8) ((p \"FCPortugalD\" 7) -4.41 12.55 0 0 1 -72) ((p \"FCPortugalD\" 8) -4.41 -12.55 0 0 0 70) ((p \"FCPortugalD\" 9) -1.5 -0.27 0 0 -1 -19) ((p \"FCPortugalD\" 10) -1.5 26.55 0 0 0 -86) ((p \"FCPortugalD\" 11) -1.5 -26.55 0 0 -1 88) ((p \"FCPortugalX\" 1 goalie) 50 -0 0 0 179 0) ((p \"FCPortugalX\" 2) 14.86 -16.23 0 0 180 -47) ((p \"FCPortugalX\" 3) 14.86 -4.55 0 0 180 -17) ((p \"FCPortugalX\" 4) 14.86 4.55 0 0 179 17) ((p \"FCPortugalX\" 5) 14.86 16.23 0 0 180 48) ((p \"FCPortugalX\" 6) 9.39097 1.11341 0 0 179 8) ((p \"FCPortugalX\" 7) 4.41 -12.55 0 0 -179 -71) ((p \"FCPortugalX\" 8) 4.41 12.55 0 0 180 70) ((p \"FCPortugalX\" 9) 9.30053 1.6741 0 0 124 66) ((p \"FCPortugalX\" 10) 1.5 -26.55 0 0 180 -50) ((p \"FCPortugalX\" 11) 33 -37 0 0 0 0))";

}
