/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.net;

import com.barchart.udt.ErrorUDT;
import com.barchart.udt.ExceptionUDT;

/**
 * 
 */
@SuppressWarnings("serial")
public class ExceptionReceiveUDT extends ExceptionUDT {

	protected ExceptionReceiveUDT(final int socketID, final ErrorUDT error,
			final String comment) {
		super(socketID, error, comment);
	}

}
