/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper around the base UDT congestion control class
 * 
 * @see <a href="http://udt.sourceforge.net/udt4/doc/ccc.htm">reference</a>
 * @see <a href="http://udt.sourceforge.net/udt4/doc/t-cc.htm">tutorial</a>
 * @see FactoryUDT
 * @see FactoryInterfaceUDT
 * 
 * @author CCob
 */
public class CCC {

	/** Force SocketUDT to load JNI if it hasn't already */
	private static boolean initOk = SocketUDT.INIT_OK;

	/** Used internally by the JNI layer, points to JNICCC class */
	private long nativeHandle;

	private int msINT;
	private int pktINT;
	private int usRTO;
	private final Logger log = LoggerFactory.getLogger(CCC.class);

	private native void initNative();

	protected native void setACKTimer(final int msINT);

	protected native void setACKInterval(final int pktINT);

	protected native void setRTO(final int usRTO);

	protected native void setPacketSndPeriod(final double sndPeriod);

	protected native void setCWndSize(final double cWndSize);

	protected native MonitorUDT getPerfInfo();

	public CCC() {
		initNative();
	}

	public void init() {
		log.info("CCC::init");
	}

	public void close() {
		log.info("CCC::close");
	}

	public void onACK(final int ack) {
	}

	public void onLoss(final int[] lossList) {
	}

	public void onTimeout() {
	}

	// TODO: implement Java wrapper around CPacket
	// public void onPktSent(const CPacket* pkt) {}
	// public void onPktReceived(const CPacket* pkt) {}
	// public void processCustomMsg(const CPacket& pkt) {}/
	// void sendCustomMsg(CPacket& pkt) const;
}
