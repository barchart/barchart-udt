/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import static org.junit.Assert.*;

import java.util.Arrays;

public class TestSendRecv0 extends TestSendRecvAbstract<byte[]> {

	@Override
	protected void doClientReader() throws Exception {

		// blocks here
		final byte[] arraySent = clientQueue.take();

		final byte[] arrayReceived = new byte[SIZE];

		// blocks here
		final int size = client.receive(arrayReceived);
		assertEquals(size, SIZE);

		assertTrue(Arrays.equals(arraySent, arrayReceived));

	}

	@Override
	protected void doClientWriter() throws Exception {

		final byte[] array = new byte[SIZE];

		generator.nextBytes(array);

		// blocks here
		final int size = client.send(array);
		assertEquals(size, SIZE);

		clientQueue.put(array);

	}

	@Override
	protected void doServerReader() throws Exception {

		final byte[] array = new byte[SIZE];

		// blocks here
		final int size = connector.receive(array);
		assertEquals(size, SIZE);

		serverQueue.put(array);

	}

	@Override
	protected void doServerWriter() throws Exception {

		// blocks here
		final byte[] array = serverQueue.take();

		// blocks here
		final int size = connector.send(array);
		assertEquals(size, SIZE);

	}

}
