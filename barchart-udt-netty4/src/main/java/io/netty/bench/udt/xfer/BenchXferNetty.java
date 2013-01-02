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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioUdtProvider;
import io.netty.example.udt.util.ConsoleReporterUDT;
import io.netty.example.udt.util.ThreadFactoryUDT;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Timer;

/**
 * perform two way raw send/recv
 */
public final class BenchXferNetty {

    private BenchXferNetty() {
    }

    static final Logger log = LoggerFactory.getLogger(BenchXferNetty.class);

    /** benchmark duration */
    static final int time = 10 * 60 * 1000;

    /** transfer chunk size */
    static final int size = 64 * 1024;

    static final Counter benchTime = Metrics.newCounter(BenchXferNetty.class,
            "bench time");

    static final Counter benchSize = Metrics.newCounter(BenchXferNetty.class,
            "bench size");

    static {
        benchTime.inc(time);
        benchSize.inc(size);
    }

    static final Meter sendRate = Metrics.newMeter(BenchXferNetty.class,
            "send rate", "bytes", TimeUnit.SECONDS);

    static final Timer sendTime = Metrics.newTimer(BenchXferNetty.class,
            "send time", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

    public static void main(final String[] args) throws Exception {

        log.info("init");

        final AtomicBoolean isOn = new AtomicBoolean(true);

        final InetSocketAddress addr1 = UnitHelp.localSocketAddress();
        final InetSocketAddress addr2 = UnitHelp.localSocketAddress();

        final ChannelHandler handler1 = new BenchPeerHandler(sendRate, size);
        final ChannelHandler handler2 = new BenchPeerHandler(null, size);

        final Bootstrap peerBoot1 = peerBoot(addr1, addr2, handler1);
        final Bootstrap peerBoot2 = peerBoot(addr2, addr1, handler2);

        final ChannelFuture peerFuture1 = peerBoot1.connect();
        final ChannelFuture peerFuture2 = peerBoot2.connect();

        ConsoleReporterUDT.enable(3, TimeUnit.SECONDS);

        Thread.sleep(time);

        isOn.set(false);

        Thread.sleep(1 * 1000);

        peerFuture1.channel().close().sync();
        peerFuture2.channel().close().sync();

        Thread.sleep(1 * 1000);

        peerBoot1.shutdown();
        peerBoot2.shutdown();

        Metrics.defaultRegistry().shutdown();

        log.info("done");
    }

    static Bootstrap peerBoot(final InetSocketAddress self,
            final InetSocketAddress peer, final ChannelHandler handler) {

        final Bootstrap boot = new Bootstrap();

        final ThreadFactory connectFactory = new ThreadFactoryUDT("peer");

        final NioEventLoopGroup connectGroup = new NioEventLoopGroup(2,
                connectFactory, NioUdtProvider.MESSAGE_PROVIDER);

        boot.group(connectGroup)
                .channelFactory(NioUdtProvider.MESSAGE_RENDEZVOUS)
                .localAddress(self).remoteAddress(peer).handler(handler);

        return boot;
    }

}
