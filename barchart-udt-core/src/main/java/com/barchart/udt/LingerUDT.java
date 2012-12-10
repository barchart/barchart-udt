/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4';VERSION='1.0.2-SNAPSHOT';TIMESTAMP='2011-01-11_09-30-59';
 *
 * Copyright (C) 2009-2011, Barchart, Inc. (http://www.barchart.com/)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *     * Neither the name of the Barchart, Inc. nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Developers: Andrei Pozolotin;
 *
 * =================================================================================
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
