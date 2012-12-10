/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4';VERSION='1.0.2-SNAPSHOT';TIMESTAMP='2011-01-11_09-30-59';
 *
 * Copyright (C) 2009-2011, Barchart, Inc. (http://www.barchart.com/)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *     * Neither the name of the Barchart, Inc. nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Developers: Andrei Pozolotin;
 *
 * =================================================================================
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
