/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

/**
 * status of underlying UDT native socket
 */
public enum StatusUDT {

	/** non UDT value */
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
