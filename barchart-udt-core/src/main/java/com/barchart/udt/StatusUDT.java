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

/** status of underlying UDT native socket */
public enum StatusUDT {

	/* non UDT value */
	UNKNOWN(0), //

	//

	/* keep in sync with udt.h UDTSTATUS enum */

	/** newly created socket; both connector and acceptor */
	INIT(1), //

	/** just bound socket; both connector and acceptor */
	OPENED(2), //

	/** bound + listening acceptor socket */
	LISTENING(3), //

	/** connected connector socket */
	CONNECTED(4), //

	/** acceptor socket after close() */
	BROKEN(5), //

	/** connector socket after close() */
	CLOSED(6), //

	/** trying to check status on socket that was closed and removed */
	NONEXIST(7), //

	;

	private final int code;

	private StatusUDT(final int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	private static final StatusUDT[] ENUM_VALS = values();

	public static final StatusUDT fromCode(final int code) {
		for (final StatusUDT status : ENUM_VALS) {
			if (status.code == code) {
				return status;
			}
		}
		return UNKNOWN;
	}

	/**
	 * map UDT socket status to emulate JDK expected behavior
	 */
	public boolean isOpenEmulateJDK() {
		switch (this) {
		case INIT:
		case OPENED:
		case LISTENING:
		case CONNECTED:
			return true;
		default:
			return false;
		}
	}

}
