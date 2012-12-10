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
