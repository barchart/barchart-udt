/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;
import com.barchart.udt.anno.ThreadSafe;

/**
 * {@link Socket} - like wrapper for {@link SocketUDT}
 */
public class NetSocketUDT extends Socket implements IceSocket, IceCommon {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@ThreadSafe("this")
	protected InputStream inputStream;
	@ThreadSafe("this")
	protected OutputStream outputStream;

	protected final SocketUDT socketUDT;

	/** uses {@link TypeUDT#STREAM} socket in blocking mode */
	public NetSocketUDT() throws ExceptionUDT {
		this(new SocketUDT(TypeUDT.STREAM));
		this.socketUDT.setBlocking(true);
	}

	/** uses provided socket keeping blocking mode */
	protected NetSocketUDT(final SocketUDT socketUDT) {
		this.socketUDT = socketUDT;
	}

	@Override
	public void bind(SocketAddress bindpoint) throws IOException {
		if (bindpoint == null) {
			bindpoint = new InetSocketAddress(0);
		}
		socketUDT.bind((InetSocketAddress) bindpoint);
	}

	@Override
	public synchronized void close() throws IOException {
		socketUDT.close();
	}

	@Override
	public void connect(final SocketAddress endpoint) throws IOException {
		socketUDT.connect((InetSocketAddress) endpoint);
	}

	@Override
	public void connect(final SocketAddress endpoint, final int timeout)
			throws IOException {
		throw new UnsupportedOperationException("feature not available");
	}

	@Override
	public SocketChannel getChannel() {
		throw new UnsupportedOperationException("feature not available");
	}

	@Override
	public InetAddress getInetAddress() {
		return socketUDT.getRemoteInetAddress();
	}

	@Override
	public synchronized InputStream getInputStream() throws IOException {
		if (inputStream == null) {
			inputStream = new NetInputStreamUDT(socketUDT);
		}
		return inputStream;
	}

	@Override
	public boolean getKeepAlive() throws SocketException {
		// UDT has keep alive automatically under the
		// hood which I believe you cannot turn off
		return true;
	}

	@Override
	public InetAddress getLocalAddress() {
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
	public boolean getOOBInline() throws SocketException {
		return false;
	}

	@Override
	public synchronized OutputStream getOutputStream() throws IOException {
		if (outputStream == null) {
			outputStream = new NetOutputStreamUDT(socketUDT);
		}
		return outputStream;
	}

	@Override
	public int getPort() {
		return socketUDT.getRemoteInetPort();
	}

	@Override
	public synchronized int getReceiveBufferSize() throws SocketException {
		return socketUDT.getReceiveBufferSize();
	}

	@Override
	public SocketAddress getRemoteSocketAddress() {
		try {
			return socketUDT.getRemoteSocketAddress();
		} catch (final ExceptionUDT e) {
			return null;
		}
	}

	@Override
	public boolean getReuseAddress() throws SocketException {
		return socketUDT.getReuseAddress();
	}

	@Override
	public synchronized int getSendBufferSize() throws SocketException {
		return socketUDT.getSendBufferSize();
	}

	@Override
	public int getSoLinger() throws SocketException {
		return socketUDT.getSoLinger();
	}

	@Override
	public synchronized int getSoTimeout() throws SocketException {
		return socketUDT.getSoTimeout();
	}

	@Override
	public boolean getTcpNoDelay() throws SocketException {
		return false;
	}

	@Override
	public int getTrafficClass() throws SocketException {
		return 0;
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
	public boolean isConnected() {
		return socketUDT.isConnected();
	}

	@Override
	public boolean isInputShutdown() {
		return socketUDT.isClosed();
	}

	@Override
	public boolean isOutputShutdown() {
		return socketUDT.isClosed();
	}

	@Override
	public void sendUrgentData(final int data) throws IOException {
		log.debug("Sending urgent data not supported in Barchart UDT...");
	}

	@Override
	public void setKeepAlive(final boolean on) throws SocketException {
		log.debug("Keep alive not supported in Barchart UDT...");
	}

	@Override
	public void setOOBInline(final boolean on) throws SocketException {
		log.debug("OOB inline  not supported in Barchart UDT...");
	}

	@Override
	public void setPerformancePreferences(final int connectionTime,
			final int latency, final int bandwidth) {
	}

	@Override
	public synchronized void setReceiveBufferSize(final int size)
			throws SocketException {
		socketUDT.setReceiveBufferSize(size);
	}

	@Override
	public void setReuseAddress(final boolean on) throws SocketException {
		socketUDT.setReuseAddress(on);
	}

	@Override
	public synchronized void setSendBufferSize(final int size)
			throws SocketException {
		socketUDT.setSendBufferSize(size);
	}

	@Override
	public void setSoLinger(final boolean on, final int linger)
			throws SocketException {
		socketUDT.setSoLinger(on, linger);
	}

	@Override
	public synchronized void setSoTimeout(final int timeout)
			throws SocketException {
		socketUDT.setSoTimeout(timeout);
	}

	@Override
	public void setTcpNoDelay(final boolean on) throws SocketException {
		log.debug("TCP no delay not supported in Barchart UDT...");
	}

	@Override
	public void setTrafficClass(final int tc) throws SocketException {
		log.debug("Traffic class not supported in Barchart UDT...");
	}

	@Override
	public void shutdownInput() throws IOException {
		socketUDT.close();
	}

	@Override
	public void shutdownOutput() throws IOException {
		socketUDT.close();
	}

	@Override
	public SocketUDT socketUDT() {
		return socketUDT;
	}

}
