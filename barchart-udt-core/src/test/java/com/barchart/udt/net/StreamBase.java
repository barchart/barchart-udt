/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.net;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.barchart.udt.SocketUDT;

abstract class StreamBase implements Runnable {

	final InetSocketAddress localAddress;
	final InetSocketAddress remoteAddress;

	final SocketUDT socket;

	final InputStream streamIn;
	final OutputStream streamOut;

	StreamBase(final SocketUDT socket, final InetSocketAddress localAddress,
			final InetSocketAddress remoteAddress) throws Exception {

		this.localAddress = localAddress;
		this.remoteAddress = remoteAddress;

		this.socket = socket;
		assert socket.isOpen();
		assert socket.isBlocking();

		socket.setBlocking(true);

		streamIn = new NetInputStreamUDT(socket);
		streamOut = new NetOutputStreamUDT(socket);

	}

	// StreamBase(final InetSocketAddress localAddress,
	// final InetSocketAddress remoteAddress) throws Exception {
	//
	// this(new SocketUDT(TypeUDT.DATAGRAM), localAddress, remoteAddress);
	//
	// }

}
