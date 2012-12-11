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

public class TestSendRecv1 extends TestSendRecvAbstract<byte[]> {

	final static Logger log = LoggerFactory.getLogger(TestSendRecv1.class);

	final static int POSITION = 1234;
	final static int LIMIT = POSITION + SIZE;
	final static int CAPACITY = 3 * 1024;

	@Override
	protected void doClientReader() throws Exception {

		// blocks here
		byte[] arraySent = clientQueue.take();

		byte[] arrayReceived = new byte[CAPACITY];

		// blocks here
		int size = client.receive(arrayReceived, POSITION, LIMIT);
		assertEquals(size, SIZE);

		byte[] dataSent = new byte[SIZE];
		byte[] dataReceived = new byte[SIZE];

		System.arraycopy(//
				arraySent, POSITION, dataSent, 0, SIZE);
		System.arraycopy(//
				arrayReceived, POSITION, dataReceived, 0, SIZE);

		assertTrue(Arrays.equals(dataSent, dataReceived));
	}

	@Override
	protected void doClientWriter() throws Exception {

		byte[] array = new byte[CAPACITY];

		generator.nextBytes(array);

		// blocks here
		int size = client.send(array, POSITION, LIMIT);
		assertEquals(size, SIZE);

		clientQueue.put(array);

	}

	@Override
	protected void doServerReader() throws Exception {

		byte[] array = new byte[CAPACITY];

		// blocks here
		int size = connector.receive(array, POSITION, LIMIT);
		assertEquals(size, SIZE);

		serverQueue.put(array);

	}

	@Override
	protected void doServerWriter() throws Exception {

		// blocks here
		byte[] array = serverQueue.take();

		// blocks here
		int size = connector.send(array, POSITION, LIMIT);
		assertEquals(size, SIZE);

	}

}
