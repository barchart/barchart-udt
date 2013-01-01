package com.barchart.udt.nio;

import com.barchart.udt.SocketUDT;

/**
 * TODO rendezvous
 */
public class DatagramChannelUDT extends SocketChannelUDT implements ChannelUDT {

	protected DatagramChannelUDT( //
			final SelectorProviderUDT provider, //
			final SocketUDT socketUDT //
	) {

		super(provider, socketUDT);

	}

	@Override
	public KindUDT kindUDT() {
		return KindUDT.RENDEZVOUS;
	}

}
