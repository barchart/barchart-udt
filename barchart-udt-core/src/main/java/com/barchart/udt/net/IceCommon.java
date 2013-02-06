/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.net;

import com.barchart.udt.SocketUDT;

/**
 * custom/common acceptor/connector socket features
 */
public interface IceCommon {

	/**
	 * expose underlying socket
	 */
	SocketUDT socketUDT();

}
