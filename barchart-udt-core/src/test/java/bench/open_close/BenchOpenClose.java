/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package bench.open_close;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import util.UnitHelp;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.StatusUDT;
import com.barchart.udt.TypeUDT;

public class BenchOpenClose extends SocketUDT {

	public BenchOpenClose(final TypeUDT type) throws ExceptionUDT {
		super(type);
	}

	static final int SIZE = 10 * 1024;

	static final BlockingQueue<SocketUDT> //
	queue = new LinkedBlockingQueue<SocketUDT>(SIZE);

	static final ExecutorService service = Executors.newSingleThreadExecutor();

	static final Runnable task = new Runnable() {

		@Override
		public void run() {
			while (true) {
				try {

					final SocketUDT socket = queue.take();

					socket.close();

					UnitHelp.socketAwait(socket, StatusUDT.CLOSED);

				} catch (final Exception e) {
					log.error("", e);
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
			socket.bind(UnitHelp.localSocketAddress());
			UnitHelp.socketAwait(socket, StatusUDT.OPENED);

			Thread.sleep(20);

			queue.put(socket);

			final Runtime runtime = Runtime.getRuntime();

			if (count % 1000 == 0) {

				runtime.gc();

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
