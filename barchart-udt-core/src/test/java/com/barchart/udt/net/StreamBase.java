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

		socket.configureBlocking(true);

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
