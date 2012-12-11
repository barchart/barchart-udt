/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
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

import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class NetSocketUDT extends Socket implements IceSocket {

	protected InputStream inputStream;
	protected OutputStream outputStream;

	protected final SocketUDT socketUDT;

	public NetSocketUDT() throws ExceptionUDT {
		this.socketUDT = new SocketUDT(TypeUDT.STREAM);
		this.socketUDT.configureBlocking(true);
	}

	/** NOTE: you just carefully choose TypeUDT */
	public NetSocketUDT(final SocketUDT socketUDT) {
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
	public void close() throws IOException {
		socketUDT.close();
	}

	@Override
	public void connect(SocketAddress endpoint) throws IOException {
		socketUDT.connect((InetSocketAddress) endpoint);
	}

	@Override
	public void connect(SocketAddress endpoint, int timeout) throws IOException {
		throw new RuntimeException("feature not available");
	}

	@Override
	public SocketChannel getChannel() {
		throw new RuntimeException("feature not available");
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
		throw new RuntimeException("feature not available");
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
		} catch (ExceptionUDT e) {
			return null;
		}
	}

	@Override
	public boolean getOOBInline() throws SocketException {
		throw new RuntimeException("feature not available");
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
	public int getReceiveBufferSize() throws SocketException {
		return socketUDT.getReceiveBufferSize();
	}

	@Override
	public SocketAddress getRemoteSocketAddress() {
		try {
			return socketUDT.getRemoteSocketAddress();
		} catch (ExceptionUDT e) {
			return null;
		}
	}

	@Override
	public boolean getReuseAddress() throws SocketException {
		return socketUDT.getReuseAddress();
	}

	@Override
	public int getSendBufferSize() throws SocketException {
		return socketUDT.getSendBufferSize();
	}

	@Override
	public int getSoLinger() throws SocketException {
		return socketUDT.getSoLinger();
	}

	@Override
	public int getSoTimeout() throws SocketException {
		return socketUDT.getSoTimeout();
	}

	@Override
	public boolean getTcpNoDelay() throws SocketException {
		throw new RuntimeException("feature not available");
	}

	@Override
	public int getTrafficClass() throws SocketException {
		throw new RuntimeException("feature not available");
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
	public void sendUrgentData(int data) throws IOException {
		throw new RuntimeException("feature not available");
	}

	@Override
	public void setKeepAlive(boolean on) throws SocketException {
		throw new RuntimeException("feature not available");
	}

	@Override
	public void setOOBInline(boolean on) throws SocketException {
		throw new RuntimeException("feature not available");
	}

	@Override
	public void setPerformancePreferences(int connectionTime, int latency,
			int bandwidth) {
		throw new RuntimeException("feature not available");
	}

	@Override
	public void setReceiveBufferSize(int size) throws SocketException {
		socketUDT.setReceiveBufferSize(size);
	}

	@Override
	public void setReuseAddress(boolean on) throws SocketException {
		socketUDT.setReuseAddress(on);
	}

	@Override
	public void setSendBufferSize(int size) throws SocketException {
		socketUDT.setSendBufferSize(size);
	}

	@Override
	public void setSoLinger(boolean on, int linger) throws SocketException {
		socketUDT.setSoLinger(on, linger);
	}

	@Override
	public void setSoTimeout(int timeout) throws SocketException {
		socketUDT.setSoTimeout(timeout);
	}

	@Override
	public void setTcpNoDelay(boolean on) throws SocketException {
		throw new RuntimeException("feature not available");
	}

	@Override
	public void setTrafficClass(int tc) throws SocketException {
		throw new RuntimeException("feature not available");
	}

	@Override
	public void shutdownInput() throws IOException {
		socketUDT.close();
	}

	@Override
	public void shutdownOutput() throws IOException {
		socketUDT.close();
	}

}
