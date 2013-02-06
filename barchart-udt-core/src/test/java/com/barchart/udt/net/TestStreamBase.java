/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.net;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.StopWatch;
import util.TestAny;
import util.UnitHelp;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class TestStreamBase extends TestAny {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	final ServiceFactory factory1 = new ServiceFactory() {
		@Override
		public StreamService newService(final SocketUDT connectorSocket)
				throws Exception {
			return new StreamService(connectorSocket) {
				@Override
				public void run() {
					while (true) {
						try {
							final int value = streamIn.read();
							streamOut.write(value);
						} catch (final IOException e) {
							log.error("server; {}", e.getMessage());
							break;
						}
					}
				}
			};
		}
	};

	@Test
	public void testStream11() throws Exception {

		final InetSocketAddress serverAddress = UnitHelp.localSocketAddress();

		final StreamServer server = new StreamServer(TypeUDT.DATAGRAM,
				serverAddress, factory1);

		final StreamClient client = new StreamClient(TypeUDT.DATAGRAM,
				serverAddress) {
			@Override
			public void run() {
				try {
					final int loop = 100;
					final StopWatch timer = new StopWatch();
					timer.start();
					for (int k = 0; k < loop; k++) {
						for (int index = Byte.MIN_VALUE; index <= Byte.MAX_VALUE; index++) {
							streamOut.write(index);
						}
						for (int index = Byte.MIN_VALUE; index <= Byte.MAX_VALUE; index++) {
							final int value = streamIn.read();
							assertEquals(value, index);
						}
					}
					timer.stop();
					log.info("timer : {}", timer.nanoString());
					synchronized (this) {
						this.notifyAll();
					}
				} catch (final Exception e) {
					log.error("client; {}", e.getMessage());
				}
			}
		};

		server.showtime();
		client.showtime();

		synchronized (client) {
			client.wait();
		}

		client.shutdown();
		server.shutdown();

	}

	// #########################################################

	final ServiceFactory factory2 = new ServiceFactory() {
		@Override
		public StreamService newService(final SocketUDT connectorSocket)
				throws Exception {
			return new StreamService(connectorSocket) {
				@Override
				public void run() {
					final int size = 1234;
					final byte[] array = new byte[size];
					while (true) {
						try {
							final int count = streamIn.read(array);
							streamOut.write(array, 0, count);
						} catch (final IOException e) {
							log.error("server; {}", e.getMessage());
							break;
						}
					}
				}
			};
		}
	};

	@Test
	public void testStream22() throws Exception {

		final InetSocketAddress serverAddress = UnitHelp.localSocketAddress();

		final StreamServer server = new StreamServer(TypeUDT.DATAGRAM,
				serverAddress, factory2);

		final StreamClient client = new StreamClient(TypeUDT.DATAGRAM,
				serverAddress) {
			@Override
			public void run() {
				final Random random = new Random();
				final int loop = 10000;
				final int size = 1000;
				final byte[] arrayOut = new byte[size];
				final byte[] arrayIn = new byte[size];
				try {
					final StopWatch timer = new StopWatch();
					timer.start();
					for (int k = 0; k < loop; k++) {
						random.nextBytes(arrayOut);
						streamOut.write(arrayOut);
						final int count = streamIn.read(arrayIn);
						assertEquals(count, size);
						assertTrue(Arrays.equals(arrayIn, arrayOut));
					}
					timer.stop();
					log.info("timer : {}", timer.nanoString());
					synchronized (this) {
						this.notifyAll();
					}
				} catch (final Exception e) {
					log.error("client; {}", e.getMessage());
				}
			}
		};

		server.showtime();
		client.showtime();

		synchronized (client) {
			client.wait();
		}

		client.shutdown();
		server.shutdown();

	}

	// #########################################################

	// @Test
	public void testStream12() throws Exception {

		final InetSocketAddress serverAddress = UnitHelp.localSocketAddress();

		final StreamServer server = new StreamServer(TypeUDT.STREAM,
				serverAddress, factory1);

		final StreamClient client = new StreamClient(TypeUDT.STREAM,
				serverAddress) {
			@Override
			public void run() {
				final Random random = new Random();
				final int loop = 3;
				final int size = 100;
				final byte[] arrayOut = new byte[size];
				final byte[] arrayIn = new byte[size];
				try {
					final StopWatch timer = new StopWatch();
					timer.start();
					for (int k = 0; k < loop; k++) {
						random.nextBytes(arrayOut);
						streamOut.write(arrayOut);
						final int count = streamIn.read(arrayIn);
						assertEquals(count, size);
						assertTrue(Arrays.equals(arrayIn, arrayOut));
					}
					timer.stop();
					log.info("timer : {}", timer.nanoString());
					synchronized (this) {
						this.notifyAll();
					}
				} catch (final Exception e) {
					log.error("client; {}", e.getMessage());
				}
			}
		};

		server.showtime();
		client.showtime();

		synchronized (client) {
			client.wait();
		}

		client.shutdown();
		server.shutdown();

	}

}
