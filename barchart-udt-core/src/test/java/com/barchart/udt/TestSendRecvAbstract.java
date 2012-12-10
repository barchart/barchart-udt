/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4'.VERSION='1.0.0-SNAPSHOT'.TIMESTAMP='2009-09-09_23-19-15'
 *
 * Copyright (C) 2009, Barchart, Inc. (http://www.barchart.com/)
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
package com.barchart.udt;

import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.util.HelperUtils;

public abstract class TestSendRecvAbstract<T> {

	final static Logger log = LoggerFactory
			.getLogger(TestSendRecvAbstract.class);

	final static int TEST_TIMEOUT = 30; // seconds

	final static int SIZE = 1460;
	final static int COUNT = 1000;
	final static int THREADS = 4;

	volatile SocketUDT server;
	volatile SocketUDT connector;

	volatile SocketUDT client;

	volatile InetSocketAddress serverAddress;
	volatile InetSocketAddress clientAddress;

	volatile ExecutorService service;

	volatile CyclicBarrier barrier;

	volatile BlockingQueue<T> serverQueue;
	volatile BlockingQueue<T> clientQueue;

	final Random generator = new Random();

	//

	protected abstract void doServerReader() throws Exception;

	protected abstract void doServerWriter() throws Exception;

	protected abstract void doClientReader() throws Exception;

	protected abstract void doClientWriter() throws Exception;

	//

	final Runnable serverReader = new Runnable() {
		@Override
		public void run() {
			try {
				log.info("server reader start");
				server.bind(serverAddress);
				server.listen(1);
				// blocks here
				connector = server.accept();
				for (int k = 0; k < COUNT; k++) {
					doServerReader();
				}
				log.info("server reader finish");
			} catch (Exception e) {
				log.error("", e);
				fail(e.getMessage());
			}
			try {
				barrier.await();
			} catch (Exception e) {
				log.error("", e);
			}
		}

	};

	final Runnable serverWriter = new Runnable() {
		@Override
		public void run() {
			try {
				log.info("server writer start");
				for (int k = 0; k < COUNT; k++) {
					doServerWriter();
				}
				log.info("server writer finish");
			} catch (Exception e) {
				log.error("", e);
				fail(e.getMessage());
			}
			try {
				barrier.await();
			} catch (Exception e) {
				log.error("", e);
			}
		}
	};

	final Runnable clientWriter = new Runnable() {
		@Override
		public void run() {
			try {
				log.info("client writer start");
				client.bind(clientAddress);
				// blocks here
				client.connect(serverAddress);
				for (int k = 0; k < COUNT; k++) {
					doClientWriter();
				}
				log.info("client writer finish");
			} catch (Exception e) {
				log.error("", e);
				fail(e.getMessage());
			}
			try {
				barrier.await();
			} catch (Exception e) {
				log.error("", e);
			}
		}
	};

	final Runnable clientReader = new Runnable() {
		@Override
		public void run() {
			try {
				log.info("client reader start");
				for (int k = 0; k < COUNT; k++) {
					doClientReader();
				}
				log.info("client reader finish");
			} catch (Exception e) {
				log.error("", e);
				fail(e.getMessage());
			}
			try {
				barrier.await();
			} catch (Exception e) {
				log.error("", e);
			}
		}
	};

	@Before
	public void setUp() throws Exception {

		log.info("started {}", System.getProperty("os.arch"));

		serverQueue = new LinkedBlockingQueue<T>();
		clientQueue = new LinkedBlockingQueue<T>();

		server = new SocketUDT(TypeUDT.DATAGRAM);
		client = new SocketUDT(TypeUDT.DATAGRAM);

		server.configureBlocking(true);
		client.configureBlocking(true);

		serverAddress = HelperUtils.getLocalSocketAddress();
		clientAddress = HelperUtils.getLocalSocketAddress();

		log.info("serverAddress={} clientAddress={}", serverAddress,
				clientAddress);

		service = Executors.newFixedThreadPool(THREADS);

		barrier = new CyclicBarrier(THREADS + 1);

	}

	@After
	public void tearDown() throws Exception {

		client.close();

		server.close();

		service.shutdownNow();

		barrier.reset();

	}

	@Test
	public void testSend0Receive0() {

		log.info("testSend0Receive0");

		try {

			service.execute(serverReader);
			service.execute(serverWriter);
			service.execute(clientReader);
			service.execute(clientWriter);

			barrier.await(TEST_TIMEOUT, TimeUnit.SECONDS);

		} catch (Exception e) {
			log.error("", e);
			fail(e.getMessage());
		}

	}

}
