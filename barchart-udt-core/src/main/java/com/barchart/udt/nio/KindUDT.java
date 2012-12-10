/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

/**
 * UDT channel role type.
 * 
 * NOTE: {@link #TypeUDT} means stream vs datagram;
 * {@link com.barchart.udt.nio.Kind} means server vs client.
 * <p>
 */
public enum KindUDT {

	/**
	 * The ACCEPTOR. Server mode: listens and accepts connections; generates
	 * CONNECTOR as a result of accept()
	 */
	ACCEPTOR, //

	/**
	 * The CONNECTOR. Client mode: user-created channel which initiates
	 * connections to servers; Server mode: channel which is a result of
	 * accept();
	 */
	CONNECTOR, //

}
