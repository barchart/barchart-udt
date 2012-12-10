/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConnectorTaskUDT implements Runnable {

	private static final Logger log = LoggerFactory
			.getLogger(ConnectorTaskUDT.class);

	final SelectionKeyUDT keyUDT;
	final ConcurrentMap<SelectionKeyUDT, Runnable> taskMap;
	final Queue<SelectionKeyUDT> readyQueue;
	final InetSocketAddress remoteSocketAddress;

	ConnectorTaskUDT(SelectionKeyUDT keyUDT,
			ConcurrentMap<SelectionKeyUDT, Runnable> taskMap,
			Queue<SelectionKeyUDT> readyQueue,
			InetSocketAddress remoteSocketAddress) {
		this.keyUDT = keyUDT;
		this.taskMap = taskMap;
		this.readyQueue = readyQueue;
		this.remoteSocketAddress = remoteSocketAddress;
	}

	@Override
	public void run() {

		IOException exception = null;

		assert keyUDT.channelUDT.getChannelKind() == KindUDT.CONNECTOR;

		log.debug("connect() start 	: socketID={} remoteSocketAddress={}", //
				keyUDT.socketID, remoteSocketAddress);

		try {

			// NOTE:
			// will block here
			// connect socket, not channel
			keyUDT.socketUDT.connect(remoteSocketAddress);

		} catch (IOException e) {

			// do not process; will be thrown later
			exception = e;

		} catch (Throwable e) {

			// unexpected - wrap; also process here
			log.error("run failed", e);
			exception = new IOException(e.getMessage());

		} finally {

			ChannelSocketUDT socketChannel = (ChannelSocketUDT) keyUDT.channelUDT;
			socketChannel.setConnectException(exception);

			taskMap.remove(keyUDT);
			readyQueue.offer(keyUDT);
			keyUDT.selectorUDT.wakeup();

		}

		log.debug("connect() finish : socketID={} remoteSocketAddress={}", //
				keyUDT.socketID, remoteSocketAddress);

	}

}
