/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
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

/**
 * {@link ServerSocket} - like wrapper for {@link SocketUDT}
 */
public class NetServerSocketUDT extends ServerSocket implements
		IceServerSocket, IceCommon {

	protected final SocketUDT socketUDT;

	/** uses {@link TypeUDT#STREAM} socket in blocking mode */
	public NetServerSocketUDT() throws IOException {
		this(new SocketUDT(TypeUDT.STREAM));
		this.socketUDT.setBlocking(true);
	}

	/** uses provided socket keeping blocking mode */
	protected NetServerSocketUDT(final SocketUDT socketUDT) throws IOException {
		this.socketUDT = socketUDT;
	}

	@Override
	public Socket accept() throws IOException {
		final SocketUDT clientUDT = socketUDT.accept();
		return new NetSocketUDT(clientUDT);
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
		socketUDT.bind((InetSocketAddress) bindpoint);
		socketUDT.listen(backlog);
	}

	@Override
	public void close() throws IOException {
		socketUDT.close();
	}

	@Override
	public ServerSocketChannel getChannel() {
		throw new UnsupportedOperationException("feature not available");
	}

	@Override
	public InetAddress getInetAddress() {
		return socketUDT.getLocalInetAddress();
	}

	@Override
	public int getLocalPort() {
		return socketUDT.getLocalInetPort();
	}

	@Override
	public SocketAddress getLocalSocketAddress() {
		try {
			return socketUDT.getLocalSocketAddress();
		} catch (final ExceptionUDT e) {
			return null;
		}
	}

	@Override
	public int getReceiveBufferSize() throws SocketException {
		return socketUDT.getReceiveBufferSize();
	}

	@Override
	public boolean getReuseAddress() throws SocketException {
		return socketUDT.getReuseAddress();
	}

	@Override
	public int getSoTimeout() throws IOException {
		return socketUDT.getSoTimeout();
	}

	@Override
	public boolean isBound() {
		return socketUDT.isBound();
	}

	@Override
	public boolean isClosed() {
		return socketUDT.isClosed();
	}

	@Override
	public void setPerformancePreferences(final int connectionTime,
			final int latency, final int bandwidth) {
		throw new UnsupportedOperationException("feature not available");
	}

	// NOTE: set both send and receive, since they are inherited on accept()
	@Override
	public void setReceiveBufferSize(final int size) throws SocketException {
		socketUDT.setReceiveBufferSize(size);
		socketUDT.setSendBufferSize(size);
	}

	@Override
	public void setReuseAddress(final boolean on) throws SocketException {
		socketUDT.setReuseAddress(on);
	}

	@Override
	public void setSoTimeout(final int timeout) throws SocketException {
		socketUDT.setSoTimeout(timeout);
	}

	@Override
	public SocketUDT socketUDT() {
		return socketUDT;
	}

}
