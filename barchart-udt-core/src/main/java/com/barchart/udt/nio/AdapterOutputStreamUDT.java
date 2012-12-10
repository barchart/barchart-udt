package com.barchart.udt.nio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * {@link OutputStream} for UDT sockets.
 */
class AdapterOutputStreamUDT extends OutputStream {

	private final SocketChannel channel;

	/**
	 * Creates a new UDT output stream.
	 * 
	 * @param channel
	 *            The UDT socket channel.
	 * @param socketUDT
	 *            The UDT socket.
	 */
	AdapterOutputStreamUDT(final SocketChannel channel, final Socket socketUDT) {
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
