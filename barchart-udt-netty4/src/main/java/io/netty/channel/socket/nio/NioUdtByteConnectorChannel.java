/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.socket.nio;

import io.netty.buffer.BufType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.socket.DefaultUdtChannelConfig;
import io.netty.channel.socket.UdtChannel;
import io.netty.channel.socket.UdtChannelConfig;
import io.netty.logging.InternalLogger;
import io.netty.logging.InternalLoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.barchart.udt.TypeUDT;
import com.barchart.udt.nio.ChannelSocketUDT;
import com.sun.nio.sctp.SctpChannel;

/**
 * 
 */
public class NioUdtByteConnectorChannel extends AbstractNioByteChannel
		implements UdtChannel {

	protected static final InternalLogger logger = //
	InternalLoggerFactory.getInstance(NioUdtByteConnectorChannel.class);

	protected static final ChannelMetadata METADATA = //
	new ChannelMetadata(BufType.MESSAGE, false);

	private final UdtChannelConfig config;

	/**
	 */
	protected NioUdtByteConnectorChannel() {
		this(TypeUDT.STREAM);
	}

	/**
	 * Create a new instance
	 * 
	 * @param parent
	 *            the {@link Channel} which is the parent of this
	 *            {@link NioUdtByteConnectorChannel} or {@code null}.
	 * @param id
	 *            the id to use for this instance or {@code null} if a new once
	 *            should be generated
	 * @param channelUDT
	 *            the underlying {@link SctpChannel}
	 */
	protected NioUdtByteConnectorChannel( //
			final Channel parent, //
			final Integer id, //
			final ChannelSocketUDT channelUDT //
	) {

		super(parent, id, channelUDT);

		try {

			channelUDT.configureBlocking(false);

			config = new DefaultUdtChannelConfig(channelUDT.socketUDT());

		} catch (final IOException e) {
			try {
				channelUDT.close();
			} catch (final IOException e2) {
				if (logger.isWarnEnabled()) {
					logger.warn("Failed to close channel.", e2);
				}
			}
			throw new ChannelException("Failed to enter non-blocking mode.", e);
		}
	}

	/**
	 */
	protected NioUdtByteConnectorChannel(final ChannelSocketUDT channelUDT) {
		this(null, channelUDT.socketUDT().getSocketId(), channelUDT);
	}

	/**
	 */
	protected NioUdtByteConnectorChannel(final TypeUDT type) {
		this(NioUdtProvider.newConnectorChannelUDT(type));
	}

	@Override
	public UdtChannelConfig config() {
		return config;
	}

	@Override
	protected void doBind(final SocketAddress localAddress) throws Exception {

		logger.debug("### bind = " + localAddress);

		javaChannel().bind(localAddress);

	}

	@Override
	protected void doClose() throws Exception {
		javaChannel().close();
	}

	@Override
	protected boolean doConnect(final SocketAddress remoteAddress,
			final SocketAddress localAddress) throws Exception {

		logger.debug("### connect " + javaChannel());

		if (localAddress != null) {
			javaChannel().bind(localAddress);
		}

		boolean success = false;

		try {

			final boolean connected = javaChannel().connect(remoteAddress);

			logger.debug("### connected=" + connected);

			if (connected) {
				selectionKey().interestOps(SelectionKey.OP_READ);
			} else {
				selectionKey().interestOps(SelectionKey.OP_CONNECT);
			}

			success = true;

			return connected;

		} finally {
			if (!success) {
				doClose();
			}
			logger.debug("### success=" + success);
		}

	}

	@Override
	protected void doDisconnect() throws Exception {
		doClose();
	}

	@Override
	protected void doFinishConnect() throws Exception {

		if (!javaChannel().finishConnect()) {
			throw new Error("provider error");
		}

		selectionKey().interestOps(SelectionKey.OP_READ);

	}

	@Override
	protected int doReadBytes(final ByteBuf byteBuf) throws Exception {
		return byteBuf.writeBytes(javaChannel(), byteBuf.writableBytes());
	}

	@Override
	protected int doWriteBytes(final ByteBuf byteBuf, final boolean lastSpin)
			throws Exception {

		final int pendingBytes = byteBuf.readableBytes();

		final int writtenBytes = byteBuf.readBytes( //
				javaChannel(), pendingBytes);

		final SelectionKey key = selectionKey();

		final int interestOps = key.interestOps();

		if (writtenBytes >= pendingBytes) {
			// wrote the buffer completely - clear OP_WRITE.
			if ((interestOps & SelectionKey.OP_WRITE) != 0) {
				key.interestOps(interestOps & ~SelectionKey.OP_WRITE);
			}
		} else {
			// wrote partial or nothing - ensure OP_WRITE
			if (writtenBytes > 0 || lastSpin) {
				if ((interestOps & SelectionKey.OP_WRITE) == 0) {
					key.interestOps(interestOps | SelectionKey.OP_WRITE);
				}
			}
		}

		return writtenBytes;

	}

	@Override
	public boolean isActive() {
		final SocketChannel ch = javaChannel();
		return ch.isOpen() && ch.isConnected();
	}

	@Override
	protected ChannelSocketUDT javaChannel() {
		return (ChannelSocketUDT) super.javaChannel();
	}

	@Override
	protected SocketAddress localAddress0() {
		return javaChannel().socket().getLocalSocketAddress();
	}

	@Override
	public ChannelMetadata metadata() {
		return METADATA;
	}

	@Override
	protected SocketAddress remoteAddress0() {
		return javaChannel().socket().getRemoteSocketAddress();
	}

}
