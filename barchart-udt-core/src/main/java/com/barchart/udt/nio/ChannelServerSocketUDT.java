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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

/**
 * you must use {@link SelectorProviderUDT#openServerSocketChannel()} to obtain
 * instance of this class; do not use JDK
 * {@link java.nio.channels.ServerSocketChannel#open()};
 * <p>
 * example:
 * 
 * <pre>
 * SelectorProvider provider = SelectorProviderUDT.DATAGRAM;
 * ServerSocketChannel acceptChannel = provider.openServerSocketChannel();
 * ServerSocket acceptSocket = acceptChannel.socket();
 * InetSocketAddress acceptAddress = new InetSocketAddress(&quot;localhost&quot;, 12345);
 * acceptorSocket.bind(acceptAddress);
 * assert acceptSocket.isBound();
 * SocketChannel connectChannel = acceptChannel.accept();
 * assert connectChannel.isConnected();
 * </pre>
 */
public class ChannelServerSocketUDT extends ServerSocketChannel implements
		ChannelUDT {

	protected static final Logger log = LoggerFactory
			.getLogger(ChannelServerSocketUDT.class);

	/** guarded by 'this' */
	protected ServerSocket socketAdapter;

	protected final SocketUDT socketUDT;

	protected ChannelServerSocketUDT( //
			final SelectorProviderUDT provider, //
			final SocketUDT socketUDT //
	) {

		super(provider);
		this.socketUDT = socketUDT;

	}

	@Override
	public ChannelSocketUDT accept() throws IOException {
		try {

			begin();

			final SocketUDT clientUDT = socketUDT.accept();

			if (clientUDT == null) {

				return null;

			} else {

				return new ChannelSocketUDT( //
						providerUDT(), //
						clientUDT, //
						clientUDT.isConnected() //
				);

			}
		} finally {
			end(true);
		}
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
	public boolean isConnectFinished() {
		return true;
	}

	@Override
	public boolean isOpenSocketUDT() {
		return socketUDT.isOpen();
	}

	@Override
	public KindUDT kindUDT() {
		return KindUDT.ACCEPTOR;
	}

	@Override
	public SelectorProviderUDT providerUDT() {
		return (SelectorProviderUDT) super.provider();
	}

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
	public String toString() {
		return socketUDT.toString();
	}

	@Override
	public TypeUDT typeUDT() {
		return providerUDT().type;
	}

}
