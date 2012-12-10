/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
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