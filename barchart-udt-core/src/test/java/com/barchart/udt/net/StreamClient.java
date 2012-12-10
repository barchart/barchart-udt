package com.barchart.udt.net;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;
import com.barchart.udt.util.HelperUtils;

abstract class StreamClient extends StreamBase implements Runnable {

	private static final Logger log = LoggerFactory
			.getLogger(StreamClient.class);

	final ExecutorService executor;

	StreamClient(final TypeUDT type, final InetSocketAddress remoteAddress)
			throws Exception {

		super(new SocketUDT(type), HelperUtils.getLocalSocketAddress(),
				remoteAddress);

		this.executor = Executors.newCachedThreadPool();

	}

	void showtime() throws Exception {

		socket.bind(localAddress);
		assert socket.isBound();

		socket.connect(remoteAddress);
		assert socket.isConnected();

		executor.submit(this);

	}

	void shutdown() throws Exception {

		socket.close();

		executor.shutdown();

	}

}
