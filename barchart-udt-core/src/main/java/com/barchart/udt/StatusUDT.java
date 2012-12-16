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
 * <p>
 * keep in sync with udt.h UDTSTATUS enum; see:
 * <p>
 * "enum UDTSTATUS { INIT = 1, OPENED = 2, LISTENING = 3, CONNECTING = 4, CONNECTED = 5, BROKEN = 6, CLOSING = 7, CLOSED = 8, NONEXIST = 9 };"
 */
public enum StatusUDT {

	/** non UDT value */
	UNKNOWN(0), //

	//

	/** newly created socket; both connector and acceptor */
	INIT(1), //

	/** just bound socket; both connector and acceptor */
	OPENED(2), //

	/** bound + listening acceptor socket */
	LISTENING(3), //

	/** connector socket trytin to connect */
	CONNECTING(4), //

	/** connected connector socket */
	CONNECTED(5), //

	/** acceptor socket after close() */
	BROKEN(6), //

	/** connector socket after close() is in progress */
	CLOSING(7), //

	/** connector socket after close() is done */
	CLOSED(8), //

	/** trying to check status on socket that was closed and removed */
	NONEXIST(9), //

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
