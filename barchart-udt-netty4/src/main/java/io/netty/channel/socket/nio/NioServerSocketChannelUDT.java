package io.netty.channel.socket.nio;

import java.nio.channels.ServerSocketChannel;

import com.barchart.udt.nio.ChannelServerSocketUDT;

public class NioServerSocketChannelUDT extends NioServerSocketChannel {

	protected static void assertChannelUDT(final ServerSocketChannel channel)
			throws RuntimeException {
		if (!(channel instanceof ChannelServerSocketUDT)) {
			throw new IllegalArgumentException(
					"!(channel instanceof ChannelServerSocketUDT)");
		}
	}

}
