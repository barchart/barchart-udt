/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
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

import util.TestAny;
import util.UnitHelp;

public abstract class TestSendRecvAbstract<T> extends TestAny {

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
			} catch (final Exception e) {
				log.error("", e);
				fail(e.getMessage());
			}
			try {
				barrier.await();
			} catch (final Exception e) {
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
			} catch (final Exception e) {
				log.error("", e);
				fail(e.getMessage());
			}
			try {
				barrier.await();
			} catch (final Exception e) {
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
			} catch (final Exception e) {
				log.error("", e);
				fail(e.getMessage());
			}
			try {
				barrier.await();
			} catch (final Exception e) {
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
			} catch (final Exception e) {
				log.error("", e);
				fail(e.getMessage());
			}
			try {
				barrier.await();
			} catch (final Exception e) {
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

		server.setBlocking(true);
		client.setBlocking(true);

		serverAddress = UnitHelp.localSocketAddress();
		clientAddress = UnitHelp.localSocketAddress();

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

		} catch (final Exception e) {
			log.error("", e);
			fail(e.getMessage());
		}

	}

}
