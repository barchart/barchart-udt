/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

import com.barchart.udt.net.NetServerSocketUDT;

public class NioServerSocketUDT extends NetServerSocketUDT {

	protected final ServerSocketChannelUDT channelUDT;

	protected NioServerSocketUDT(final ServerSocketChannelUDT channelUDT)
			throws IOException {
		super(channelUDT.socketUDT());
		this.channelUDT = channelUDT;
	}

	@Override
	public Socket accept() throws IOException {
		throw new RuntimeException("feature not available");
	}

	@Override
	public void bind(final SocketAddress endpoint) throws IOException {
		final SelectorProviderUDT provider = //
		(SelectorProviderUDT) channelUDT.provider();
		final int backlog = provider.getAcceptQueueSize();
		bind(endpoint, backlog);
	}

	@Override
	public ServerSocketChannelUDT getChannel() {
		return channelUDT;
	}

}
