/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import static java.nio.channels.SelectionKey.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	protected static final Logger log = LoggerFactory
			.getLogger(ChannelServerSocketUDT.class);

	/** note: 1<->1 mapping of channels and keys */
	protected volatile SelectionKeyUDT channelKey;

	@Override
	public void bindKey(final SelectionKeyUDT key) {
		assert channelKey == null;
		channelKey = key;
	}

	protected final SocketUDT socketUDT;

	protected ChannelServerSocketUDT(final SelectorProvider provider,
			final SocketUDT socketUDT) {
		super(provider);
		this.socketUDT = socketUDT;
	}

	@Override
	protected void implCloseSelectableChannel() throws IOException {
		socketUDT.close();
	}

	@Override
	protected void implConfigureBlocking(final boolean block)
			throws IOException {
		socketUDT.configureBlocking(block);
	}

	@Override
	public SocketChannel accept() throws IOException {
		try {

			begin();

			final SocketUDT clientUDT = socketUDT.accept();

			if (clientUDT == null) {

				return null;

			} else {

				/** FIXME review select contract */
				final SelectionKeyUDT key = channelKey;
				if (key != null) {
					key.readyOps &= ~OP_ACCEPT;
				}

				return new ChannelSocketUDT(provider(), clientUDT);

			}
		} finally {
			end(true);
		}
	}

	/** guarded by 'this' */
	protected ServerSocket socketAdapter;

	@Override
	public ServerSocket socket() {
		synchronized (this) {
			if (socketAdapter == null) {
				try {
					socketAdapter = new AdapterServerSocketUDT(this, socketUDT);
				} catch (final IOException e) {
					return null;
				}
			}
			return socketAdapter;
		}
	}

	@Override
	public SocketUDT socketUDT() {
		return socketUDT;
	}

	@Override
	public KindUDT kindUDT() {
		return KindUDT.ACCEPTOR;
	}

	@Override
	public boolean isOpenSocketUDT() {
		return socketUDT.isOpen();
	}

	//

	@Override
	public String toString() {
		return socketUDT.toString();
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
