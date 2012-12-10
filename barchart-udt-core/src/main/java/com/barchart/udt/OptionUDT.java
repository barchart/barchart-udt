/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4';VERSION='1.0.2-SNAPSHOT';TIMESTAMP='2011-01-11_09-30-59';
 *
 * Copyright (C) 2009-2011, Barchart, Inc. (http://www.barchart.com/)
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

import static com.barchart.udt.OptionFormatUDT.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Enum OptionUDT.
 */
public enum OptionUDT {

	// keep code values in sync with UDT::UDTOpt
	// provide 2 names: 1) UDT original and 2) human-readble

	/** the Maximum Transfer Unit. */
	UDT_MSS(0, Integer.class, DECIMAL), //
	/** the Maximum Transfer Unit. */
	Maximum_Transfer_Unit(0, Integer.class, DECIMAL), //

	/** if sending is blocking. */
	UDT_SNDSYN(1, Boolean.class, BOOLEAN), //
	/** if sending is blocking. */
	Is_Send_Synchronous(1, Boolean.class, BOOLEAN), //

	/** if receiving is blocking. */
	UDT_RCVSYN(2, Boolean.class, BOOLEAN), //
	/** if receiving is blocking. */
	Is_Receive_Synchronous(2, Boolean.class, BOOLEAN), //

	// custom congestion control algorithm
	/* TODO not yet implemented: FactoryUDT */
	// UDT_CC(3, FactoryUDT.class), //

	/** Flight flag size (window size). */
	UDT_FC(4, Integer.class, BINARY), //
	/** Flight flag size (window size). */
	Flight_Window_Size(4, Integer.class, DECIMAL), //

	/** maximum buffer in sending queue. */
	UDT_SNDBUF(5, Integer.class, DECIMAL), //
	/** maximum buffer in sending queue. */
	Protocol_Send_Buffer_Size(5, Integer.class, DECIMAL), //

	/** UDT receiving buffer size. */
	UDT_RCVBUF(6, Integer.class, DECIMAL), //
	/** UDT receiving buffer size. */
	Protocol_Receive_Buffer_Size(6, Integer.class, DECIMAL), //

	/** waiting for unsent data when closing. */
	UDT_LINGER(7, LingerUDT.class, DECIMAL), //
	/** waiting for unsent data when closing. */
	Time_To_Linger_On_Close(7, LingerUDT.class, DECIMAL), //

	/** UDP sending buffer size. */
	UDP_SNDBUF(8, Integer.class, DECIMAL), //
	/** UDP sending buffer size. */
	Kernel_Send_Buffer_Size(8, Integer.class, DECIMAL), //

	/** UDP receiving buffer size. */
	UDP_RCVBUF(9, Integer.class, DECIMAL), //
	/** UDP receiving buffer size. */
	Kernel_Receive_Buffer_Size(9, Integer.class, DECIMAL), //

	// XXX not exposed in UDT api
	// maximum datagram message size
	/* TODO UDT Error; code:'5000' message:'Operation not supported.' */
	// UDT_MAXMSG(10, Integer.class), //

	// XXX not exposed in UDT api
	// time-to-live of a datagram message
	/* TODO UDT Error; code:'5000' message:'Operation not supported.' */
	// UDT_MSGTTL(11, Integer.class), //

	/** rendezvous connection mode. */
	UDT_RENDEZVOUS(12, Boolean.class, BOOLEAN), //
	/** rendezvous connection mode. */
	Is_Randezvous_Connect_Enabled(12, Boolean.class, BOOLEAN), //

	/** send() timeout. */
	UDT_SNDTIMEO(13, Integer.class, DECIMAL), //
	/** send() timeout. */
	Send_Timeout(13, Integer.class, DECIMAL), //

	/** recv() timeout. */
	UDT_RCVTIMEO(14, Integer.class, DECIMAL), //
	/** recv() timeout. */
	Receive_Timeout(14, Integer.class, DECIMAL), //

	/** reuse an existing port or create a new one. */
	UDT_REUSEADDR(15, Boolean.class, BOOLEAN), //
	/** reuse an existing port or create a new one. */
	Is_Address_Reuse_Enabled(15, Boolean.class, BOOLEAN), //

	/** maximum bandwidth (bytes per second) that the connection can use. */
	UDT_MAXBW(16, Long.class, DECIMAL), //
	/** maximum bandwidth (bytes per second) that the connection can use. */
	Maximum_Bandwidth(16, Long.class, DECIMAL), //

	/** The code. */
	;

	private final int code;

	public int getCode() {
		return code;
	}

	/** The klaz. */
	private final Class<?> klaz;

	public Class<?> getKlaz() {
		return klaz;
	}

	/** The format. */
	private final OptionFormatUDT format;

	public OptionFormatUDT getFormat() {
		return format;
	}

	/**
	 * Instantiates a new option udt.
	 * 
	 * @param code
	 *            the code
	 * @param klaz
	 *            the klaz
	 * @param format
	 *            the format
	 */
	private OptionUDT(final int code, final Class<?> klaz,
			final OptionFormatUDT format) {
		this.code = code;
		this.klaz = klaz;
		this.format = format;
	}

	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(OptionUDT.class);

	/**
	 * show all option values.
	 * 
	 * @param socketUDT
	 *            the socket
	 * @param text
	 *            the text
	 */
	public static void appendSnapshot(final SocketUDT socketUDT,
			final StringBuilder text) {

		text.append("\n\t");
		text.append("socketID");
		text.append(" = ");
		text.append(socketUDT.socketID);

		for (OptionUDT option : values()) {
			int optionCode = 0;
			String optionName = null;
			String optionValue = null;
			try {

				optionCode = option.code;
				optionName = option.name();
				optionValue = option.format.convert(//
						socketUDT.getOption(option));

				// TODO fix this hack
				if (optionName.startsWith("UD")) {
					continue;
				}

				text.append("\n\t");
				text.append(optionCode);
				text.append(") ");
				text.append(optionName);
				text.append(" = ");
				text.append(optionValue);

			} catch (Exception e) {
				log.error("unexpected; " + optionName, e);
			}

		}

	}

}
