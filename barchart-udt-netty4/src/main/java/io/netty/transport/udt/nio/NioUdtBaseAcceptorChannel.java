/*
 * Copyright 2012 The Netty Project
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
package io.netty.transport.udt.nio;

import io.netty.buffer.BufType;
import io.netty.buffer.MessageBuf;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.socket.nio.AbstractNioMessageChannel;
import io.netty.logging.InternalLogger;
import io.netty.logging.InternalLoggerFactory;
import io.netty.transport.udt.DefaultUdtChannelConfig;
import io.netty.transport.udt.UdtChannel;
import io.netty.transport.udt.UdtChannelConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;

import com.barchart.udt.TypeUDT;
import com.barchart.udt.nio.ServerSocketChannelUDT;

/**
 * Common base for Netty Byte/Message UDT Stream/Datagram acceptors.
 */
public abstract class NioUdtBaseAcceptorChannel extends
        AbstractNioMessageChannel implements UdtChannel {

    protected static final InternalLogger logger = InternalLoggerFactory
            .getInstance(NioUdtBaseAcceptorChannel.class);

    protected static final ChannelMetadata METADATA = new ChannelMetadata(
            BufType.MESSAGE, false);

    protected final UdtChannelConfig config;

    protected NioUdtBaseAcceptorChannel(final ServerSocketChannelUDT channelUDT) {
        super(null, channelUDT.socketUDT().id(), channelUDT,
                SelectionKey.OP_ACCEPT);
        try {
            channelUDT.configureBlocking(false);
            config = new DefaultUdtChannelConfig();
            config.apply(channelUDT);
        } catch (final IOException e) {
            try {
                channelUDT.close();
            } catch (final IOException e2) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to close channel.", e2);
                }
            }
            throw new ChannelException("Failed configure channel.", e);
        }
    }

    protected NioUdtBaseAcceptorChannel(final TypeUDT type) {
        this(NioUdtProvider.newAcceptorChannelUDT(type));
    }

    @Override
    public UdtChannelConfig config() {
        return config;
    }

    @Override
    protected void doBind(final SocketAddress localAddress) throws Exception {
        javaChannel().socket().bind(localAddress, config.getBacklog());
        final SelectionKey selectionKey = selectionKey();
        selectionKey.interestOps(selectionKey.interestOps()
                | SelectionKey.OP_ACCEPT);
    }

    @Override
    protected void doClose() throws Exception {
        javaChannel().close();
    }

    @Override
    protected boolean doConnect(final SocketAddress remoteAddress,
            final SocketAddress localAddress) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doDisconnect() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doFinishConnect() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int doWriteMessages(final MessageBuf<Object> buf,
            final boolean lastSpin) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isActive() {
        return javaChannel().socket().isBound();
    }

    @Override
    protected ServerSocketChannelUDT javaChannel() {
        return (ServerSocketChannelUDT) super.javaChannel();
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
    public InetSocketAddress remoteAddress() {
        return null;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return null;
    }

}
