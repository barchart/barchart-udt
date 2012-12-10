package com.barchart.udt.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;

abstract class StreamService extends StreamBase {

	private static final Logger log = LoggerFactory
			.getLogger(StreamService.class);

	StreamService(final SocketUDT socket) throws Exception {

		super(socket, socket.getLocalSocketAddress(), socket
				.getRemoteSocketAddress());

		assert socket.isConnected();

	}

	void shutdown() throws Exception {

		socket.close();

	}

}
