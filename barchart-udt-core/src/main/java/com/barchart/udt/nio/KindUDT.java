/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

/**
 * UDT channel role type, or kind.
 * <p>
 * {@link TypeUDT} means stream (byte oriented) vs datagram (message oriented).
 * <p>
 * {@link KindUDT} gives distinction between server vs client vs peer.
 */
public enum KindUDT {

	/**
	 * Server mode: listens and accepts connections; generates
	 * {@link #CONNECTOR} as a result of {@link SocketUDT#accept()}
	 * 
	 * @see ServerSocketChannelUDT
	 */
	ACCEPTOR, //

	/**
	 * Client mode: channel which initiates connections to servers; options are
	 * user-provided.
	 * <p>
	 * Server mode: channel which is a result of accept(); inherits options from
	 * parent {@link #ACCEPTOR}.
	 * 
	 * @see SocketChannelUDT
	 */
	CONNECTOR, //

	/**
	 * Rendezvous mode: symmetric peer channel on each side of the connection.
	 * 
	 * @see RendezvousChannelUDT
	 */
	RENDEZVOUS, //

}
