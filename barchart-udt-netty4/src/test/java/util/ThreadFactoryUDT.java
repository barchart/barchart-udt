package util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactoryUDT implements ThreadFactory {

	private static final AtomicInteger counter = new AtomicInteger();

	private final String name;

	public ThreadFactoryUDT(final String name) {
		this.name = name;
	}

	@Override
	public Thread newThread(final Runnable runnable) {

		return new Thread( //
				runnable, "udt-" + name + "-" + counter.getAndIncrement());

	};

}
