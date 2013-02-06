/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.ccc;

import static org.junit.Assert.*;
import static util.UnitHelp.*;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.TestAny;

import com.barchart.udt.FactoryUDT;
import com.barchart.udt.MonitorUDT;
import com.barchart.udt.OptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class TestUDPBlast extends TestAny {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * This test uses the {@link OptionUDT#UDT_CC} option using the
	 * {@link UDPBlast} custom congestion control algorithm limiting bandwidth
	 * to 50Mbs between localhost
	 */
	@Test
	public void test() throws Exception {

		// UDT has roughly 7% congestion control overhead
		// so we need to take that into account if you want
		// to limit wire speed
		final double maxBW = 50 * 0.93;
		log.info("Attempting to rate limit using custom CCC UDPBlast class");

		final SocketUDT serverSocket = new SocketUDT(TypeUDT.STREAM);
		final InetSocketAddress serverAddress = localSocketAddress();
		serverSocket.bind(serverAddress);
		serverSocket.listen(1);
		assertTrue(serverSocket.isBound());

		final SocketUDT clientSocket = new SocketUDT(TypeUDT.STREAM);

		clientSocket.setOption(OptionUDT.UDT_CC, new FactoryUDT<UDPBlast>(
				UDPBlast.class));

		final InetSocketAddress clientAddress = localSocketAddress();

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

				final MonitorUDT mon = clientSocket.monitor();

				log.info(String.format("Current Rate: %.2f Mbs",
						mon.mbpsSendRate()));

				maxMbs = mon.mbpsSendRate() * 0.93;

			}

		}

		// check we are within 10% of our desired rate
		assertTrue("Max bandwidth exceeded limit",
				maxMbs < (maxBW + (maxBW * 0.1)));

		clientSocket.close();
		acceptSocket.close();
		serverSocket.close();

	}

}
