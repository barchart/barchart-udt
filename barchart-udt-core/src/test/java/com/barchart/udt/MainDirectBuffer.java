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