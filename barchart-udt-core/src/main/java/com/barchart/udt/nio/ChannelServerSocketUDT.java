/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import com.barchart.udt.SocketUDT;

/**
 * you must use {@link SelectorProviderUDT#openServerSocketChannel()} to obtain
 * instance of this class; do not use JDK
 * {@link java.nio.channels.ServerSocketChannel#open()}; <br>
 * 
 * example:
 * 
 * [code]
 * 
 * SelectorProvider provider = SelectorProviderUDT.DATAGRAM;
 * 
 * ServerSocketChannel acceptorChannel = provider.openServerSocketChannel();
 * 
 * ServerSocket acceptorSocket = acceptorChannel.socket();
 * 
 * InetSocketAddress acceptorAddress= new InetSocketAddress("localhost", 12345);
 * 
 * acceptorSocket.bind(acceptorAddress);
 * 
 * assert acceptorSocket.isBound();
 * 
 * SocketChannel connectorChannel = acceptorChannel.accept();
 * 
 * assert connectorChannel.isConnected();
 * 
 * [/code]
 */
public class ChannelServerSocketUDT extends ServerSocketChannel implements
		ChannelUDT {

	protected final SocketUDT serverSocketUDT;

	protected ChannelServerSocketUDT(final SelectorProvider provider,
			final SocketUDT socketUDT) {
		super(provider);
		this.serverSocketUDT = socketUDT;
	}

	@Override
	protected void implCloseSelectableChannel() throws IOException {
		serverSocketUDT.close();
	}

	@Override
	protected void implConfigureBlocking(final boolean block)
			throws IOException {
		serverSocketUDT.configureBlocking(block);
	}

	@Override
	public SocketChannel accept() throws IOException {
		try {
			begin();
			final SocketUDT socketUDT = serverSocketUDT.accept();
			if (socketUDT == null) {
				return null;
			} else {
				return new ChannelSocketUDT(provider(), socketUDT);
			}
		} finally {
			end(true);
		}
	}

	// guarded by 'this'
	private ServerSocket serverSocketAdapter;

	@Override
	public ServerSocket socket() {
		synchronized (this) {
			if (serverSocketAdapter == null) {
				try {
					serverSocketAdapter = new AdapterServerSocketUDT(this,
							serverSocketUDT);
				} catch (final IOException e) {
					return null;
				}
			}
			return serverSocketAdapter;
		}
	}

	@Override
	public SocketUDT socketUDT() {
		return serverSocketUDT;
	}

	@Override
	public KindUDT kindUDT() {
		return KindUDT.ACCEPTOR;
	}

	@Override
	public boolean isOpenSocketUDT() {
		return serverSocketUDT.isOpen();
	}

	//

	@Override
	public String toString() {
		return serverSocketUDT.toString();
	}

	@Override
	public boolean isConnectionPending() {
		return false;
	}

	@Override
	public boolean finishConnect() {
		return false;
	}

}
