/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package bench.transfer;

import static util.UnitHelp.*;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.MonitorUDT;
import com.barchart.udt.OptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class MainRunServer {

	private static Logger log = LoggerFactory.getLogger(MainRunServer.class);

	public static void main(final String[] args) {

		log.info("started SERVER");

		// specify server listening interface
		final String bindAddress = property("udt.bind.address");

		// specify server listening port
		final int localPort = Integer.parseInt(property("udt.local.port"));

		// specify how many packets must come before stats logging
		final int countMonitor = Integer
				.parseInt(property("udt.count.monitor"));

		try {

			final SocketUDT acceptor = new SocketUDT(TypeUDT.DATAGRAM);
			log.info("init; acceptor={}", acceptor.id());

			InetSocketAddress localSocketAddress = new InetSocketAddress(
					bindAddress, localPort);

			acceptor.bind(localSocketAddress);
			localSocketAddress = acceptor.getLocalSocketAddress();
			log.info("bind; localSocketAddress={}", localSocketAddress);

			acceptor.listen(10);
			log.info("listen;");

			final SocketUDT receiver = acceptor.accept();

			log.info("accept; receiver={}", receiver.id());

			assert receiver.id() != acceptor.id();

			final long timeStart = System.currentTimeMillis();

			//

			final InetSocketAddress remoteSocketAddress = receiver
					.getRemoteSocketAddress();

			log.info("receiver; remoteSocketAddress={}", remoteSocketAddress);

			StringBuilder text = new StringBuilder(1024);
			OptionUDT.appendSnapshot(receiver, text);
			text.append("\t\n");
			log.info("receiver options; {}", text);

			final MonitorUDT monitor = receiver.monitor();

			while (true) {

				final byte[] array = new byte[SIZE];

				final int result = receiver.receive(array);

				assert result == SIZE : "wrong size";

				getSequenceNumber(array);

				if (sequenceNumber % countMonitor == 0) {

					receiver.updateMonitor(false);
					text = new StringBuilder(1024);
					monitor.appendSnapshot(text);
					log.info("stats; {}", text);

					final long timeFinish = System.currentTimeMillis();
					final long timeDiff = 1 + (timeFinish - timeStart) / 1000;

					final long byteCount = sequenceNumber * SIZE;
					final long rate = byteCount / timeDiff;

					log.info("receive rate, bytes/second: {}", rate);

				}

			}

			// log.info("result={}", result);

		} catch (final Throwable e) {
			log.error("unexpected", e);
		}

	}

	static long sequenceNumber = 0;

	static void getSequenceNumber(final byte[] array) {

		final ByteBuffer buffer = ByteBuffer.wrap(array);

		final long currentNumber = buffer.getLong();

		if (currentNumber == sequenceNumber) {
			sequenceNumber++;
		} else {
			log.error("sequence error; currentNumber={} sequenceNumber={}",//
					currentNumber, sequenceNumber);
			System.exit(1);
		}

	}

	final static AtomicLong sequencNumber = new AtomicLong(0);

	private static final int SIZE = 1460;

}
