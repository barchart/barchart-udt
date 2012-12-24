package io.netty.channel.socket.nio;

import io.netty.bootstrap.AbstractBootstrap.ChannelFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;

import java.io.IOException;
import java.nio.channels.spi.SelectorProvider;

import com.barchart.udt.TypeUDT;
import com.barchart.udt.nio.ChannelServerSocketUDT;
import com.barchart.udt.nio.ChannelSocketUDT;
import com.barchart.udt.nio.KindUDT;
import com.barchart.udt.nio.SelectorProviderUDT;

public class NioUdtProvider implements ChannelFactory {

	public static final NioUdtProvider BYTE_ACCEPTOR = //
	new NioUdtProvider(TypeUDT.STREAM, KindUDT.ACCEPTOR);

	public static final NioUdtProvider BYTE_CONNECTOR = //
	new NioUdtProvider(TypeUDT.STREAM, KindUDT.CONNECTOR);

	public static final SelectorProvider BYTE_PROVIDER = //
	SelectorProviderUDT.STREAM;

	//

	public static final NioUdtProvider MESSAGE_ACCEPTOR = //
	new NioUdtProvider(TypeUDT.DATAGRAM, KindUDT.ACCEPTOR);

	public static final NioUdtProvider MESSAGE_CONNECTOR = //
	new NioUdtProvider(TypeUDT.DATAGRAM, KindUDT.CONNECTOR);

	public static final SelectorProvider MESSAGE_PROVIDER = //
	SelectorProviderUDT.DATAGRAM;

	//

	protected static ChannelServerSocketUDT newAcceptorChannelUDT(
			final TypeUDT type) {
		try {
			return SelectorProviderUDT.from(type).openServerSocketChannel();
		} catch (final IOException e) {
			throw new ChannelException("Failed to open channel");
		}
	}

	protected static ChannelSocketUDT newConnectorChannelUDT(final TypeUDT type) {
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

}
