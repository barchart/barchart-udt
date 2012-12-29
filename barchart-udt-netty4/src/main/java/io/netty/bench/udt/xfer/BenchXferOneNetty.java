/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.bench.udt.xfer;

import io.netty.bench.udt.util.UnitHelp;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.MessageBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandler;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.UdtMessage;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioUdtProvider;
import io.netty.example.udt.util.ConsoleReporterUDT;
import io.netty.example.udt.util.ThreadFactoryUDT;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

/**
 * perform one way raw send/recv in 2 threads
 */
public final class BenchXferOneNetty {

    private BenchXferOneNetty() {
    }

    static final Logger log = LoggerFactory.getLogger(BenchXferOneNetty.class);

    /** benchmark duration */
    static final int time = 60 * 1000;

    /** transfer chunk size */
    static final int size = 64 * 1024;

    static final Counter benchTime = Metrics.newCounter(
            BenchXferOneNetty.class, "bench time");

    static final Counter benchSize = Metrics.newCounter(
            BenchXferOneNetty.class, "bench size");

    static {
        benchTime.inc(time);
        benchSize.inc(size);
    }

    static final Meter sendRate = Metrics.newMeter(BenchXferOneNetty.class,
            "send rate", "bytes", TimeUnit.SECONDS);

    static final Timer sendTime = Metrics.newTimer(BenchXferOneNetty.class,
            "send time", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

    public static void main(final String[] args) throws Exception {

        log.info("init");

        final ExecutorService executor = Executors.newFixedThreadPool(1);

        final AtomicBoolean isOn = new AtomicBoolean(true);

        // receiver
        final ChannelInboundMessageHandler<UdtMessage> serverHandler = //
        new ChannelInboundMessageHandlerAdapter<UdtMessage>() {
            @Override
            protected void messageReceived(final ChannelHandlerContext ctx,
                    final UdtMessage message) throws Exception {
                message.free();
            }
        };

        // sender
        final ChannelInboundMessageHandler<UdtMessage> clientHandler = //
        new ChannelInboundMessageHandlerAdapter<UdtMessage>() {

            @Override
            public void channelActive(final ChannelHandlerContext ctx)
                    throws Exception {

                final Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        while (isOn.get()) {
                            try {
                                final ByteBuf byteBuf = ctx.alloc()
                                        .directBuffer(size);

                                byteBuf.setIndex(0, byteBuf.capacity());

                                final UdtMessage message = new UdtMessage(
                                        byteBuf);

                                sendRate.mark(message.data().readableBytes());

                                final MessageBuf<Object> out = ctx
                                        .nextOutboundMessageBuffer();

                                out.add(message);

                                final TimerContext timer = sendTime.time();

                                ctx.flush().sync();

                                timer.stop();

                            } catch (final Exception e) {
                                log.error("", e);
                            }
                        }
                    }
                };

                executor.submit(task);
            }

            @Override
            protected void messageReceived(final ChannelHandlerContext ctx,
                    final UdtMessage message) throws Exception {
                message.free();
            }
        };

        final InetSocketAddress recvAddress = UnitHelp.localSocketAddress();

        final ServerBootstrap serverBoot = serverBoot(recvAddress,
                serverHandler);

        final Bootstrap clientBoot = clientBoot(recvAddress, clientHandler);

        final ChannelFuture serverFuture = serverBoot.bind().sync();

        final ChannelFuture clientFuture = clientBoot.connect().sync();

        ConsoleReporterUDT.enable(3, TimeUnit.SECONDS);

        Thread.sleep(time);

        isOn.set(false);

        clientFuture.channel().close().sync();
        serverFuture.channel().close().sync();

        clientBoot.shutdown();
        serverBoot.shutdown();

        executor.shutdownNow();

        Thread.sleep(1 * 1000);

        Metrics.defaultRegistry().shutdown();

        log.info("done");
    }

    static ServerBootstrap serverBoot(final InetSocketAddress address,
            final ChannelHandler handler) {

        final ThreadFactory acceptFactory = new ThreadFactoryUDT("accept");

        final ThreadFactory serverFactory = new ThreadFactoryUDT("server");

        final NioEventLoopGroup acceptGroup = new NioEventLoopGroup(1,
                acceptFactory, NioUdtProvider.MESSAGE_PROVIDER);

        final NioEventLoopGroup connectGroup = new NioEventLoopGroup(1,
                serverFactory, NioUdtProvider.MESSAGE_PROVIDER);

        final ServerBootstrap boot = new ServerBootstrap();

        boot.group(acceptGroup, connectGroup)
                .channelFactory(NioUdtProvider.MESSAGE_ACCEPTOR)
                .option(ChannelOption.SO_BACKLOG, 10).localAddress(address)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(handler);

        return boot;
    }

    static Bootstrap clientBoot(final InetSocketAddress address,
            final ChannelHandler handler) {

        final Bootstrap boot = new Bootstrap();

        final ThreadFactory connectFactory = new ThreadFactoryUDT("client");

        final NioEventLoopGroup connectGroup = new NioEventLoopGroup(1,
                connectFactory, NioUdtProvider.MESSAGE_PROVIDER);

        boot.group(connectGroup)
                .channelFactory(NioUdtProvider.MESSAGE_CONNECTOR)
                .localAddress("localhost", 0).remoteAddress(address)
                .handler(handler);

        return boot;
    }

}
