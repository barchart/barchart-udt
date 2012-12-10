/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4';VERSION='1.0.2-SNAPSHOT';TIMESTAMP='2011-01-11_09-30-59';
 *
 * Copyright (C) 2009-2011, Barchart, Inc. (http://www.barchart.com/)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *     * Neither the name of the Barchart, Inc. nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Developers: Andrei Pozolotin;
 *
 * =================================================================================
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
	public void bind(SocketAddress endpoint) throws IOException {
		final int backlog = SocketUDT.DEFAULT_ACCEPT_QUEUE_SIZE;
		bind(endpoint, backlog);
	}

	// NOTE: bind() means listen() for UDT server socket
	/*
	 * The listen method lets a UDT socket enter listening state. The socket
	 * must call bind before a listen call.
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
		} catch (ExceptionUDT e) {
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
	public void setPerformancePreferences(int connectionTime, int latency,
			int bandwidth) {
		throw new RuntimeException("feature not available");
	}

	// NOTE: set both send and receive, since they are inherited on accept()
	@Override
	public void setReceiveBufferSize(int size) throws SocketException {
		serverSocketUDT.setReceiveBufferSize(size);
		serverSocketUDT.setSendBufferSize(size);
	}

	@Override
	public void setReuseAddress(boolean on) throws SocketException {
		serverSocketUDT.setReuseAddress(on);
	}

	@Override
	public void setSoTimeout(int timeout) throws SocketException {
		serverSocketUDT.setSoTimeout(timeout);
	}

}
