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
import java.nio.channels.ServerSocketChannel;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.net.NetServerSocketUDT;

class AdapterServerSocketUDT extends NetServerSocketUDT {

	final ChannelServerSocketUDT serverChannelUDT;

	// exception thanks to JDK designers
	AdapterServerSocketUDT(ChannelServerSocketUDT channelSocketUDT,
			SocketUDT socketUDT) throws IOException {
		super(socketUDT);
		this.serverChannelUDT = channelSocketUDT;
	}

	//

	@Override
	public Socket accept() throws IOException {
		throw new RuntimeException("feature not available");
	}

	@Override
	public void bind(SocketAddress endpoint) throws IOException {
		SelectorProviderUDT provider = //
		(SelectorProviderUDT) serverChannelUDT.provider();
		int backlog = provider.getAcceptQueueSize();
		bind(endpoint, backlog);
	}

	@Override
	public ServerSocketChannel getChannel() {
		return serverChannelUDT;
	}

}
