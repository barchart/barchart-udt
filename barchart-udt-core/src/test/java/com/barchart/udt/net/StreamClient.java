/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.net;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.UnitHelp;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

abstract class StreamClient extends StreamBase implements Runnable {

	private static final Logger log = LoggerFactory
			.getLogger(StreamClient.class);

	final ExecutorService executor;

	StreamClient(final TypeUDT type, final InetSocketAddress remoteAddress)
			throws Exception {

		super(new SocketUDT(type), UnitHelp.localSocketAddress(), remoteAddress);

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
