/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package design.select;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.barchart.udt.EpollUDT;
import com.barchart.udt.EpollUDT.Opt;
import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;
import com.barchart.udt.util.HelpUDT;

/**
 * possible way to emulate select() with epoll_wait()
 */
public class Select01 extends SocketUDT {

	public Select01(final TypeUDT type) throws ExceptionUDT {
		super(type);
	}

	private final static EpollUDT epoll;

	static {
		try {
			epoll = new EpollUDT();
		} catch (final ExceptionUDT e) {
			log.error("", e);
			throw new IllegalStateException(e);
		}
	}

	private static final ConcurrentMap<Integer, Integer> //
	registrationMap = new ConcurrentHashMap<Integer, Integer>();

	private static void ensureRegistaration(final Integer socketId)
			throws ExceptionUDT {
		if (registrationMap.containsKey(socketId)) {
			return;
		} else {
			registrationMap.put(socketId, socketId);
			epollAdd0(epoll.id(), socketId.intValue(), Opt.BOTH.code);
		}
	}

	private static IntBuffer readBuffer = HelpUDT
			.newDirectIntBufer(DEFAULT_MAX_SELECTOR_SIZE);
	private static IntBuffer writeBuffer = HelpUDT
			.newDirectIntBufer(DEFAULT_MAX_SELECTOR_SIZE);
	private static IntBuffer sizeBuffer = HelpUDT
			.newDirectIntBufer(UDT_SIZE_COUNT);

	/**
	 * result of select operation
	 * <p>
	 * note: exceptions will be reported in both read and write lists
	 */
	public static class Result {
		public final List<Integer> readList = new ArrayList<Integer>();
		public final List<Integer> writeList = new ArrayList<Integer>();
	}

	/**
	 * TODO add epoll un-registration
	 * 
	 * @param socketList
	 *            - list of sockets interested in select
	 */
	public synchronized static Result select( //
			final List<Integer> socketList, //
			final long millisTimeout) throws ExceptionUDT {

		for (final Integer socketId : socketList) {
			ensureRegistaration(socketId);
		}

		epollWait0(//
				epoll.id(), readBuffer, writeBuffer, sizeBuffer, millisTimeout);

		final int readSize = sizeBuffer.get(UDT_READ_INDEX);

		final Result result = new Result();

		for (int index = 0; index < readSize; index++) {
			result.readList.add(readBuffer.get(index));
		}

		final int writeSize = sizeBuffer.get(UDT_WRITE_INDEX);

		for (int index = 0; index < writeSize; index++) {
			result.writeList.add(writeBuffer.get(index));
		}

		return result;

	}

}
