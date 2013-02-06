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
import java.nio.IntBuffer;

import org.junit.Test;

import util.TestAny;

import com.barchart.udt.util.HelpUDT;

public class TestDirectBuffer extends TestAny {

	@Test
	public void testByt() throws Exception {

		final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);

		assertTrue("byteBuffer.isDirect={}", byteBuffer.isDirect());

		SocketUDT.testDirectByteBufferAccess0(byteBuffer);
		for (int k = 0; k < 8; k++) {
			final byte byteValue = byteBuffer.get(k);
			log.info("k={} byteBuffer[k]={}", k, (char) byteValue);
		}

		assertEquals('A', byteBuffer.get(0));
		assertEquals('B', byteBuffer.get(1));
		assertEquals('C', byteBuffer.get(2));
		assertEquals('D', byteBuffer.get(3));
		assertEquals('E', byteBuffer.get(4));
		assertEquals('F', byteBuffer.get(5));
		assertEquals('G', byteBuffer.get(6));
		assertEquals('H', byteBuffer.get(7));

	}

	@Test
	public void testInt() throws Exception {

		final IntBuffer intBuffer = HelpUDT.newDirectIntBufer(1024);

		assertTrue("intBuffer.isDirect={}", intBuffer.isDirect());

		SocketUDT.testDirectIntBufferAccess0(intBuffer);

		for (int k = 0; k < 8; k++) {
			final int intValue = intBuffer.get(k);
			log.info("k={} intBuffer[k]={}", k, (char) intValue);
		}

		assertEquals('A', intBuffer.get(0));
		assertEquals('B', intBuffer.get(1));
		assertEquals('C', intBuffer.get(2));
		assertEquals('D', intBuffer.get(3));
		assertEquals('E', intBuffer.get(4));
		assertEquals('F', intBuffer.get(5));
		assertEquals('G', intBuffer.get(6));
		assertEquals('H', intBuffer.get(7));

	}

}
