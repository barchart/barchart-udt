/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package bench.exception;

import java.util.concurrent.TimeUnit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.reporting.ConsoleReporter;

public class BenchSocket extends SocketUDT {

	public BenchSocket(final TypeUDT type) throws ExceptionUDT {
		super(type);
	}

	static final Meter construct = Metrics.newMeter(BenchSocket.class, "init",
			"constructions", TimeUnit.SECONDS);

	static final int COUNT = 100 * 1000 * 1000;

	public static void main(final String... args) throws Exception {

		final Logger logBack = (Logger) log;

		logBack.setLevel(Level.INFO);

		log.info("init");

		ConsoleReporter.enable(3, TimeUnit.SECONDS);

		for (int k = 0; k < COUNT; k++) {

			construct.mark();

			final BenchSocket socket = new BenchSocket(TypeUDT.DATAGRAM);

			socket.close();

		}

		log.info("done");

	}

}
