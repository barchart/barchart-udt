package io.netty.channel.socket.nio;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;

import java.nio.channels.SocketChannel;

import com.barchart.udt.nio.ChannelSocketUDT;
import com.barchart.udt.nio.SelectorProviderUDT;

/**
 * emulate jdk socket channel for netty
 */
public class NioUdtSocketChannel extends NioSocketChannel {

	protected static void assertChannelUDT(final SocketChannel channel)
			throws RuntimeException {
		if (!(channel instanceof ChannelSocketUDT)) {
			throw new IllegalArgumentException(
					"!(channel instanceof ChannelSocketUDT)");
		}
	}

	protected static ChannelSocketUDT newChannelUDT() {
		return newChannelUDT(SelectorProviderUDT.STREAM);
	}

	protected static ChannelSocketUDT newChannelUDT(
			final SelectorProviderUDT provider) {
		try {
			return provider.openSocketChannel();
		} catch (final Exception e) {
			throw new ChannelException("Failed to open a socket.", e);
		}
	}

	public NioUdtSocketChannel() {

		this(newChannelUDT());

	}

	public NioUdtSocketChannel(final ChannelSocketUDT channelUDT) {

		this(null, channelUDT.socketUDT().getSocketId(), channelUDT);

	}

	public NioUdtSocketChannel(final Channel parent, final Integer id,
			final ChannelSocketUDT channelUDT) {

		super(parent, id, channelUDT);

	}

}
