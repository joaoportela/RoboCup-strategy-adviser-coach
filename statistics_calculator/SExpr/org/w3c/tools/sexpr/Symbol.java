/*
 *  Symbol.java
 *
 *  Copyright 1997 Massachusetts Institute of Technology.
 *  All Rights Reserved.
 *
 *  Author: Ora Lassila
 *
 *  $Id: Symbol.java,v 1.2 1998/01/22 13:09:34 bmahe Exp $
 */

package org.w3c.tools.sexpr;

import java.io.PrintStream;
import java.util.Dictionary;

/**
 * Base class for lisp-like symbols.
 */
public class Symbol implements SExpr {

	private final String name;

	/**
	 * Creates a symbol and potentially interns it in a symbol table.
	 */
	public static Symbol makeSymbol(final String name, final Dictionary symbols) {
		if (symbols == null) {
			return new Symbol(name);
		} else {
			final String key = name.toLowerCase();
			Symbol s = (Symbol) symbols.get(key);
			if (s == null) {
				s = new Symbol(name);
				symbols.put(key, s);
			}
			return s;
		}
	}

	protected Symbol(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public boolean equals(final Object o) {
		if(o instanceof Symbol) {
			final Symbol sym = (Symbol) o;
			return this.name.equals(sym.name);
		}
		return false;
	}

	public void printExpr(final PrintStream out) {
		out.print(this.toString());
	}

}
