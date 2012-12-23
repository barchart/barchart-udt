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
package example.echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannelUDT;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.nio.SelectorProviderUDT;

import example.util.ThreadFactoryUDT;

/**
 * Sends one message when a connection is open and echoes back any received data
 * to the server. Simply put, the echo client initiates the ping-pong traffic
 * between the echo client and server by sending the first message to the
 * server.
 */
public class EchoClient {

	static Logger log = LoggerFactory.getLogger("client");

	private final String host;
	private final int port;
	private final int firstMessageSize;

	public EchoClient(final String host, final int port,
			final int firstMessageSize) {
		this.host = host;
		this.port = port;
		this.firstMessageSize = firstMessageSize;
	}

	public void run() throws Exception {

		// Configure the client.

		final Bootstrap boot = new Bootstrap();

		final SelectorProvider selectorProvider = SelectorProviderUDT.DATAGRAM;

		final ThreadFactory connectFactory = new ThreadFactoryUDT("udt-connect");

		final NioEventLoopGroup connectGroup = //
		new NioEventLoopGroup(1, connectFactory, selectorProvider);

		try {

			boot.group(connectGroup)
					//
					.channel(NioSocketChannelUDT.class)
					.remoteAddress(new InetSocketAddress(host, port))
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(final SocketChannel ch)
								throws Exception {
							ch.pipeline().addLast(
									new LoggingHandler(LogLevel.INFO),
									new EchoClientHandler(firstMessageSize));
						}
					});

			// Start the client.
			final ChannelFuture f = boot.connect().sync();

			// Wait until the connection is closed.
			f.channel().closeFuture().sync();

		} finally {

			// Shut down the event loop to terminate all threads.
			boot.shutdown();

		}

	}

	public static void main(final String[] args) throws Exception {

		log.info("init");

		final String host = "localhost";
		final int port = 8080;
		final int firstMessageSize = 256;

		new EchoClient(host, port, firstMessageSize).run();

		log.info("done");

	}

}
