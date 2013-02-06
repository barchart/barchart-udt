/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

/**
 * keep code values in sync with
 * 
 * @see <a href="http://udt.sourceforge.net/udt4/doc/ecode.htm">UDT Error Codes
 *      List</a>
 */
public enum ErrorUDT {

	SUCCESS(0, "success operation"), //

	ECONNSETUP(1000, "connection setup failure"), //

	NOSERVER(1001, "server does not exist"), //

	ECONNREJ(1002, "connection request was rejected by server"), //

	ESOCKFAIL(1003, "could not create/configure UDP socket"), //

	ESECFAIL(1004, "connection request was aborted due to security reasons"), //

	ECONNFAIL(2000, "connection failure"), //

	ECONNLOST(2001, "connection was broken"), //

	ENOCONN(2002, "connection does not exist"), //

	ERESOURCE(3000, "system resource failure"), //

	ETHREAD(3001, "could not create new thread"), //

	ENOBUF(3002, "no memory space"), //

	EFILE(4000, "file access error"), //

	EINVRDOFF(4001, "invalid read offset"), //

	ERDPERM(4002, "no read permission"), //

	EINVWROFF(4003, "invalid write offset"), //

	EWRPERM(4004, "no write permission"), //

	EINVOP(5000, "operation not supported"), //

	EBOUNDSOCK(5001, "cannot execute the operation on a bound socket"), //

	ECONNSOCK(5002, "cannot execute the operation on a connected socket"), //

	EINVPARAM(5003, "bad parameters"), //

	EINVSOCK(5004, "invalid UDT socket"), //

	EUNBOUNDSOCK(5005, "cannot listen on unbound socket"), //

	ENOLISTEN(5006, "(accept) socket is not in listening state"), //

	ERDVNOSERV(5007,
			"rendezvous connection process does not allow listen and accept call"), //

	ERDVUNBOUND(
			5008,
			"rendezvous connection setup is enabled but bind has not been called before connect"), //

	ESTREAMILL(5009, "operation not supported in SOCK_STREAM mode"), //

	EDGRAMILL(5010, "operation not supported in SOCK_DGRAM mode"), //

	EDUPLISTEN(5011, "another socket is already listening on the same UDP port"), //

	ELARGEMSG(5012, "message is too large to be hold in the sending buffer"), //

	EINVPOLLID(5013, "epoll ID is invalid"), //

	EASYNCFAIL(6000, "non-blocking call failure"), //

	EASYNCSND(6001, "no buffer available for sending"), //

	EASYNCRCV(6002, "no data available for read"), //

	ETIMEOUT(6003, "timeout before operation completes"), //

	EPEERERR(7000, "error has happened at the peer side"), //

	// non UDT values:

	WRAPPER_UNKNOWN(-1, "unknown error code"), //
	WRAPPER_UNIMPLEMENTED(-2, "this feature is not yet implemented"), //
	WRAPPER_MESSAGE(-3, "wrapper generated error"), //
	USER_DEFINED_MESSAGE(-4, "user defined message"), //

	;

	private final int code;

	public int getCode() {
		return code;
	}

	private final String description;

	public String getDescription() {
		return description;
	}

	private ErrorUDT(final int code, final String description) {
		this.code = code;
		this.description = description;
	}

	static final ErrorUDT[] ENUM_VALS = values();

	public static ErrorUDT errorFrom(final int code) {
		for (final ErrorUDT known : ENUM_VALS) {
			if (known.code == code) {
				return known;
			}
		}
		return WRAPPER_UNKNOWN;
	}

	//

	public static String descriptionFrom(final int socketID,
			final int errorCode, final String errorComment) {
		final ErrorUDT error = ErrorUDT.errorFrom(errorCode);
		return String.format("UDT Error : %d : %s : %s [id: 0x%08x]", //
				errorCode, error.description, errorComment, socketID);
	}

}
