package io.netty.channel.socket.nio;

import io.netty.buffer.ChannelBufType;
import io.netty.buffer.MessageBuf;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.socket.DefaultServerSocketChannelConfig;
import io.netty.channel.socket.ServerSocketChannelConfig;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.barchart.udt.nio.ChannelServerSocketUDT;
import com.barchart.udt.nio.SelectorProviderUDT;

/**
 * emulate jdk server socket channel for netty
 */
public class NioServerSocketChannelUDT extends AbstractNioMessageChannel
		implements io.netty.channel.socket.ServerSocketChannel {

	protected static void assertChannelUDT(final ServerSocketChannel channel)
			throws RuntimeException {
		if (!(channel instanceof ChannelServerSocketUDT)) {
			throw new IllegalArgumentException(
					"!(channel instanceof ChannelServerSocketUDT)");
		}
	}

	protected static ServerSocketChannel newChannelUDT() {
		return newChannelUDT(SelectorProviderUDT.STREAM);
	}

	protected static ServerSocketChannel newChannelUDT(
			final SelectorProviderUDT provider) {
		try {
			return provider.openServerSocketChannel();
		} catch (final Exception e) {
			throw new ChannelException("Failed to open a socket.", e);
		}
	}

	//

	protected static final ChannelMetadata METADATA = new ChannelMetadata(
			ChannelBufType.MESSAGE, false);

	protected final ServerSocketChannelConfig config;

	public NioServerSocketChannelUDT() {
		super(null, null, newChannelUDT(), SelectionKey.OP_ACCEPT);
		config = new DefaultServerSocketChannelConfig(javaChannel().socket());
	}

	@Override
	public ChannelMetadata metadata() {
		return METADATA;
	}

	@Override
	public ServerSocketChannelConfig config() {
		return config;
	}

	@Override
	public boolean isActive() {
		return javaChannel().socket().isBound();
	}

	@Override
	public InetSocketAddress remoteAddress() {
		return null;
	}

	@Override
	protected ServerSocketChannel javaChannel() {
		return (ServerSocketChannel) super.javaChannel();
	}

	@Override
	protected SocketAddress localAddress0() {
		return javaChannel().socket().getLocalSocketAddress();
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
	protected int doReadMessages(final MessageBuf<Object> buf) throws Exception {
		final SocketChannel ch = javaChannel().accept();
		if (ch == null) {
			return 0;
		}
		buf.add(new NioSocketChannel(this, null, ch));
		return 1;
	}

	//

	@Override
	protected boolean doConnect(final SocketAddress remoteAddress,
			final SocketAddress localAddress) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void doFinishConnect() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	protected SocketAddress remoteAddress0() {
		return null;
	}

	@Override
	protected void doDisconnect() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	protected int doWriteMessages(final MessageBuf<Object> buf,
			final boolean lastSpin) throws Exception {
		throw new UnsupportedOperationException();
	}

}
