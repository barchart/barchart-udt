/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.net.NetSocketUDT;

public class NioSocketUDT extends NetSocketUDT {

	protected final SocketChannelUDT channelUDT;

	protected NioSocketUDT(final SocketChannelUDT channelUDT)
			throws ExceptionUDT {
		super(channelUDT.socketUDT());
		this.channelUDT = channelUDT;
	}

	@Override
	public SocketChannelUDT getChannel() {
		return channelUDT;
	}

	@Override
	public synchronized InputStream getInputStream() throws IOException {
		if (inputStream == null) {
			inputStream = new NioInputStreamUDT(channelUDT);
		}
		return inputStream;
	}

	@Override
	public synchronized OutputStream getOutputStream() throws IOException {
		if (outputStream == null) {
			outputStream = new NioOutputStreamUDT(channelUDT);
		}
		return outputStream;
	}

}
