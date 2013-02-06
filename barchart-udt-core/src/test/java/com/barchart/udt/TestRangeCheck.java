/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.TestAny;

public class TestRangeCheck extends TestAny {

	private final byte[] array = new byte[1460];

	private volatile SocketUDT socket;

	@Before
	public void setUp() throws Exception {

		socket = new SocketUDT(TypeUDT.DATAGRAM);

	}

	@After
	public void tearDown() throws Exception {

		socket.close();

	}

	@Test
	public void testRangeLimitOverCapacity() {
		try {
			final int position = 0;
			final int limit = 2000;
			socket.send(array, position, limit);
		} catch (final Throwable e) {
			if (e instanceof ExceptionUDT) {
				final ExceptionUDT eUDT = (ExceptionUDT) e;
				switch (eUDT.getError()) {
				case WRAPPER_MESSAGE:
					log.info("message={}", eUDT.getMessage());
					return;
				default:
					break;
				}
			}
		}
		fail("did not detect limit > capacity");
	}

	@Test
	public void testRangeLimitUnderZero() {
		try {
			final int position = 110;
			final int limit = -200;
			socket.send(array, position, limit);
		} catch (final Throwable e) {
			if (e instanceof ExceptionUDT) {
				final ExceptionUDT eUDT = (ExceptionUDT) e;
				switch (eUDT.getError()) {
				case WRAPPER_MESSAGE:
					log.info("message={}", eUDT.getMessage());
					return;
				default:
					break;
				}
			}
		}
		fail("did not detect limit < 0");
	}

	@Test
	public void testRangePositionUnderZero() {
		try {
			final int position = -10;
			final int limit = 1000;
			socket.send(array, position, limit);
		} catch (final Throwable e) {
			if (e instanceof ExceptionUDT) {
				final ExceptionUDT eUDT = (ExceptionUDT) e;
				switch (eUDT.getError()) {
				case WRAPPER_MESSAGE:
					log.info("message={}", eUDT.getMessage());
					return;
				default:
					break;
				}
			}
		}
		fail("did not detect position < 0");
	}

	@Test
	public void testRangePositionOverCapacity() {
		try {
			final int position = 3010;
			final int limit = 1000;
			socket.send(array, position, limit);
		} catch (final Throwable e) {
			if (e instanceof ExceptionUDT) {
				final ExceptionUDT eUDT = (ExceptionUDT) e;
				switch (eUDT.getError()) {
				case WRAPPER_MESSAGE:
					log.info("message={}", eUDT.getMessage());
					return;
				default:
					break;
				}
			}
		}
		fail("did not detect position > capacity");
	}

	@Test
	public void testRangePositionOverLimit() {
		try {
			final int position = 1400;
			final int limit = 1000;
			socket.send(array, position, limit);
		} catch (final Throwable e) {
			if (e instanceof ExceptionUDT) {
				final ExceptionUDT eUDT = (ExceptionUDT) e;
				switch (eUDT.getError()) {
				case WRAPPER_MESSAGE:
					log.info("message={}", eUDT.getMessage());
					return;
				default:
					break;
				}
			}
		}
		fail("did not detect position > limit");
	}

}
