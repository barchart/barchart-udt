/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.net.NetServerSocketUDT;

public class AdapterServerSocketUDT extends NetServerSocketUDT {

	protected final ChannelServerSocketUDT channelUDT;

	protected AdapterServerSocketUDT( //
			final ChannelServerSocketUDT channelUDT, //
			final SocketUDT socketUDT //
	) throws IOException {

		super(socketUDT);
		this.channelUDT = channelUDT;

	}

	//

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
	public ChannelServerSocketUDT getChannel() {
		return channelUDT;
	}

}
