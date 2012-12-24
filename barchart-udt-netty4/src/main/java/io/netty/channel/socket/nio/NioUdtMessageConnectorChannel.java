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
import io.netty.buffer.MessageBuf;
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

/**
 * 
 */
public class NioUdtMessageConnectorChannel extends AbstractNioMessageChannel
		implements UdtChannel {

	protected static final InternalLogger logger = //
	InternalLoggerFactory.getInstance(NioUdtMessageConnectorChannel.class);

	protected static final ChannelMetadata METADATA = //
	new ChannelMetadata(BufType.MESSAGE, false);

	private final UdtChannelConfig config;

	/**
	 * Create a new instance
	 */
	protected NioUdtMessageConnectorChannel() {
		this(TypeUDT.DATAGRAM);
	}

	/**
	 * Create a new instance
	 * 
	 * @param parent
	 *            the {@link Channel} which is the parent of this
	 *            {@link NioUdtMessageConnectorChannel} or {@code null}.
	 * @param id
	 *            the id to use for this instance or {@code null} if a new once
	 *            should be generated
	 * @param channelUDT
	 *            the underlying {@link SctpChannel}
	 */
	protected NioUdtMessageConnectorChannel( //
			final Channel parent, //
			final Integer id, //
			final ChannelSocketUDT channelUDT //
	) {

		super(parent, id, channelUDT, SelectionKey.OP_READ);

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
	protected NioUdtMessageConnectorChannel(final ChannelSocketUDT channelUDT) {
		this(null, channelUDT.socketUDT().getSocketId(), channelUDT);
	}

	/**
	 */
	protected NioUdtMessageConnectorChannel(final TypeUDT type) {
		this(NioUdtProvider.newConnectorChannelUDT(type));
	}

	@Override
	public UdtChannelConfig config() {
		return config;
	}

	@Override
	protected void doBind(final SocketAddress localAddress) throws Exception {
		javaChannel().bind(localAddress);
	}

	@Override
	protected void doClose() throws Exception {
		javaChannel().close();
	}

	@Override
	protected boolean doConnect(final SocketAddress remoteAddress,
			final SocketAddress localAddress) throws Exception {

		if (localAddress != null) {
			javaChannel().bind(localAddress);
		}

		boolean success = false;

		try {
			final boolean connected = javaChannel().connect(remoteAddress);
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

	@Override
	protected int doReadMessages(final MessageBuf<Object> buf) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int doWriteMessages(final MessageBuf<Object> buf,
			final boolean lastSpin) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

}
