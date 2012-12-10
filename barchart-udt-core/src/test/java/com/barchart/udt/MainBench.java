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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.util.StopWatch;

public class MainBench {

	static final int COUNT = 1 * 10 * 1000;

	static final int SIZE = 1024;

	private static Logger log = LoggerFactory.getLogger(MainBench.class);

	public static void main(String[] args) {

		log.info("started");

		try {

			testJava();

			testJNI();

		} catch (Exception e) {
			log.info("unexpected", e);
		}

	}

	static void testJava() throws Exception {

		long nanos;
		StopWatch timer = new StopWatch();

		timer.start();
		for (int k = 0; k < COUNT; k++) {
			// baseline
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("baseline nanos={}", nanos);

		timer.start();
		for (int k = 0; k < COUNT; k++) {
			// small array
			byte[] array = new byte[128];
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("make small array; nanos={}", nanos);

		timer.start();
		for (int k = 0; k < COUNT; k++) {
			// medium array
			byte[] array = new byte[1024];
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("make medium array; nanos={}", nanos);

		int[] arrayInt = new int[SIZE];
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			Arrays.fill(arrayInt, 1235678);
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT / SIZE;
		log.info("fill array; nanos={}", nanos);

		//

		Integer[] array = new Integer[1024];
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			for (Integer i : array) {
				// iterate array
				Integer x = i;
			}
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT / 1024;
		log.info("iterate array; nanos={}", nanos);

		// SET
		Set<Integer> set = new HashSet<Integer>();
		for (int k = 0; k < 1024; k++) {
			set.add(k);
		}
		//
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			for (Integer i : set) {
				// iterate set
				Integer x = i;
			}
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT / 1024;
		log.info("iterate set; nanos={}", nanos);
		//
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			Object[] x = set.toArray();
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("set to array; nanos={}", nanos);

	}

	static void testJNI() throws Exception {

		SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		long nanos;
		StopWatch timer = new StopWatch();

		timer.start();
		for (int k = 0; k < COUNT; k++) {
			socket.testEmptyCall0();
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("empty call; nanos={}", nanos);

		timer.start();
		for (int k = 0; k < COUNT * 10; k++) {
			int[] array = socket.testMakeArray0(SIZE);
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT / 10;
		log.info("make arrray; nanos={}", nanos);

		int[] arrayInt = new int[SIZE];
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			socket.testGetSetArray0(arrayInt, true);
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("get/set/update array; nanos={}", nanos);
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			socket.testGetSetArray0(arrayInt, false);
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("get/set/abort array; nanos={}", nanos);

		Object[] array = new Object[SIZE];
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			socket.testIterateArray0(array);
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT / SIZE;
		log.info("iterate object array; nanos={}", nanos);

		//
		Set<Object> set = new HashSet<Object>();
		for (int k = 0; k < SIZE; k++) {
			set.add(k);
		}
		timer.start();
		for (int k = 0; k < COUNT / 10; k++) {
			socket.testIterateSet0(set);
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT / SIZE * 10;
		log.info("iterate object set; nanos={}", nanos);

		//

		final int FILL_SIZE = 16;

		byte[] fillArray = new byte[FILL_SIZE];
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			socket.testFillArray0(fillArray);
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("fillArray; nanos={}", nanos);

		ByteBuffer fillBuffer = ByteBuffer.allocateDirect(FILL_SIZE);
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			socket.testFillBuffer0(fillBuffer);
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("fillBuffer; nanos={}", nanos);

	}

}
