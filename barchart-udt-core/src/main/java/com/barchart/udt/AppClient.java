/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.barchart.udt.net.NetSocketUDT;

public class AppClient {

	static boolean finished = false;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) {

		String host;
		int port = 9000;
		final int size = 10000;
		final byte[] data = new byte[size];
		Future<Boolean> monResult = null;

		if (args.length != 2) {
			System.out.println("usage: appclient server_host server_port");
			return;
		}

		host = args[0];
		port = Integer.parseInt(args[1]);

		try {

			final NetSocketUDT socket = new NetSocketUDT();

			if (System.getProperty("os.name").contains("win"))
				socket.socketUDT().setOption(OptionUDT.UDT_MSS, 1052);

			socket.connect(new InetSocketAddress(host, port));
			final OutputStream os = socket.getOutputStream();

			// Start the monitor background task
			monResult = Executors.newSingleThreadExecutor().submit(
					new Callable<Boolean>() {
						@Override
						public Boolean call() {
							return monitor(socket.socketUDT());
						}
					});

			for (int i = 0; i < 1000000; i++) {
				os.write(data);
			}

			finished = true;
			if (monResult != null)
				monResult.get();

		} catch (final IOException ioe) {
			ioe.printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		}

	}

	public static boolean monitor(final SocketUDT socket) {

		System.out
				.println("SendRate(Mb/s)\tRTT(ms)\tCWnd\tPktSndPeriod(us)\tRecvACK\tRecvNAK");

		try {

			while (!finished) {

				Thread.sleep(1000);

				socket.updateMonitor(false);

				System.out.printf("%.2f\t\t" + "%.2f\t" + "%d\t" + "%.2f\t\t\t"
						+ "%d\t" + "%d\n", socket.monitor().mbpsSendRate,
						socket.monitor().msRTT,
						socket.monitor().pktCongestionWindow,
						socket.monitor().usPktSndPeriod,
						socket.monitor().pktRecvACK,
						socket.monitor().pktRecvNAK);
			}

			return true;

		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
