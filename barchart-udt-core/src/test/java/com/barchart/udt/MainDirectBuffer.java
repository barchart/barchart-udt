/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class MainDirectBuffer {

	private static Logger log = LoggerFactory.getLogger(MainDirectBuffer.class);

	public static void main(String[] args) {

		log.info("started; trying access direct buffer");

		try {

			TypeUDT type = TypeUDT.DATAGRAM;

			SocketUDT socket = new SocketUDT(type);

			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
			log.info("byteBuffer.isDirect={}", byteBuffer.isDirect());

			socket.testDirectByteBufferAccess0(byteBuffer);
			for (int k = 0; k < 3; k++) {
				byte byteValue = byteBuffer.get(k);
				log.info("k={} byteBuffer[k]={}", k, (char) byteValue);
			}

			//

			IntBuffer intBuffer = ByteBuffer.allocateDirect(1024 * 4).order(
					ByteOrder.nativeOrder()).asIntBuffer();

			log.info("intBuffer.isDirect={}", intBuffer.isDirect());

			for (int k = 0; k < 3; k++) {
				intBuffer.put(k, 0);
			}

			socket.testDirectIntBufferAccess0(intBuffer);
			for (int k = 0; k < 3; k++) {
				int intValue = intBuffer.get(k);
				log.info("k={} intBuffer[k]={}", k, (char) intValue);
			}

		} catch (Throwable e) {
			log.error("unexpected", e);
		}

	}
}