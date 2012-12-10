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

import java.nio.ByteBuffer;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * verify mingw c++ exceptions are thread safe (will crash jvm if not using
 * -mthreads option for gcc/ld)
 */
public class TestException {

	static final Logger log = LoggerFactory.getLogger(TestException.class);

	final static int TEST_TIMEOUT = 10; // seconds

	static final int SIZE = 1460;
	static final int COUNT = 1000;
	static final int THREADS = 30;

	volatile CyclicBarrier barrier;

	volatile ExecutorService service;

	volatile AtomicInteger exceptionCount;

	@Before
	public void setUp() throws Exception {

		log.info("started {}", System.getProperty("os.arch"));

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
			} catch (Exception e) {
				fail("can not make socket; " + e.getMessage());
			}

			for (int k = 0; k < COUNT; k++) {
				try {
					// log.info("k={}", k);
					// InetSocketAddress remoteSocketAddress = new
					// InetSocketAddress(0);
					// if (k % 1000 == 0) {
					// log.info("k={}", k);
					// }
					//
					// must throw exception
					socket.receive(buffer);
					//
					fail("exception not thrown");
					//
				} catch (Exception e) {
					// log.info("e={}", e.getMessage());
					exceptionCount.getAndIncrement();
				}
			}

			try {
				barrier.await();
			} catch (Exception e) {
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
		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertEquals(exceptionCount.get(), THREADS * COUNT);

		log.info("finish");

	}

}
