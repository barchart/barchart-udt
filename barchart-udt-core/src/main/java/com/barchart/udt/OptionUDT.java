/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import static com.barchart.udt.OptionUDT.Format.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.util.HelpUDT;

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
public class OptionUDT<T> {

	static {
		log = LoggerFactory.getLogger(OptionUDT.class);
		values = new CopyOnWriteArrayList<OptionUDT<?>>();
	}

	/** the Maximum Transfer Unit. */
	public static final OptionUDT<Integer> UDT_MSS = //
	NEW(0, Integer.class, DECIMAL);
	/** the Maximum Transfer Unit., bytes */
	public static final OptionUDT<Integer> Maximum_Transfer_Unit = //
	NEW(0, Integer.class, DECIMAL);

	/** if sending is blocking. */
	public static final OptionUDT<Boolean> UDT_SNDSYN = //
	NEW(1, Boolean.class, BOOLEAN);
	/** if sending is blocking., true/false */
	public static final OptionUDT<Boolean> Is_Send_Synchronous = //
	NEW(1, Boolean.class, BOOLEAN);

	/** if receiving is blocking. */
	public static final OptionUDT<Boolean> UDT_RCVSYN = //
	NEW(2, Boolean.class, BOOLEAN);
	/** if receiving is blocking, true/false */
	public static final OptionUDT<Boolean> Is_Receive_Synchronous = //
	NEW(2, Boolean.class, BOOLEAN);

	/** custom congestion control algorithm */
	@SuppressWarnings("rawtypes")
	public static final OptionUDT<FactoryUDT> UDT_CC = //
	NEW(3, FactoryUDT.class, DEFAULT);
	/** custom congestion control algorithm, class factory */
	@SuppressWarnings("rawtypes")
	public static final OptionUDT<FactoryUDT> Custom_Congestion_Control = //
	NEW(3, FactoryUDT.class, DEFAULT);

	/** Flight flag size (window size). */
	public static final OptionUDT<Integer> UDT_FC = //
	NEW(4, Integer.class, BINARY);
	/** Flight flag size (window size), bytes */
	public static final OptionUDT<Integer> Flight_Window_Size = //
	NEW(4, Integer.class, BINARY);

	/** maximum buffer in sending queue. */
	public static final OptionUDT<Integer> UDT_SNDBUF = //
	NEW(5, Integer.class, DECIMAL);
	/** maximum buffer in sending queue. */
	public static final OptionUDT<Integer> Protocol_Send_Buffer_Size = //
	NEW(5, Integer.class, DECIMAL);

	/** UDT receiving buffer size. */
	public static final OptionUDT<Integer> UDT_RCVBUF = //
	NEW(6, Integer.class, DECIMAL);
	/** UDT receiving buffer size limit, bytes */
	public static final OptionUDT<Integer> Protocol_Receive_Buffer_Size = //
	NEW(6, Integer.class, DECIMAL);

	/** waiting for unsent data when closing. */
	public static final OptionUDT<LingerUDT> UDT_LINGER = //
	NEW(7, LingerUDT.class, DECIMAL);
	/** waiting for unsent data when closing. true/false and timeout, seconds */
	public static final OptionUDT<LingerUDT> Time_To_Linger_On_Close = //
	NEW(7, LingerUDT.class, DECIMAL);

	/** UDP sending buffer size. */
	public static final OptionUDT<Integer> UDP_SNDBUF = //
	NEW(8, Integer.class, DECIMAL);
	/** UDP sending buffer size limit, bytes */
	public static final OptionUDT<Integer> System_Send_Buffer_Size = //
	NEW(8, Integer.class, DECIMAL);

	/** UDP receiving buffer size. */
	public static final OptionUDT<Integer> UDP_RCVBUF = //
	NEW(9, Integer.class, DECIMAL);
	/** UDP receiving buffer size limit, bytes */
	public static final OptionUDT<Integer> System_Receive_Buffer_Size = //
	NEW(9, Integer.class, DECIMAL);

	/* maximum datagram message size */
	// UDT_MAXMSG(10, Integer.class, DECIMAL); no support in udt core

	/* time-to-live of a datagram message */
	// UDT_MSGTTL(11, Integer.class, DECIMAL); no support in udt core

	/** rendezvous connection mode. */
	public static final OptionUDT<Boolean> UDT_RENDEZVOUS = //
	NEW(12, Boolean.class, BOOLEAN);
	/** rendezvous connection mode, enabled/disabled */
	public static final OptionUDT<Boolean> Is_Randezvous_Connect_Enabled = //
	NEW(12, Boolean.class, BOOLEAN);

	/** send() timeout. */
	public static final OptionUDT<Integer> UDT_SNDTIMEO = //
	NEW(13, Integer.class, DECIMAL);
	/** send() timeout. milliseconds */
	public static final OptionUDT<Integer> Send_Timeout = //
	NEW(13, Integer.class, DECIMAL);

	/** recv() timeout. */
	public static final OptionUDT<Integer> UDT_RCVTIMEO = //
	NEW(14, Integer.class, DECIMAL);
	/** recv() timeout. milliseconds */
	public static final OptionUDT<Integer> Receive_Timeout = //
	NEW(14, Integer.class, DECIMAL);

	/** reuse an existing port or create a one. */
	public static final OptionUDT<Boolean> UDT_REUSEADDR = //
	NEW(15, Boolean.class, BOOLEAN);
	/** reuse an existing port or create a one. true/false */
	public static final OptionUDT<Boolean> Is_Address_Reuse_Enabled = //
	NEW(15, Boolean.class, BOOLEAN);

	/** maximum bandwidth (bytes per second) that the connection can use. */
	public static final OptionUDT<Long> UDT_MAXBW = //
	NEW(16, Long.class, DECIMAL);
	/** maximum bandwidth (bytes per second) that the connection can use. */
	public static final OptionUDT<Long> Maximum_Bandwidth = //
	NEW(16, Long.class, DECIMAL);

	/** current socket state, see UDTSTATUS, read only */
	public static final OptionUDT<Integer> UDT_STATE = //
	NEW(17, Integer.class, DECIMAL);
	/** current socket status code, see {@link StatusUDT#getCode()}, read only */
	public static final OptionUDT<Integer> Status_Code = //
	NEW(17, Integer.class, DECIMAL);

	/** current available events associated with the socket */
	public static final OptionUDT<Integer> UDT_EVENT = //
	NEW(18, Integer.class, DECIMAL);
	/** current available epoll events, see {@link EpollUDT.Opt#code} */
	public static final OptionUDT<Integer> Epoll_Event_Mask = //
	NEW(18, Integer.class, DECIMAL);

	/** size of data in the sending buffer */
	public static final OptionUDT<Integer> UDT_SNDDATA = //
	NEW(19, Integer.class, DECIMAL);
	/** current consumed sending buffer utilization, read only, bytes */
	public static final OptionUDT<Integer> Send_Buffer_Consumed = //
	NEW(19, Integer.class, DECIMAL);

	/** size of data available for recv */
	public static final OptionUDT<Integer> UDT_RCVDATA = //
	NEW(20, Integer.class, DECIMAL);
	/** current available receiving buffer capacity, read only, bytes */
	public static final OptionUDT<Integer> Receive_Buffer_Available = //
	NEW(20, Integer.class, DECIMAL);

	//

	protected OptionUDT(final int code, final Class<T> klaz, final Format format) {

		this.code = code;
		this.type = klaz;
		this.format = format;

		values.add(this);

	}

	protected static <T> OptionUDT<T> NEW(final int code, final Class<T> klaz,
			final Format format) {
		return new OptionUDT<T>(code, klaz, format);
	}

	public static void appendSnapshot( //
			final SocketUDT socketUDT, //
			final StringBuilder text //
	) {

		text.append("\n\t");
		text.append(String.format("[id: 0x%08x]", socketUDT.id()));

		for (final OptionUDT<?> option : values) {
			int optionCode = 0;
			String optionName = null;
			String optionValue = null;
			try {

				optionCode = option.code;
				optionName = option.name();

				optionValue = option.format.convert(//
						socketUDT.getOption(option));

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

	protected static final Logger log;
	protected static final List<OptionUDT<?>> values;

	private final int code;
	private final Class<?> type;
	private final Format format;
	private String name;

	public int code() {
		return code;
	}

	public Class<?> type() {
		return type;
	}

	public Format format() {
		return format;
	}

	public String name() {
		if (name == null) {
			name = HelpUDT.constantFieldName(getClass(), this);
		}
		return name;
	}

	/**
	 * render options in human format
	 */
	public enum Format {

		DECIMAL() {
			@Override
			public String convert(final Object value) {
				if (value instanceof Number) {
					final long number = ((Number) value).longValue();
					return String.format("%,d", number);
				}
				return "invalid format";
			}
		}, //

		BINARY() {
			@Override
			public String convert(final Object value) {
				if (value instanceof Number) {
					final long number = ((Number) value).longValue();
					return String.format("%,d (%,d K)", number, number / 1024);
				}
				return "invalid format";
			}
		}, //

		BOOLEAN() {
			@Override
			public String convert(final Object value) {
				if (value instanceof Boolean) {
					final boolean bool = ((Boolean) value).booleanValue();
					return String.format("%b", bool);
				}
				return "invalid format";
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

}
