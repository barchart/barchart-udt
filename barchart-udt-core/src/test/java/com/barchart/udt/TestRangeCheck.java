/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRangeCheck {

	Logger log = LoggerFactory.getLogger(TestRangeCheck.class);

	volatile SocketUDT socket;

	final byte[] array = new byte[1460];

	@Before
	public void setUp() throws Exception {

		log.info("started {}", System.getProperty("os.arch"));

		socket = new SocketUDT(TypeUDT.DATAGRAM);

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRangeLimitOverCapacity() {
		try {
			int position = 0;
			int limit = 2000;
			socket.send(array, position, limit);
		} catch (Throwable e) {
			if (e instanceof ExceptionUDT) {
				ExceptionUDT eUDT = (ExceptionUDT) e;
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
			int position = 110;
			int limit = -200;
			socket.send(array, position, limit);
		} catch (Throwable e) {
			if (e instanceof ExceptionUDT) {
				ExceptionUDT eUDT = (ExceptionUDT) e;
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
			int position = -10;
			int limit = 1000;
			socket.send(array, position, limit);
		} catch (Throwable e) {
			if (e instanceof ExceptionUDT) {
				ExceptionUDT eUDT = (ExceptionUDT) e;
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
			int position = 3010;
			int limit = 1000;
			socket.send(array, position, limit);
		} catch (Throwable e) {
			if (e instanceof ExceptionUDT) {
				ExceptionUDT eUDT = (ExceptionUDT) e;
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
			int position = 1400;
			int limit = 1000;
			socket.send(array, position, limit);
		} catch (Throwable e) {
			if (e instanceof ExceptionUDT) {
				ExceptionUDT eUDT = (ExceptionUDT) e;
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
