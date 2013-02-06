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

public class MainRunClient {

	private static Logger log = LoggerFactory.getLogger(MainRunClient.class);

	public static void main(final String[] args) {

		log.info("started CLIENT");

		// specify client sender interface
		final String bindAddress = property("udt.bind.address");

		// specify server listening address
		final String remoteAddress = property("udt.remote.address");

		// specify server listening port
		final int remotePort = Integer.parseInt(property("udt.remote.port"));

		// specify server bandwidth limit
		final long maxBandwidth = Integer
				.parseInt(property("udt.max.bandwidth"));

		// specify number of packets sent in a batch
		final int countBatch = Integer.parseInt(property("udt.count.batch"));

		// specify number of millis to sleep between batches of packets
		final int countSleep = Integer.parseInt(property("udt.count.sleep"));

		// specify number of packet batches between stats logging
		final int countMonitor = Integer
				.parseInt(property("udt.count.monitor"));

		try {

			final SocketUDT sender = new SocketUDT(TypeUDT.DATAGRAM);

			// specify maximum upload speed, bytes/sec
			sender.setOption(OptionUDT.UDT_MAXBW, maxBandwidth);

			InetSocketAddress localSocketAddress = new InetSocketAddress( //
					bindAddress, 0);

			log.info("localSocketAddress : {}", localSocketAddress);

			sender.bind(localSocketAddress);
			localSocketAddress = sender.getLocalSocketAddress();
			log.info("bind; localSocketAddress={}", localSocketAddress);

			InetSocketAddress remoteSocketAddress = new InetSocketAddress(//
					remoteAddress, remotePort);

			sender.connect(remoteSocketAddress);
			remoteSocketAddress = sender.getRemoteSocketAddress();
			log.info("connect; remoteSocketAddress={}", remoteSocketAddress);

			StringBuilder text = new StringBuilder(1024);
			OptionUDT.appendSnapshot(sender, text);
			text.append("\t\n");
			log.info("sender options; {}", text);

			long count = 0;

			final MonitorUDT monitor = sender.monitor();

			while (true) {

				for (int k = 0; k < countBatch; k++) {

					final byte[] array = new byte[SIZE];

					putSequenceNumber(array);

					final int result = sender.send(array);

					assert result == SIZE : "wrong size";

				}

				// sleep between batches
				Thread.sleep(countSleep);

				count++;

				if (count % countMonitor == 0) {
					sender.updateMonitor(false);
					text = new StringBuilder(1024);
					monitor.appendSnapshot(text);
					log.info("stats; {}", text);
				}

			}

			// log.info("result={}", result);

		} catch (final Throwable e) {
			log.error("unexpected", e);
		}

	}

	private static final int SIZE = 1460;

	final static AtomicLong sequencNumber = new AtomicLong(0);

	static void putSequenceNumber(final byte[] array) {

		final ByteBuffer buffer = ByteBuffer.wrap(array);

		buffer.putLong(sequencNumber.getAndIncrement());

	}

}
