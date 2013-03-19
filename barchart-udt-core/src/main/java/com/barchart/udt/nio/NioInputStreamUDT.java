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
import java.nio.ByteBuffer;
import java.nio.channels.IllegalBlockingModeException;

/**
 * {@link InputStream} implementation for UDT sockets.
 */
public class NioInputStreamUDT extends InputStream {

	protected final SocketChannelUDT channel;

	/**
	 * Creates a new input stream for the specified channel.
	 * 
	 * @param channel
	 *            The UDT socket channel.
	 */
	protected NioInputStreamUDT(final SocketChannelUDT channel) {
		if (channel == null) {
			throw new NullPointerException("channel == null");
		}
		if (!channel.isBlocking()) {
			throw new IllegalBlockingModeException();
		}
		this.channel = channel;
	}

	@Override
	public int read() throws IOException {

		/*
		 * Here's the contract from the JavaDoc on this for SocketChannel:
		 * 
		 * A read operation might not fill the buffer, and in fact it might not
		 * read any bytes at all. Whether or not it does so depends upon the
		 * nature and state of the channel. A socket channel in non-blocking
		 * mode, for example, cannot read any more bytes than are immediately
		 * available from the socket's input buffer; similarly, a file channel
		 * cannot read any more bytes than remain in the file. It is guaranteed,
		 * however, that if a channel is in blocking mode and there is at least
		 * one byte remaining in the buffer then this method will block until at
		 * least one byte is read.
		 * 
		 * Long story short: This UDT InputStream should only ever be created
		 * when the SocketChannel's in blocking mode, and when it's in blocking
		 * mode the SocketChannel read call below will block just like we need
		 * it too.
		 */

		final byte[] data = new byte[1];
		read(data);
		return data[0];
	}

	@Override
	public int read(final byte[] bytes) throws IOException {
		return read(bytes, 0, bytes.length);
	}

	@Override
	public int read(final byte[] bytes, final int off, final int len)
			throws IOException {

		if (len > bytes.length - off) {
			throw new IndexOutOfBoundsException("len > bytes.length - off");
		}

		final ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.position(off);
		buffer.limit(off + len);

		final int read = channel.read(buffer);
		return read;
	}

	@Override
	public long skip(final long n) throws IOException {

		final ByteBuffer buffer = ByteBuffer.allocateDirect(32768);
		long remaining = n;

		while (remaining > 0) {

			buffer.limit((int) Math.min(remaining, buffer.capacity()));
			final int ret = channel.read(buffer);

			if (ret <= 0)
				break;

			remaining -= ret;
			buffer.rewind();
		}

		return n - remaining;
	}

	@Override
	public int available() throws IOException {
		// This is the default InputStream return value.
		// The java/net/SocketInputStream.java implementation delegates to
		// the native implementation, which returns 0 on at least some OSes.
		return 0;
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}

	@Override
	public void mark(final int readlimit) {
		throw new UnsupportedOperationException("mark not supported");
	}

	@Override
	public void reset() throws IOException {
		throw new UnsupportedOperationException("reset not supported");
	}

	@Override
	public boolean markSupported() {
		return false;
	}

}
