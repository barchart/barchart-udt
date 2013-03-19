/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * {@link OutputStream} for UDT sockets.
 */
public class NioOutputStreamUDT extends OutputStream {

	protected final SocketChannelUDT channel;

	/**
	 * Creates a new UDT output stream.
	 * 
	 * @param channel
	 *            The UDT socket channel.
	 */
	protected NioOutputStreamUDT(final SocketChannelUDT channel) {
		this.channel = channel;
	}

	@Override
	public void write(final byte[] bytes, final int off, final int len)
			throws IOException {
		channel.write(ByteBuffer.wrap(bytes, off, len));
	}

	@Override
	public void write(final byte[] bytes) throws IOException {
		channel.write(ByteBuffer.wrap(bytes));
	}

	@Override
	public void write(final int b) throws IOException {
		// Just cast it -- this is the same thing SocketOutputStream does.
		final byte[] bytes = { (byte) b };
		channel.write(ByteBuffer.wrap(bytes));
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}

}
