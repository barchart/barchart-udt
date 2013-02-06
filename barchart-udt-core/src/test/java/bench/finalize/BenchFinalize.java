/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package bench.finalize;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class BenchFinalize extends SocketUDT {

	public BenchFinalize(final TypeUDT type) throws ExceptionUDT {
		super(type);
	}

	static final BlockingQueue<SocketUDT> queue = new LinkedBlockingQueue<SocketUDT>();

	static final ExecutorService service = Executors.newSingleThreadExecutor();

	static final Runnable task = new Runnable() {

		@Override
		public void run() {
			while (true) {
				try {
					final SocketUDT socket = queue.take();
					// socket.close();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}

	};

	public static void main(final String[] args) throws Exception {

		final Logger logBack = (Logger) log;
		logBack.setLevel(Level.INFO);

		log.info("started");

		service.execute(task);

		long count = 0;

		while (true) {

			final SocketUDT socket = new SocketUDT(TypeUDT.STREAM);

			queue.put(socket);

			// socket.close();
			// socket = null;

			if (count % 100000 == 0) {

				final Runtime runtime = Runtime.getRuntime();

				runtime.gc();

				Thread.sleep(100);

				final long totalMemory = runtime.totalMemory();
				final long freeMemory = runtime.freeMemory();
				final double ratio = (double) freeMemory / (double) totalMemory;

				log.info("total={} free={} count={} free/total={}",
						totalMemory, freeMemory, count, ratio);

			}

			count++;

		}

	}

}
