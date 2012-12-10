/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConnectorThreadFactoryUDT implements ThreadFactory {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(ConnectorThreadFactoryUDT.class);

	private final AtomicLong instanceCount = new AtomicLong(0);

	public final String namePrefix;
	public final int defaultPriority;

	ConnectorThreadFactoryUDT(String namePrefix, int defaultPriority) {
		this.namePrefix = namePrefix;
		this.defaultPriority = defaultPriority;
	}

	ConnectorThreadFactoryUDT(String namePrefix) {
		this(namePrefix, Thread.NORM_PRIORITY);
	}

	@Override
	public Thread newThread(Runnable runnable) {

		Thread thread = new Thread(runnable);

		thread.setPriority(defaultPriority);

		long instanceId = instanceCount.getAndIncrement();

		String defaultName = String.format("%s%-4d", namePrefix, instanceId);

		thread.setName(defaultName);

		return thread;

	}

}
