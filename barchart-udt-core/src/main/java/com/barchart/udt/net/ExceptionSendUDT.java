package com.barchart.udt.net;

import com.barchart.udt.ErrorUDT;
import com.barchart.udt.ExceptionUDT;

@SuppressWarnings("serial")
public class ExceptionSendUDT extends ExceptionUDT {

	protected ExceptionSendUDT(final int socketID, final ErrorUDT error,
			final String comment) {
		super(socketID, error, comment);
	}

}
