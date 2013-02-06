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
import com.barchart.udt.StatusUDT;
import com.barchart.udt.TypeUDT;

class StreamServer extends StreamBase {

	private static final Logger log = LoggerFactory
			.getLogger(StreamServer.class);

	final ExecutorService executor;

	final ServiceFactory factory;

	@Override
	public void run() {

		try {

			final SocketUDT connectorSocket = socket.accept();

			final Runnable serviceTask = factory.newService(connectorSocket);

			executor.submit(serviceTask);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	StreamServer(final TypeUDT type, final InetSocketAddress serverAddress,
			final ServiceFactory factory) throws Exception {

		super(new SocketUDT(type), UnitHelp.localSocketAddress(), serverAddress);

		this.factory = factory;

		this.executor = Executors.newCachedThreadPool();

	}

	void showtime() throws Exception {

		socket.bind(remoteAddress);
		assert socket.isBound();

		socket.listen(1);
		assert socket.status() == StatusUDT.LISTENING;

		executor.submit(this);

	}

	void shutdown() throws Exception {

		socket.close();

		executor.shutdown();

	}

}
