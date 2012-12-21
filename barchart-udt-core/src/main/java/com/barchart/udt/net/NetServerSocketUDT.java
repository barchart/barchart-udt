/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;

import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class NetServerSocketUDT extends ServerSocket implements IceServerSocket {

	protected final SocketUDT serverSocketUDT;

	public NetServerSocketUDT() throws IOException {
		this.serverSocketUDT = new SocketUDT(TypeUDT.STREAM);
		this.serverSocketUDT.configureBlocking(true);
	}

	// exception thanks to JDK designers
	/** NOTE: you just carefully choose TypeUDT */
	public NetServerSocketUDT(final SocketUDT socketUDT) throws IOException {
		this.serverSocketUDT = socketUDT;
	}

	//

	@Override
	public Socket accept() throws IOException {
		final SocketUDT connector = serverSocketUDT.accept();
		return new NetSocketUDT(connector);
	}

	@Override
	public void bind(final SocketAddress endpoint) throws IOException {
		final int backlog = SocketUDT.DEFAULT_ACCEPT_QUEUE_SIZE;
		bind(endpoint, backlog);
	}

	/**
	 * NOTE: bind() means listen() for UDT server socket
	 */
	@Override
	public void bind(SocketAddress bindpoint, int backlog) throws IOException {
		if (bindpoint == null) {
			bindpoint = new InetSocketAddress(0);
		}
		if (backlog <= 0) {
			backlog = SocketUDT.DEFAULT_ACCEPT_QUEUE_SIZE;
		}
		serverSocketUDT.bind((InetSocketAddress) bindpoint);
		serverSocketUDT.listen(backlog);
	}

	@Override
	public void close() throws IOException {
		serverSocketUDT.close();
	}

	@Override
	public ServerSocketChannel getChannel() {
		throw new RuntimeException("feature not available");
	}

	@Override
	public InetAddress getInetAddress() {
		return serverSocketUDT.getLocalInetAddress();
	}

	@Override
	public int getLocalPort() {
		return serverSocketUDT.getLocalInetPort();
	}

	@Override
	public SocketAddress getLocalSocketAddress() {
		try {
			return serverSocketUDT.getLocalSocketAddress();
		} catch (final ExceptionUDT e) {
			return null;
		}
	}

	@Override
	public int getReceiveBufferSize() throws SocketException {
		return serverSocketUDT.getReceiveBufferSize();
	}

	@Override
	public boolean getReuseAddress() throws SocketException {
		return serverSocketUDT.getReuseAddress();
	}

	@Override
	public int getSoTimeout() throws IOException {
		return serverSocketUDT.getSoTimeout();
	}

	@Override
	public boolean isBound() {
		return serverSocketUDT.isBound();
	}

	@Override
	public boolean isClosed() {
		return serverSocketUDT.isClosed();
	}

	@Override
	public void setPerformancePreferences(final int connectionTime,
			final int latency, final int bandwidth) {
		throw new RuntimeException("feature not available");
	}

	// NOTE: set both send and receive, since they are inherited on accept()
	@Override
	public void setReceiveBufferSize(final int size) throws SocketException {
		serverSocketUDT.setReceiveBufferSize(size);
		serverSocketUDT.setSendBufferSize(size);
	}

	@Override
	public void setReuseAddress(final boolean on) throws SocketException {
		serverSocketUDT.setReuseAddress(on);
	}

	@Override
	public void setSoTimeout(final int timeout) throws SocketException {
		serverSocketUDT.setSoTimeout(timeout);
	}

}
