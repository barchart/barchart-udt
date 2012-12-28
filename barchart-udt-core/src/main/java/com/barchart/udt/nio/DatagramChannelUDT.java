package com.barchart.udt.nio;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

/**
 * TODO rendezvous
 */
public class DatagramChannelUDT extends DatagramChannel implements ChannelUDT {

	protected DatagramChannelUDT(final SelectorProviderUDT provider) {

		super(provider);

	}

	@Override
	public DatagramChannel connect(final SocketAddress remote)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DatagramChannel disconnect() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void implCloseSelectableChannel() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void implConfigureBlocking(final boolean block)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isConnectFinished() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public KindUDT kindUDT() {
		return KindUDT.RENDEZVOUS;
	}

	@Override
	public SelectorProviderUDT providerUDT() {
		return (SelectorProviderUDT) super.provider();
	}

	@Override
	public int read(final ByteBuffer dst) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long read(final ByteBuffer[] dsts, final int offset, final int length)
			throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SocketAddress receive(final ByteBuffer dst) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int send(final ByteBuffer src, final SocketAddress target)
			throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DatagramSocket socket() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SocketUDT socketUDT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int write(final ByteBuffer src) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long write(final ByteBuffer[] srcs, final int offset,
			final int length) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TypeUDT typeUDT() {
		return providerUDT().type;
	}

}
