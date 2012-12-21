/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import static com.barchart.udt.util.TestHelp.*;
import static org.junit.Assert.*;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestOption {

	Logger log = LoggerFactory.getLogger(TestOption.class);

	@Before
	public void setUp() throws Exception {

		log.info("started {}", System.getProperty("os.arch"));

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testOptionBasic() {

		try {

			final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

			OptionUDT option;

			option = OptionUDT.UDT_SNDSYN;
			boolean booleanValue;
			booleanValue = true;
			socket.setOption(option, booleanValue);
			assertEquals(booleanValue, socket.getOption(option));
			booleanValue = false;
			socket.setOption(option, booleanValue);
			assertEquals(booleanValue, socket.getOption(option));

			log.info("pass: boolean");

			option = OptionUDT.UDP_RCVBUF;
			int intValue;
			intValue = 123456789;
			socket.setOption(option, intValue);
			assertEquals(intValue, socket.getOption(option));
			intValue = 987654321;
			socket.setOption(option, intValue);
			assertEquals(intValue, socket.getOption(option));

			log.info("pass: int");

			option = OptionUDT.UDT_MAXBW;
			long longValue;
			longValue = 1234567890123456789L;
			socket.setOption(option, longValue);
			assertEquals(longValue, socket.getOption(option));
			longValue = 8765432109876543210L;
			socket.setOption(option, longValue);
			assertEquals(longValue, socket.getOption(option));

			log.info("pass: long");

		} catch (final Exception e) {
			fail("SocketException; " + e.getMessage());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOptionLingerRange1() throws ExceptionUDT {
		new LingerUDT(12345678);
	}

	@Test
	public void testOptionLingerRange2() throws ExceptionUDT {
		new LingerUDT(-12345678);
		new LingerUDT(0);
		new LingerUDT(65535);
	}

	@Test
	public void testOptionLinger() {

		try {

			final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

			final OptionUDT option = OptionUDT.UDT_LINGER;

			final LingerUDT linger1 = new LingerUDT(65432);
			socket.setOption(option, linger1);
			assertEquals(linger1, socket.getOption(option));

			final LingerUDT linger2 = new LingerUDT(-12345);
			socket.setOption(option, linger2);
			assertEquals(LingerUDT.LINGER_ZERO, socket.getOption(option));

			log.info("pass: linger");

		} catch (final Exception e) {
			fail("SocketException; " + e.getMessage());
		}

	}

	@Test
	public void testOptionsPrint() {
		try {

			final SocketUDT serverSocket = new SocketUDT(TypeUDT.DATAGRAM);
			final InetSocketAddress serverAddress = getLocalSocketAddress();
			serverSocket.bind(serverAddress);
			serverSocket.listen(1);
			assertTrue(serverSocket.isBound());

			final SocketUDT clientSocket = new SocketUDT(TypeUDT.DATAGRAM);
			final InetSocketAddress clientAddress = getLocalSocketAddress();
			clientSocket.bind(clientAddress);
			assertTrue(clientSocket.isBound());

			clientSocket.connect(serverAddress);
			assertTrue(clientSocket.isConnected());

			final SocketUDT acceptSocket = serverSocket.accept();
			assertTrue(acceptSocket.isConnected());

			log.info("client options:{}", clientSocket.toStringOptions());
			log.info("accept options:{}", acceptSocket.toStringOptions());

		} catch (final Exception e) {
			fail(e.getMessage());
		}

	}

	/**
	 * This test uses the UDT_CC option using the UDPBlast custom congestion
	 * control algorithm limiting bandwidth to 50Mbs between localhost
	 */
	@Test
	public void testOptionCC() {

		try {

			// UDT has roughly 7% congestion control overhead
			// so we need to take that into account if you want
			// to limit wire speed
			final double maxBW = 50 * 0.93;
			log.info("Attempting to rate limit using custom CCC UDPBlast class");

			final SocketUDT serverSocket = new SocketUDT(TypeUDT.STREAM);
			final InetSocketAddress serverAddress = getLocalSocketAddress();
			serverSocket.bind(serverAddress);
			serverSocket.listen(1);
			assertTrue(serverSocket.isBound());

			final SocketUDT clientSocket = new SocketUDT(TypeUDT.STREAM);

			clientSocket.setOption(OptionUDT.UDT_CC, new FactoryUDT<UDPBlast>(
					UDPBlast.class));

			final InetSocketAddress clientAddress = getLocalSocketAddress();

			clientSocket.bind(clientAddress);
			clientSocket.setSoLinger(false, 0);
			assertTrue(clientSocket.isBound());

			clientSocket.connect(serverAddress);
			assertTrue(clientSocket.isConnected());

			final SocketUDT acceptSocket = serverSocket.accept();
			assertTrue(acceptSocket.isConnected());

			final Object obj = clientSocket.getOption(OptionUDT.UDT_CC);

			assertTrue("UDT_CC Object is of unexpected type",
					obj instanceof UDPBlast);

			final UDPBlast objCCC = (UDPBlast) obj;
			objCCC.setRate((int) maxBW);

			final byte[] data = new byte[65536];
			final long start = System.currentTimeMillis();
			long interval = System.currentTimeMillis();
			double maxMbs = -1;

			while ((System.currentTimeMillis() - start) < 15000) {

				clientSocket.send(data);
				acceptSocket.receive(data);

				if (System.currentTimeMillis() - interval > 1000) {
					interval = System.currentTimeMillis();
					clientSocket.updateMonitor(true);
					final MonitorUDT mon = clientSocket.getMonitor();
					log.info(String.format("Current Rate: %.2f Mbs",
							mon.mbpsSendRate));

					maxMbs = mon.mbpsSendRate * 0.93;
				}
			}

			// check we are within 10% of our desired rate
			assertTrue("Max bandwidth exceeded limit",
					maxMbs < (maxBW + (maxBW * 0.1)));

			clientSocket.close();
			acceptSocket.close();
			serverSocket.close();

		} catch (final ExceptionUDT e) {
			fail(e.getMessage());
		}
	}
}
