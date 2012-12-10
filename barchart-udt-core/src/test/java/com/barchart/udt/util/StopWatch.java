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
package com.barchart.udt.util;

import java.util.concurrent.atomic.AtomicBoolean;

public class StopWatch {

	private AtomicBoolean isRunning = new AtomicBoolean(false);

	private long start;
	private long stop;

	public void clear() {
		start = 0;
		stop = 0;
		isRunning.set(false);
	}

	public boolean isRunning() {
		return isRunning.get();
	}

	public boolean isExceeded(long duration) {
		return System.nanoTime() - start > duration;
	}

	public void start() {
		// if (isRunning.compareAndSet(false, true)) {
		// start = System.nanoTime(); // start timing
		// }
		start = System.nanoTime(); // start timing
	}

	public void stop() {
		// if (isRunning.compareAndSet(true, false)) {
		// stop = System.nanoTime(); // stop timing
		// }
		stop = System.nanoTime(); // stop timing
	}

	public long nanoTime() {
		return stop - start;
	}

	public String nanoString() {

		final double SECOND = 1000000000;
		final double MILLIS = 1000000;
		final double MICROS = 1000;

		double nanoDiff = stop - start;

		int second = (int) (nanoDiff / SECOND);

		int millis = (int) ((nanoDiff - second * SECOND) / MILLIS);

		int micros = (int) ((nanoDiff - second * SECOND - millis * MILLIS) / MICROS);
		int nanos = (int) (nanoDiff - second * SECOND - millis * MILLIS - micros
				* MICROS);
		return String.format("time: %d s %d ms %d us %d ns ", second, millis,
				micros, nanos);

	}

	@Override
	public String toString() {
		return "nanoTime: " + Long.toString(nanoTime());
	}

}
