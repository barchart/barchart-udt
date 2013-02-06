/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

public class LingerUDT extends Number implements Comparable<LingerUDT> {

	public static final LingerUDT LINGER_ZERO = new LingerUDT(0);

	// measured in seconds
	final int timeout;

	/**
	 * Default constructor. NOTE: linger value is "u_short" on windows and "int"
	 * on linux:<br>
	 * Windows: <a
	 * href="http://msdn.microsoft.com/en-us/library/ms739165(VS.85).aspx">
	 * linger Structure on Windows</a><br>
	 * Linux: <a href=
	 * "http://www.gnu.org/s/libc/manual/html_node/Socket_002dLevel-Options.html"
	 * >GCC Socket-Level Options</a><br>
	 * Therefore select smallest range: 0 <= linger <= 65535 <br>
	 * 
	 * @param lingerSeconds
	 *            the seconds to linger; "0" means "do not linger"
	 * 
	 * @throws IllegalArgumentException
	 *             when lingerSeconds is out of range
	 */
	public LingerUDT(int lingerSeconds) throws IllegalArgumentException {
		if (65535 < lingerSeconds) {
			throw new IllegalArgumentException(
					"lingerSeconds is out of range: 0 <= linger <= 65535");
		}
		this.timeout = lingerSeconds > 0 ? lingerSeconds : 0;
	}

	private static final long serialVersionUID = 3414455799823407217L;

	@Override
	public double doubleValue() {
		return timeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#floatValue()
	 */
	@Override
	public float floatValue() {
		return timeout;
	}

	@Override
	public int intValue() {
		return timeout;
	}

	@Override
	public long longValue() {
		return timeout;
	}

	boolean isLingerOn() {
		return timeout > 0;
	}

	int timeout() {
		return timeout;
	}

	@Override
	public boolean equals(Object otherLinger) {
		if (otherLinger instanceof LingerUDT) {
			LingerUDT other = (LingerUDT) otherLinger;
			return other.timeout == this.timeout;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return timeout;
	}

	@Override
	public int compareTo(LingerUDT other) {
		return other.timeout - this.timeout;
	}

	@Override
	public String toString() {
		return String.valueOf(timeout);
	}

}
