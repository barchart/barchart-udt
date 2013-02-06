/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * status of underlying UDT native socket as reported by
 * {@link SocketUDT#getStatus0()}
 * <p>
 * keep in sync with udt.h UDTSTATUS enum; see:
 * <p>
 * "enum UDTSTATUS { INIT = 1, OPENED = 2, LISTENING = 3, CONNECTING = 4, CONNECTED = 5, BROKEN = 6, CLOSING = 7, CLOSED = 8, NONEXIST = 9 };"
 */
public enum StatusUDT {

	/** note: keep the order */

	//

	/** newly created socket; both connector and acceptor */
	INIT(1), //

	/** bound socket; both connector and acceptor */
	OPENED(2), //

	/** bound and listening acceptor socket */
	LISTENING(3), //

	/** bound connector socket trying to connect */
	CONNECTING(4), //

	/** bound and connected connector socket */
	CONNECTED(5), //

	/** acceptor socket after close(), connector socket after remote unreachable */
	BROKEN(6), //

	/** connector socket while close() is in progress */
	CLOSING(7), //

	/** connector socket after close() is done */
	CLOSED(8), //

	/** trying to check status on socket that was closed and removed */
	NONEXIST(9), //

	/** non udt constant, catch-all value */
	UNKNOWN(100), //

	;

	protected static final Logger log = LoggerFactory
			.getLogger(StatusUDT.class);

	private final int code;

	private StatusUDT(final int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static final StatusUDT from(final int code) {

		switch (code) {
		case 1:
			return INIT;
		case 2:
			return OPENED;
		case 3:
			return LISTENING;
		case 4:
			return CONNECTING;
		case 5:
			return CONNECTED;
		case 6:
			return BROKEN;
		case 7:
			return CLOSING;
		case 8:
			return CLOSED;
		case 9:
			return NONEXIST;
		default:
			log.error("unexpected code={}", code);
			return UNKNOWN;
		}

	}

}
