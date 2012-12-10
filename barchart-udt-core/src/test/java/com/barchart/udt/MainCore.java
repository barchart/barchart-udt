/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.OptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class MainCore {

	private static Logger log = LoggerFactory.getLogger(MainCore.class);

	// static {
	// try {
	// DatagramSocket socket = new DatagramSocket(null);
	// SocketAddress addr = new InetSocketAddress(0);
	// socket.bind(addr);
	// assert socket.isBound();
	// System.out.println("isBound=" + socket.isBound());
	// } catch (SocketException e) {
	// e.printStackTrace();
	// }
	// }

	static final String PATH = "java.library.path";

	public static void main(String[] args) {

		log.info("started");

		System.out.println(PATH + "=" + System.getProperty(PATH));

		try {

			TypeUDT type = TypeUDT.STREAM;

			SocketUDT socket = new SocketUDT(type);

			// socket.test();

			StringBuilder text = new StringBuilder(1024);

			OptionUDT.appendSnapshot(socket, text);
			text.append("\t\n");

			socket.setOption(OptionUDT.UDT_MSS, 1234);
			socket.setOption(OptionUDT.UDT_SNDSYN, false);
			socket.setOption(OptionUDT.UDT_RCVSYN, false);
			socket.setOption(OptionUDT.UDP_RCVBUF, 12345678);
			socket.setOption(OptionUDT.UDP_SNDBUF, 23456789);
			socket.setOption(OptionUDT.UDT_MAXBW, 777777777L);

			OptionUDT.appendSnapshot(socket, text);
			text.append("\t\n");

			log.info("options; {}", text);

			InetSocketAddress localSocketAddress;
			InetSocketAddress remoteSocketAddress;

			localSocketAddress = new InetSocketAddress(0);

			socket.bind(localSocketAddress);

			int code = socket.getErrorCode();
			String message = socket.getErrorMessage();
			socket.clearError();
			log.info("code={} message={}", code, message);

			localSocketAddress = socket.getLocalSocketAddress();
			log.info("localSocketAddress={}", localSocketAddress);

			remoteSocketAddress = socket.getRemoteSocketAddress();
			log.info("remoteSocketAddress={}", remoteSocketAddress);

			//

			log.info("option test");

			// Object option;
			// option = socket.getOption(OptionUDT.UDT_SNDSYN);
			// log.info("option={}", option);
			// option = socket.getOption(OptionUDT.UDP_RCVBUF);
			// log.info("option={}", option);
			// option = socket.getOption(OptionUDT.UDT_MAXBW);
			// log.info("option={}", option);
			// option = socket.getOption(OptionUDT.UDT_CC);

		} catch (Throwable e) {
			log.error("unexpected", e);
		}

	}

	static void temp() throws Exception {

		InetAddress address;

		TypeUDT type = TypeUDT.STREAM;

		SocketUDT socket = new SocketUDT(type);

		SocketUDT client = socket.accept();

		assert client != null;

		System.out.println(client);

		InetSocketAddress localSocketAddress = new InetSocketAddress(0);

		socket.bind(localSocketAddress);

		InetSocketAddress remoteSocketAddress = new InetSocketAddress(0);

		socket.connect(remoteSocketAddress);

		localSocketAddress = socket.getLocalSocketAddress();

		remoteSocketAddress = socket.getRemoteSocketAddress();

		// socket.setOption(OptionUDT.UDT_LINGER, true);

		// Object value = socket.getOption(OptionUDT.UDT_LINGER);

		socket.listen(100);

		byte[] array = null;

		int count = 0;

		count = socket.receive(array);

		// count = socket.receiveMessage(array);

		socket.send(array);

		// socket.sendMessage(array, 0, false);

	}

}
