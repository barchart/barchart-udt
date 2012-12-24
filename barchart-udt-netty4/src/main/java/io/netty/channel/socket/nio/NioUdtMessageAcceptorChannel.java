package io.netty.channel.socket.nio;

import io.netty.buffer.MessageBuf;

import com.barchart.udt.TypeUDT;
import com.barchart.udt.nio.ChannelSocketUDT;

public class NioUdtMessageAcceptorChannel extends NioUdtBaseAcceptorChannel {

	protected NioUdtMessageAcceptorChannel() {
		super(TypeUDT.DATAGRAM);
	}

	@Override
	protected int doReadMessages(final MessageBuf<Object> buf) throws Exception {

		final ChannelSocketUDT channelUDT = javaChannel().accept();

		if (channelUDT == null) {
			return 0;
		}

		buf.add(new NioUdtMessageConnectorChannel( //
				this, channelUDT.socketUDT().getSocketId(), channelUDT));

		return 1;

	}

}
