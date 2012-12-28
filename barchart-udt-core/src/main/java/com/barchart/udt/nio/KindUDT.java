/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import com.barchart.udt.SocketUDT;

/**
 * UDT channel role type.
 * 
 * NOTE: {@link com.barchart.udt#TypeUDT} means stream vs datagram;
 * {@link KindUDT} means server vs client.
 */
public enum KindUDT {

	/**
	 * Server mode: listens and accepts connections; generates
	 * {@link #CONNECTOR} as a result of {@link SocketUDT#accept()}
	 */
	ACCEPTOR, //

	/**
	 * Client mode: user-created channel which initiates connections to servers;
	 * options are user-provided
	 * <p>
	 * Server mode: channel which is a result of accept(); inherits options from
	 * parent {@link #ACCEPTOR}
	 */
	CONNECTOR, //

	/**
	 * TODO
	 */
	RENDEZVOUS, //

}
