/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * note: do not change field names; used by JNI
 */
public class MonitorUDT {

	private static final Logger log = LoggerFactory.getLogger(MonitorUDT.class);

	protected final SocketUDT socketUDT;

	protected MonitorUDT(final SocketUDT socketUDT) {
		this.socketUDT = socketUDT;
	}

	// UDT API

	// ### global measurements

	/** time since the UDT entity is started, in milliseconds. */
	protected volatile long msTimeStamp;

	public long millisSinceStart() {
		return msTimeStamp;
	}

	/**
	 * total number of sent data packets, including retransmissions
	 */
	protected volatile long pktSentTotal;

	public long globalSentTotal() {
		return pktSentTotal;
	}

	/**
	 * total number of received packets
	 */
	protected volatile long pktRecvTotal;

	public long globalReceivedTotal() {
		return pktRecvTotal;
	}

	/**
	 * total number of lost packets (sender side)
	 */
	protected volatile int pktSndLossTotal;

	public int globalSenderLost() {
		return pktSndLossTotal;
	}

	/**
	 * total number of lost packets (receiver side)
	 */
	protected volatile int pktRcvLossTotal;

	public int globalReceiverLost() {
		return pktRcvLossTotal;
	}

	/**
	 * total number of retransmitted packets
	 */
	protected volatile int pktRetransTotal;

	public int globalRetransmittedTotal() {
		return pktRetransTotal;
	}

	/**
	 * total number of sent ACK packets
	 */
	protected volatile int pktSentACKTotal;

	public int globalSentAckTotal() {
		return pktSentACKTotal;
	}

	/**
	 * total number of received ACK packets
	 */
	protected volatile int pktRecvACKTotal;

	public int globalReceivedAckTotal() {
		return pktRecvACKTotal;
	}

	/**
	 * total number of sent NAK packets
	 */
	protected volatile int pktSentNAKTotal;

	public int globalSentNakTotal() {
		return pktSentNAKTotal;
	}

	/**
	 * total number of received NAK packets
	 */
	protected volatile int pktRecvNAKTotal;

	public int globalReceivedNakTotal() {
		return pktRecvNAKTotal;
	}

	/**
	 * total time duration when UDT is sending data (idle time exclusive)
	 */
	protected volatile long usSndDurationTotal;

	public long globalMicrosSendDurationTotal() {
		return usSndDurationTotal;
	}

	// ### local measurements

	/**
	 * number of sent data packets, including retransmissions
	 */
	protected volatile long pktSent;

	public long localPacketsSent() {
		return pktSent;
	}

	/**
	 * number of received packets
	 */
	protected volatile long pktRecv;

	public long localPacketsReceived() {
		return pktRecv;
	}

	/**
	 * number of lost packets (sender side)
	 */
	protected volatile int pktSndLoss;

	public int localSenderLost() {
		return pktSndLoss;
	}

	/**
	 * number of lost packets (receiverer side)
	 */
	protected volatile int pktRcvLoss;

	public int localReceiverLost() {
		return pktRcvLoss;
	}

	/**
	 * number of retransmitted packets
	 */
	protected volatile int pktRetrans;

	public int localRetransmitted() {
		return pktRetrans;
	}

	/**
	 * number of sent ACK packets
	 */
	protected volatile int pktSentACK;

	public int localSentAck() {
		return pktSentACK;
	}

	/**
	 * number of received ACK packets
	 */
	protected volatile int pktRecvACK;

	public int localReceivedAck() {
		return pktRecvACK;
	}

	/**
	 * number of sent NAK packets
	 */
	protected volatile int pktSentNAK;

	public int localSentNak() {
		return pktSentNAK;
	}

	/**
	 * number of received NAK packets
	 */
	protected volatile int pktRecvNAK;

	public int localReceivedNak() {
		return pktRecvNAK;
	}

	/**
	 * sending rate in Mb/s
	 */
	protected volatile double mbpsSendRate;

	public double mbpsSendRate() {
		return mbpsSendRate;
	}

	/**
	 * receiving rate in Mb/s
	 */
	protected volatile double mbpsRecvRate;

	public double mbpsReceiveRate() {
		return mbpsRecvRate;
	}

	/**
	 * busy sending time (i.e., idle time exclusive)
	 */
	protected volatile long usSndDuration;

	public long microsSendTime() {
		return usSndDuration;
	}

	// ### instant measurements

	/**
	 * packet sending period, in microseconds
	 */
	protected volatile double usPktSndPeriod;

	public double currentSendPeriod() {
		return usPktSndPeriod;
	}

	/**
	 * flow window size, in number of packets
	 */
	protected volatile int pktFlowWindow;

	public int currentFlowWindow() {
		return pktFlowWindow;
	}

	/**
	 * congestion window size, in number of packets
	 */
	protected volatile int pktCongestionWindow;

	public int currentCongestionWindow() {
		return pktCongestionWindow;
	}

	/**
	 * number of packets on flight
	 */
	protected volatile int pktFlightSize;

	public int currentFlightSize() {
		return pktFlightSize;
	}

	/**
	 * RTT, in milliseconds
	 */
	protected volatile double msRTT;

	public double currentMillisRTT() {
		return msRTT;
	}

	/**
	 * estimated bandwidth, in Mb/s
	 */
	protected volatile double mbpsBandwidth;

	public double currentMbpsBandwidth() {
		return mbpsBandwidth;
	}

	/**
	 * available UDT sender buffer size
	 */
	protected volatile int byteAvailSndBuf;

	public int currentAvailableInSender() {
		return byteAvailSndBuf;
	}

	/**
	 * available UDT receiver buffer size
	 */
	protected volatile int byteAvailRcvBuf;

	public int currentAvailableInReceiver() {
		return byteAvailRcvBuf;
	}

	/**
	 * current monitor status snapshot for all parameters
	 */
	public void appendSnapshot(final StringBuilder text) {

		text.append("\n\t");
		text.append(String.format("[id: 0x%08x]", socketUDT.id()));

		final Field fieldArray[] = MonitorUDT.class.getDeclaredFields();

		for (final Field field : fieldArray) {

			if (!isNumeric(field)) {
				continue;
			}

			try {

				field.setAccessible(true);

				final String fieldName = field.getName();
				final String fieldValue = field.get(this).toString();

				text.append("\n\t");
				text.append(fieldName);
				text.append(" = ");
				text.append(fieldValue);

			} catch (final Exception e) {
				log.error("unexpected", e);
			}

		}

		final double localSendLoss = 100.0 * pktSndLoss / pktSent;

		text.append("\n\t% localSendLoss = ");
		text.append(localSendLoss);

		final double localReceiveLoss = 100.0 * pktRcvLoss / pktRecv;

		text.append("\n\t% localReceiveLoss = ");
		text.append(localReceiveLoss);

	}

	protected boolean isNumeric(final Field field) {

		final Class<?> fieledType = field.getType();

		return fieledType == int.class || fieledType == long.class
				|| fieledType == double.class;

	}

	@Override
	public String toString() {

		final StringBuilder text = new StringBuilder(1024);

		appendSnapshot(text);

		return text.toString();

	}

}
