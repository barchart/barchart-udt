package io.netty.channel.socket.nio;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;

import java.nio.channels.SocketChannel;

import com.barchart.udt.nio.ChannelSocketUDT;
import com.barchart.udt.nio.SelectorProviderUDT;

/**
 * emulate jdk socket channel for netty
 */
public class NioSocketChannelUDT extends NioSocketChannel {

	protected static void assertChannelUDT(final SocketChannel channel)
			throws RuntimeException {
		if (!(channel instanceof ChannelSocketUDT)) {
			throw new IllegalArgumentException(
					"!(channel instanceof ChannelSocketUDT)");
		}
	}

	protected static SocketChannel newChannelUDT() {
		return newChannelUDT(SelectorProviderUDT.STREAM);
	}

	protected static SocketChannel newChannelUDT(
			final SelectorProviderUDT provider) {
		try {
			return provider.openSocketChannel();
		} catch (final Exception e) {
			throw new ChannelException("Failed to open a socket.", e);
		}
	}

	public NioSocketChannelUDT() {
		this(newChannelUDT());
	}

	public NioSocketChannelUDT(final SocketChannel channel) {

		this(null, null, channel);

		assertChannelUDT(channel);

	}

	public NioSocketChannelUDT(final Channel parent, final Integer id,
			final SocketChannel channel) {

		super(parent, id, channel);

		assertChannelUDT(channel);

	}

}
