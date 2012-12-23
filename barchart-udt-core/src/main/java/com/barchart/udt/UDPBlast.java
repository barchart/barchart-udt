/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

/**
 * 
 * @author CCob
 * 
 *         Wrapper around the CUDPBlast class that demos the use of a custom
 *         congestion control algorithm
 */
public class UDPBlast extends CCC {

	static final int iSMSS = 1500;

	public UDPBlast() {
		setCWndSize(83333.0);
	}

	void setRate(final int mbps) {
		setPacketSndPeriod((iSMSS * 8.0) / mbps);
	}

}
