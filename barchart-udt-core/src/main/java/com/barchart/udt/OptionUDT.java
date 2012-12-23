/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import static com.barchart.udt.OptionUDT.Format.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.anno.Native;

/**
 * The Enum OptionUDT.
 * <p>
 * provide 2 names: 1) UDT original and 2) human-readble
 * <p>
 * keep code values in sync with udt.h - UDT::UDTOpt; enum starts with index 0
 * 
 * @see <a href="http://udt.sourceforge.net/udt4/doc/opt.htm">udt options</a>
 *      <pre>
 * UDT_MSS, // the Maximum Transfer Unit
 * UDT_SNDSYN, // if sending is blocking
 * UDT_RCVSYN, // if receiving is blocking
 * UDT_CC, // custom congestion control algorithm
 * UDT_FC, // Flight flag size (window size)
 * UDT_SNDBUF, // maximum buffer in sending queue
 * UDT_RCVBUF, // UDT receiving buffer size
 * UDT_LINGER, // waiting for unsent data when closing
 * UDP_SNDBUF, // UDP sending buffer size
 * UDP_RCVBUF, // UDP receiving buffer size
 * UDT_MAXMSG, // maximum datagram message size
 * UDT_MSGTTL, // time-to-live of a datagram message
 * UDT_RENDEZVOUS, // rendezvous connection mode
 * UDT_SNDTIMEO, // send() timeout
 * UDT_RCVTIMEO, // recv() timeout
 * UDT_REUSEADDR, // reuse an existing port or create a new one
 * UDT_MAXBW, // maximum bandwidth (bytes per second) that the connection can  use
 * UDT_STATE, // current socket state, see UDTSTATUS, read only
 * UDT_EVENT, // current avalable events associated with the socket
 * UDT_SNDDATA, // size of data in the sending buffer
 * UDT_RCVDATA // size of data available for recv
 * </pre>
 */
public enum OptionUDT {

	/** the Maximum Transfer Unit. */
	UDT_MSS(0, Integer.class, DECIMAL), //
	/** the Maximum Transfer Unit., bytes */
	Maximum_Transfer_Unit(0, Integer.class, DECIMAL), //

	/** if sending is blocking. */
	UDT_SNDSYN(1, Boolean.class, BOOLEAN), //
	/** if sending is blocking., true/false */
	Is_Send_Synchronous(1, Boolean.class, BOOLEAN), //

	/** if receiving is blocking. */
	UDT_RCVSYN(2, Boolean.class, BOOLEAN), //
	/** if receiving is blocking, true/false */
	Is_Receive_Synchronous(2, Boolean.class, BOOLEAN), //

	/** custom congestion control algorithm */
	UDT_CC(3, FactoryUDT.class, DEFAULT), //
	/** custom congestion control algorithm, class factory */
	Custom_Congestion_Control(3, FactoryUDT.class, DEFAULT), //

	/** Flight flag size (window size). */
	UDT_FC(4, Integer.class, BINARY), //
	/** Flight flag size (window size), bytes */
	Flight_Window_Size(4, Integer.class, BINARY), //

	/** maximum buffer in sending queue. */
	UDT_SNDBUF(5, Integer.class, DECIMAL), //
	/** maximum buffer in sending queue. */
	Protocol_Send_Buffer_Size(5, Integer.class, DECIMAL), //

	/** UDT receiving buffer size. */
	UDT_RCVBUF(6, Integer.class, DECIMAL), //
	/** UDT receiving buffer size limit, bytes */
	Protocol_Receive_Buffer_Size(6, Integer.class, DECIMAL), //

	/** waiting for unsent data when closing. */
	UDT_LINGER(7, LingerUDT.class, DECIMAL), //
	/** waiting for unsent data when closing. true/false and timeout, seconds */
	Time_To_Linger_On_Close(7, LingerUDT.class, DECIMAL), //

	/** UDP sending buffer size. */
	UDP_SNDBUF(8, Integer.class, DECIMAL), //
	/** UDP sending buffer size limit, bytes */
	Kernel_Send_Buffer_Size(8, Integer.class, DECIMAL), //

	/** UDP receiving buffer size. */
	UDP_RCVBUF(9, Integer.class, DECIMAL), //
	/** UDP receiving buffer size limit, bytes */
	Kernel_Receive_Buffer_Size(9, Integer.class, DECIMAL), //

	/* maximum datagram message size */
	// UDT_MAXMSG(10, Integer.class, DECIMAL), // no support in udt core

	/* time-to-live of a datagram message */
	// UDT_MSGTTL(11, Integer.class, DECIMAL), // no support in udt core

	/** rendezvous connection mode. */
	UDT_RENDEZVOUS(12, Boolean.class, BOOLEAN), //
	/** rendezvous connection mode, enabled/disabled */
	Is_Randezvous_Connect_Enabled(12, Boolean.class, BOOLEAN), //

	/** send() timeout. */
	UDT_SNDTIMEO(13, Integer.class, DECIMAL), //
	/** send() timeout. milliseconds */
	Send_Timeout(13, Integer.class, DECIMAL), //

	/** recv() timeout. */
	UDT_RCVTIMEO(14, Integer.class, DECIMAL), //
	/** recv() timeout. milliseconds */
	Receive_Timeout(14, Integer.class, DECIMAL), //

	/** reuse an existing port or create a new one. */
	UDT_REUSEADDR(15, Boolean.class, BOOLEAN), //
	/** reuse an existing port or create a new one. true/false */
	Is_Address_Reuse_Enabled(15, Boolean.class, BOOLEAN), //

	/** maximum bandwidth (bytes per second) that the connection can use. */
	UDT_MAXBW(16, Long.class, DECIMAL), //
	/** maximum bandwidth (bytes per second) that the connection can use. */
	Maximum_Bandwidth(16, Long.class, DECIMAL), //

	/** current socket state, see UDTSTATUS, read only */
	UDT_STATE(17, Integer.class, DECIMAL), //
	/** current socket status code, see {@link StatusUDT#getCode()}, read only */
	Status_Code(17, Integer.class, DECIMAL), //

	/** current available events associated with the socket */
	UDT_EVENT(18, Integer.class, DECIMAL), //
	/** current available epoll events, see {@link EpollUDT.Opt#code} */
	Epoll_Event_Mask(18, Integer.class, DECIMAL), //

	/** size of data in the sending buffer */
	UDT_SNDDATA(19, Integer.class, DECIMAL), //
	/** current consumed sending buffer utilization, read only, bytes */
	Send_Buffer_Consumed(19, Integer.class, DECIMAL), //

	/** size of data available for recv */
	UDT_RCVDATA(20, Integer.class, DECIMAL), //
	/** current available receiving buffer capacity, read only, bytes */
	Receive_Buffer_Available(20, Integer.class, DECIMAL), //

	;

	/**
	 * render options in human format
	 */
	public static enum Format {

		DECIMAL() {
			@Override
			public String convert(final Object value) {
				if (value instanceof Number) {
					final long number = ((Number) value).longValue();
					return String.format("%,d", number);
				}
				return "invalid value";
			}
		}, //

		BINARY() {
			@Override
			public String convert(final Object value) {
				if (value instanceof Number) {
					final long number = ((Number) value).longValue();
					return String.format("%,d (%,d K)", number, number / 1024);
				}
				return "invalid value";
			}
		}, //

		BOOLEAN() {
			@Override
			public String convert(final Object value) {
				if (value instanceof Boolean) {
					final boolean bool = ((Boolean) value).booleanValue();
					return String.format("%b", bool);
				}
				return "invalid value";
			}
		}, //

		DEFAULT() {
			@Override
			public String convert(final Object value) {
				return "" + value;
			}
		}, //

		;

		public abstract String convert(Object value);

	}

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
	private final Format format;

	public Format getFormat() {
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
	@Native
	private OptionUDT(final int code, final Class<?> klaz, final Format format) {
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

		for (final OptionUDT option : values()) {
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

			} catch (final Exception e) {
				log.error("unexpected; " + optionName, e);
			}

		}

	}

}
