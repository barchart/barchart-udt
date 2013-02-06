/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package bench.poll_wait;

import java.nio.IntBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.ConsoleReporterUDT;

import com.barchart.udt.EpollUDT;
import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;
import com.barchart.udt.util.HelpUDT;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

/**
 * poll in two threads
 */
public class BenchPollTwo extends SocketUDT {

	public BenchPollTwo(final TypeUDT type) throws ExceptionUDT {
		super(type);
	}

	static final Logger log = LoggerFactory.getLogger(BenchPollTwo.class);

	static final int time = 60 * 1000;

	static {

		Metrics.newCounter( //
				BenchPollTwo.class, "benchmark duration").inc(time);

	}

	static final Timer pollTimeOne = Metrics.newTimer(BenchPollTwo.class,
			"poll time one", TimeUnit.MICROSECONDS, TimeUnit.SECONDS);

	static final Timer pollTimeTwo = Metrics.newTimer(BenchPollTwo.class,
			"poll time two", TimeUnit.MICROSECONDS, TimeUnit.SECONDS);

	public static void main(final String[] args) throws Exception {

		log.info("init");

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);
		final int socketID = socket.id();
		final int epollID = BenchPollTwo.epollCreate0();

		BenchPollTwo.epollAdd0(epollID, socketID, EpollUDT.Opt.BOTH.code);

		final AtomicBoolean isOn = new AtomicBoolean(true);

		final Runnable epollTaskOne = new Runnable() {

			final IntBuffer readBuffer = HelpUDT.newDirectIntBufer(1024);
			final IntBuffer writeBuffer = HelpUDT.newDirectIntBufer(1024);
			final IntBuffer sizeBuffer = HelpUDT.newDirectIntBufer(1024);

			@Override
			public void run() {
				try {

					while (isOn.get()) {
						runCore();
					}

				} catch (final Exception e) {
					log.error("", e);
				}
			}

			void runCore() throws Exception {

				final TimerContext timer = pollTimeOne.time();

				SocketUDT.epollWait0( //
						epollID, readBuffer, writeBuffer, sizeBuffer, 0);

				timer.stop();

			}

		};

		final Runnable epollTaskTwo = new Runnable() {

			final IntBuffer readBuffer = HelpUDT.newDirectIntBufer(1024);
			final IntBuffer writeBuffer = HelpUDT.newDirectIntBufer(1024);
			final IntBuffer sizeBuffer = HelpUDT.newDirectIntBufer(1024);

			@Override
			public void run() {
				try {

					while (isOn.get()) {
						runCore();
					}

				} catch (final Exception e) {
					log.error("", e);
				}
			}

			void runCore() throws Exception {

				final TimerContext timer = pollTimeTwo.time();

				SocketUDT.epollWait0( //
						epollID, readBuffer, writeBuffer, sizeBuffer, 0);

				timer.stop();

			}

		};

		final ExecutorService executor = Executors.newFixedThreadPool(2);

		executor.submit(epollTaskOne);
		executor.submit(epollTaskTwo);

		ConsoleReporterUDT.enable(3, TimeUnit.SECONDS);

		Thread.sleep(time);

		isOn.set(false);

		Thread.sleep(1 * 1000);

		executor.shutdownNow();

		Metrics.defaultRegistry().shutdown();

		BenchPollTwo.epollRemove0(epollID, socketID);
		BenchPollTwo.epollRelease0(epollID);

		log.info("done");

	}

}
