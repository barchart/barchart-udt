/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSendRecv0 extends TestSendRecvAbstract<byte[]> {

	final static Logger log = LoggerFactory.getLogger(TestSendRecv0.class);

	@Override
	protected void doClientReader() throws Exception {

		// blocks here
		byte[] arraySent = clientQueue.take();

		byte[] arrayReceived = new byte[SIZE];

		// blocks here
		int size = client.receive(arrayReceived);
		assertEquals(size, SIZE);

		assertTrue(Arrays.equals(arraySent, arrayReceived));

	}

	@Override
	protected void doClientWriter() throws Exception {

		byte[] array = new byte[SIZE];

		generator.nextBytes(array);

		// blocks here
		int size = client.send(array);
		assertEquals(size, SIZE);

		clientQueue.put(array);

	}

	@Override
	protected void doServerReader() throws Exception {

		byte[] array = new byte[SIZE];

		// blocks here
		int size = connector.receive(array);
		assertEquals(size, SIZE);

		serverQueue.put(array);

	}

	@Override
	protected void doServerWriter() throws Exception {

		// blocks here
		byte[] array = serverQueue.take();

		// blocks here
		int size = connector.send(array);
		assertEquals(size, SIZE);

	}

}
