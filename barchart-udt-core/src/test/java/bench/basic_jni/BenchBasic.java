/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package bench.basic_jni;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.StopWatch;

import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;
import com.barchart.udt.util.HelpUDT;

public class BenchBasic extends SocketUDT {

	public BenchBasic(final TypeUDT type) throws ExceptionUDT {
		super(type);
	}

	static final int COUNT = 1 * 100 * 1000;

	static final int SIZE = 1024;

	private static Logger log = LoggerFactory.getLogger(BenchBasic.class);

	public static void main(final String[] args) throws Exception {

		log.info("init");

		testJava();
		testJNI();

		testJava();
		testJNI();

		testJava();
		testJNI();

		log.info("done");

	}

	static void testJava() throws Exception {

		log.info("### JAVA ###");

		long nanos;
		final StopWatch timer = new StopWatch();

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
			final byte[] array = new byte[128];
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("make array 123; nanos={}", nanos);

		timer.start();
		for (int k = 0; k < COUNT; k++) {
			// medium array
			final byte[] array = new byte[1024];
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("make array 1024; nanos={}", nanos);

		final int[] arrayInt = new int[SIZE];
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			Arrays.fill(arrayInt, 1235678);
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT / SIZE;
		log.info("fill array; nanos={}", nanos);

		//

		final Integer[] array = new Integer[1024];
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			for (final Integer i : array) {
				// iterate array
				final Integer x = i;
			}
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT / 1024;
		log.info("iterate array; nanos={}", nanos);

		// SET
		final Set<Integer> set = new HashSet<Integer>();
		for (int k = 0; k < 1024; k++) {
			set.add(k);
		}
		//
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			for (final Integer i : set) {
				// iterate set
				final Integer x = i;
			}
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT / 1024;
		log.info("iterate set; nanos={}", nanos);
		//
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			final Object[] x = set.toArray();
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("set to array; nanos={}", nanos);

	}

	static void testJNI() throws Exception {

		log.info("### JNI ###");

		long nanos;
		final StopWatch timer = new StopWatch();

		timer.start();
		for (int k = 0; k < COUNT; k++) {
			// baseline
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("baseline nanos={}", nanos);

		timer.start();
		for (int k = 0; k < COUNT; k++) {
			BenchBasic.testEmptyCall0();
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("empty call; nanos={}", nanos);

		timer.start();
		for (int k = 0; k < COUNT * 10; k++) {
			final int[] array = BenchBasic.testMakeArray0(SIZE);
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT / 10;
		log.info("make arrray 1024; nanos={}", nanos);

		final int[] arrayInt = new int[SIZE];
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			BenchBasic.testGetSetArray0(arrayInt, true);
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("get/set/update array; nanos={}", nanos);
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			BenchBasic.testGetSetArray0(arrayInt, false);
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("get/set/abort  array; nanos={}", nanos);

		final Object[] array = new Object[SIZE];
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			BenchBasic.testIterateArray0(array);
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT / SIZE;
		log.info("iterate object array; nanos={}", nanos);

		//
		final Set<Object> set = new HashSet<Object>();
		for (int k = 0; k < SIZE; k++) {
			set.add(k);
		}
		timer.start();
		for (int k = 0; k < COUNT / 10; k++) {
			BenchBasic.testIterateSet0(set);
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT / SIZE * 10;
		log.info("iterate object set; nanos={}", nanos);

		//

		final int FILL_SIZE = 1024;

		final byte[] fillArray = new byte[FILL_SIZE];
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			BenchBasic.testFillArray0(fillArray);
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("fillArray;  nanos={}", nanos);

		final ByteBuffer fillBuffer = ByteBuffer.allocateDirect(FILL_SIZE);
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			BenchBasic.testFillBuffer0(fillBuffer);
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("fillBuffer; nanos={}", nanos);

		final IntBuffer loadBuffer = HelpUDT.newDirectIntBufer(1024);
		timer.start();
		for (int k = 0; k < COUNT; k++) {
			BenchBasic.testDirectIntBufferLoad0(loadBuffer);
		}
		timer.stop();
		nanos = timer.nanoTime() / COUNT;
		log.info("loadBuffer; nanos={}", nanos);

	}

}
