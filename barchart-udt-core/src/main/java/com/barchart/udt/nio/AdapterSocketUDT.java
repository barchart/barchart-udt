/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.net.NetSocketUDT;

public class AdapterSocketUDT extends NetSocketUDT {

	protected final ChannelSocketUDT channelUDT;

	protected AdapterSocketUDT(final ChannelSocketUDT channelSocketUDT,
			final SocketUDT socketUDT) {
		super(socketUDT);
		this.channelUDT = channelSocketUDT;
	}

	@Override
	public SocketChannel getChannel() {
		return channelUDT;
	}

	@Override
	public synchronized InputStream getInputStream() throws IOException {
		if (inputStream == null) {
			inputStream = new AdapterInputStreamUDT(this.channelUDT, this);
		}
		return inputStream;
	}

	@Override
	public synchronized OutputStream getOutputStream() throws IOException {
		if (outputStream == null) {
			outputStream = new AdapterOutputStreamUDT(this.channelUDT, this);
		}
		return outputStream;
	}

}
