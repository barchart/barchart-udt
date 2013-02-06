/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.TestAny;

/**
 * verify mingw c++ exceptions are thread safe (will crash jvm if not using
 * -mthreads option for gcc/ld)
 */
public class TestExceptions extends TestAny {

	final static int TEST_TIMEOUT = 10; // seconds

	static final int SIZE = 1460;
	static final int COUNT = 1000;
	static final int THREADS = 30;

	volatile CyclicBarrier barrier;

	volatile ExecutorService service;

	volatile AtomicInteger exceptionCount;

	@Before
	public void setUp() throws Exception {

		exceptionCount = new AtomicInteger(0);

		barrier = new CyclicBarrier(THREADS + 1);

		service = Executors.newFixedThreadPool(THREADS);

	}

	@After
	public void tearDown() throws Exception {

		service.shutdownNow();

	}

	static final AtomicInteger threadCount = new AtomicInteger(0);

	final Runnable exceptionTask = new Runnable() {
		@Override
		public void run() {

			Thread.currentThread().setName(
					"Exception Runner #" + threadCount.getAndIncrement());

			final ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE);

			SocketUDT socket = null;

			try {
				socket = new SocketUDT(TypeUDT.DATAGRAM);
			} catch (final Exception e) {
				fail("can not init socket; " + e.getMessage());
			}

			for (int k = 0; k < COUNT; k++) {
				try {

					/** must throw exception */
					socket.receive(buffer);

					fail("exception not thrown");

				} catch (final Exception e) {

					exceptionCount.getAndIncrement();

				}
			}

			if (socket != null) {
				try {
					socket.close();
				} catch (final Exception e) {
					fail("can not close socket");
				}
			}

			try {
				barrier.await();
			} catch (final Exception e) {
				fail(e.getMessage());
			}

		}
	};

	@Test
	public void testException() {

		log.info("start");

		for (int k = 0; k < THREADS; k++) {
			service.submit(exceptionTask);
		}

		try {
			barrier.await(TEST_TIMEOUT, TimeUnit.SECONDS);
		} catch (final Exception e) {
			fail(e.getMessage());
		}

		assertEquals(exceptionCount.get(), THREADS * COUNT);

		log.info("finish");

	}

}
