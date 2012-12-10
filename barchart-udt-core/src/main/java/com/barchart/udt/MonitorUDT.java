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

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* note: do not change field names; used by JNI */
public class MonitorUDT {

	private static final Logger log = LoggerFactory.getLogger(MonitorUDT.class);

	protected final SocketUDT socketUDT;

	protected MonitorUDT(SocketUDT socketUDT) {
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
	public void appendSnapshot(StringBuilder text) {

		text.append("\n\t");
		text.append("socketID");
		text.append(" = ");
		text.append(socketUDT.socketID);

		Field fieldArray[] = MonitorUDT.class.getDeclaredFields();

		for (Field field : fieldArray) {

			if (!isNumeric(field)) {
				continue;
			}

			try {

				field.setAccessible(true);

				String fieldName = field.getName();
				String fieldValue = field.get(this).toString();

				text.append("\n\t");
				text.append(fieldName);
				text.append(" = ");
				text.append(fieldValue);

			} catch (Exception e) {
				log.error("unexpected", e);
			}

		}

		double localSendLoss = 100.0 * (double) pktSndLoss / (double) pktSent;

		text.append("\n\t% localSendLoss = ");
		text.append(localSendLoss);

		double localReceiveLoss = 100.0 * (double) pktRcvLoss
				/ (double) pktRecv;

		text.append("\n\t% localReceiveLoss = ");
		text.append(localReceiveLoss);

	}

	protected boolean isNumeric(Field field) {

		Class<?> fieledType = field.getType();

		return fieledType == int.class || fieledType == long.class
				|| fieledType == double.class;

	}

	@Override
	public String toString() {

		StringBuilder text = new StringBuilder(1024);

		appendSnapshot(text);

		return text.toString();

	}

}
