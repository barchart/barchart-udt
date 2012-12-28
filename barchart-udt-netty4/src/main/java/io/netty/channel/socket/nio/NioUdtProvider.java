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

import io.netty.bootstrap.AbstractBootstrap.ChannelFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;

import java.io.IOException;
import java.nio.channels.spi.SelectorProvider;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;
import com.barchart.udt.nio.KindUDT;
import com.barchart.udt.nio.SelectorProviderUDT;
import com.barchart.udt.nio.ServerSocketChannelUDT;
import com.barchart.udt.nio.SocketChannelUDT;

/**
 * Netty UDT component provider:
 * <p>
 * provides ChannelFactory for UDT channels
 * <p>
 * provides SelectorProvider for UDT channels
 * <p>
 * see src/test/java/example
 */
public class NioUdtProvider implements ChannelFactory {

    public static final ChannelFactory BYTE_ACCEPTOR = //
    new NioUdtProvider(TypeUDT.STREAM, KindUDT.ACCEPTOR);

    public static final ChannelFactory BYTE_CONNECTOR = //
    new NioUdtProvider(TypeUDT.STREAM, KindUDT.CONNECTOR);

    public static final ChannelFactory BYTE_RENDEZVOUS = //
    new NioUdtProvider(TypeUDT.STREAM, KindUDT.RENDEZVOUS);

    public static final SelectorProvider BYTE_PROVIDER = //
    SelectorProviderUDT.STREAM;

    //

    public static final ChannelFactory MESSAGE_ACCEPTOR = //
    new NioUdtProvider(TypeUDT.DATAGRAM, KindUDT.ACCEPTOR);

    public static final ChannelFactory MESSAGE_CONNECTOR = //
    new NioUdtProvider(TypeUDT.DATAGRAM, KindUDT.CONNECTOR);

    public static final ChannelFactory MESSAGE_RENDEZVOUS = //
    new NioUdtProvider(TypeUDT.DATAGRAM, KindUDT.RENDEZVOUS);

    public static final SelectorProvider MESSAGE_PROVIDER = //
    SelectorProviderUDT.DATAGRAM;

    //

    protected static ServerSocketChannelUDT newAcceptorChannelUDT(
            final TypeUDT type) {
        try {
            return SelectorProviderUDT.from(type).openServerSocketChannel();
        } catch (final IOException e) {
            throw new ChannelException("Failed to open channel");
        }
    }

    protected static SocketChannelUDT newConnectorChannelUDT(final TypeUDT type) {
        try {
            return SelectorProviderUDT.from(type).openSocketChannel();
        } catch (final IOException e) {
            throw new ChannelException("Failed to open channel");
        }
    }

    public final KindUDT kind;

    public final TypeUDT type;

    protected NioUdtProvider(final TypeUDT type, final KindUDT kind) {
        this.type = type;
        this.kind = kind;
    }

    @Override
    public Channel newChannel() {
        switch (kind) {
        case ACCEPTOR:
            switch (type) {
            case DATAGRAM:
                return new NioUdtMessageAcceptorChannel();
            case STREAM:
                return new NioUdtByteAcceptorChannel();
            default:
                throw new IllegalStateException("wrong type=" + type);
            }
        case CONNECTOR:
            switch (type) {
            case DATAGRAM:
                return new NioUdtMessageConnectorChannel();
            case STREAM:
                return new NioUdtByteConnectorChannel();
            default:
                throw new IllegalStateException("wrong type=" + type);
            }
        default:
            throw new IllegalStateException("wrong kind=" + kind);
        }
    }

    public static SocketUDT socketUDT(final Channel channel) {
        if (channel instanceof NioUdtMessageAcceptorChannel) {
            return ((NioUdtMessageAcceptorChannel) channel).javaChannel()
                    .socketUDT();
        }
        if (channel instanceof NioUdtByteAcceptorChannel) {
            return ((NioUdtByteAcceptorChannel) channel).javaChannel()
                    .socketUDT();
        }
        if (channel instanceof NioUdtMessageConnectorChannel) {
            return ((NioUdtMessageConnectorChannel) channel).javaChannel()
                    .socketUDT();
        }
        if (channel instanceof NioUdtByteConnectorChannel) {
            return ((NioUdtByteConnectorChannel) channel).javaChannel()
                    .socketUDT();
        }
        return null;
    }

}
