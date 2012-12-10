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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class MainFinalize {

	private static Logger log = LoggerFactory.getLogger(MainFinalize.class);

	static final String PATH = "java.library.path";

	static BlockingQueue<SocketUDT> queue = new LinkedBlockingQueue<SocketUDT>();

	static ExecutorService service = Executors.newSingleThreadExecutor();

	static Runnable task = new Runnable() {

		@Override
		public void run() {
			while (true) {
				try {
					SocketUDT socket = queue.take();
					// socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	};

	public static void main(String[] args) {

		Logger root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

		log.info("started");

		service.execute(task);

		try {

			TypeUDT type = TypeUDT.STREAM;

			long count = 0;

			while (true) {

				SocketUDT socket = new SocketUDT(type);

				queue.put(socket);

				// socket.close();
				// socket = null;

				if (count % 100000 == 0) {

					Runtime runtime = Runtime.getRuntime();

					runtime.gc();

					Thread.sleep(100);

					long totalMemory = runtime.totalMemory();
					long freeMemory = runtime.freeMemory();
					double ratio = (double) freeMemory / (double) totalMemory;

					Object[] values = new Object[] { totalMemory, freeMemory,
							count, ratio };

					log
							.info(
									"mark; totalMemory={} freeMemory={} count={} free/total={}",
									values);

				}

				count++;

			}

		} catch (Throwable e) {
			log.error("unexpected", e);
		}

	}

}