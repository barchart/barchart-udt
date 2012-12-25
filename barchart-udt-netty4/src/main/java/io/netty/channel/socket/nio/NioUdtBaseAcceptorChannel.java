package io.netty.channel.socket.nio;

import io.netty.buffer.BufType;
import io.netty.buffer.MessageBuf;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.socket.DefaultUdtChannelConfig;
import io.netty.channel.socket.UdtChannel;
import io.netty.channel.socket.UdtChannelConfig;
import io.netty.logging.InternalLogger;
import io.netty.logging.InternalLoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;

import com.barchart.udt.TypeUDT;
import com.barchart.udt.nio.ChannelServerSocketUDT;

/**
 * emulate jdk server socket channel for netty
 */
public abstract class NioUdtBaseAcceptorChannel extends
		AbstractNioMessageChannel implements UdtChannel {

	protected static final InternalLogger logger = //
	InternalLoggerFactory.getInstance(NioUdtBaseAcceptorChannel.class);

	protected static final ChannelMetadata METADATA = //
	new ChannelMetadata(BufType.MESSAGE, false);

	//

	protected final UdtChannelConfig config;

	protected NioUdtBaseAcceptorChannel(final ChannelServerSocketUDT channelUDT) {

		super( //
				null, //
				channelUDT.socketUDT().getSocketId(), //
				channelUDT, //
				SelectionKey.OP_ACCEPT //
		);

		config = new DefaultUdtChannelConfig(channelUDT.socketUDT());

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

		selectionKey.interestOps( //
				selectionKey.interestOps() | SelectionKey.OP_ACCEPT);

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
	protected ChannelServerSocketUDT javaChannel() {

		return (ChannelServerSocketUDT) super.javaChannel();

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
