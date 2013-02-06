/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import java.net.SocketException;

import com.barchart.udt.anno.Native;

/**
 * The Class ExceptionUDT. Wraps all native UDT exceptions and more.
 */
@SuppressWarnings("serial")
public class ExceptionUDT extends SocketException {

	/**
	 * The error udt. Keeps error description for this exception. Use this enum
	 * in switch/case to fine tune exception processing.
	 */
	@Native
	private final ErrorUDT errorUDT;

	public ErrorUDT getError() {
		return errorUDT;
	}

	/**
	 * The socket id. Keeps socketID of the socket that produced this exception.
	 * Can possibly contain '0' when particular method can not determine
	 * {@link #socketID} that produced the exception.
	 */
	@Native
	private final int socketID;

	public int getSocketID() {
		return socketID;
	}

	/**
	 * Instantiates a new exception udt for native UDT::Exception. This
	 * exception is generated in the underlying UDT method.
	 * 
	 * @param socketID
	 *            the socket id
	 * @param errorCode
	 *            the error code
	 * @param comment
	 *            the comment
	 */
	@Native
	protected ExceptionUDT(final int socketID, final int errorCode,
			final String comment) {
		super(ErrorUDT.descriptionFrom(socketID, errorCode, comment));
		errorUDT = ErrorUDT.errorFrom(errorCode);
		this.socketID = socketID;
	}

	/**
	 * Instantiates a new exception udt for synthetic JNI wrapper exception.
	 * This exception is generated in the JNI glue code itself.
	 * 
	 * @param socketID
	 *            the socket id
	 * @param error
	 *            the error
	 * @param comment
	 *            the comment
	 */
	@Native
	protected ExceptionUDT(final int socketID, final ErrorUDT error,
			final String comment) {
		super(ErrorUDT.descriptionFrom(socketID, error.getCode(), comment));
		errorUDT = error;
		this.socketID = socketID;
	}

}
