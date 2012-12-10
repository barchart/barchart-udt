package com.barchart.udt.net;

import com.barchart.udt.ErrorUDT;
import com.barchart.udt.ExceptionUDT;

@SuppressWarnings("serial")
public class ExceptionReceiveUDT extends ExceptionUDT {

	protected ExceptionReceiveUDT(final int socketID, final ErrorUDT error,
			final String comment) {
		super(socketID, error, comment);
	}

}
