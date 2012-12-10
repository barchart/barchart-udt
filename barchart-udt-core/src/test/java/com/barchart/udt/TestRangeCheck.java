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
