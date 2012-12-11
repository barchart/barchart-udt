/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

class ConnectorThreadPoolUDT {

	final ConnectorThreadFactoryUDT factory;

	final Queue<SelectionKeyUDT> readyQueue = //
	new ConcurrentLinkedQueue<SelectionKeyUDT>();

	final ConcurrentMap<SelectionKeyUDT, Runnable> taskMap = //
	new ConcurrentHashMap<SelectionKeyUDT, Runnable>();

	final ExecutorService service;

	final String THREAD_PREFIX = "UDT Connector #";

	final int THREAD_PRIORITY = Thread.NORM_PRIORITY;

	final int THREAD_TIME_KEEP = 3 * 1000;
	final TimeUnit THREAD_TIME_UNIT = TimeUnit.MILLISECONDS;

	ConnectorThreadPoolUDT(int maximumPoolSize) {

		factory = new ConnectorThreadFactoryUDT(THREAD_PREFIX, THREAD_PRIORITY);

		// service = Executors.newCachedThreadPool();

		service = ConnectorExecutorsUDT.newDynamicThreadPool(//
				maximumPoolSize, //
				THREAD_TIME_KEEP, //
				THREAD_TIME_UNIT, //
				factory);

	}

	void submitRequest(SelectionKeyUDT keyUDT, InetSocketAddress remote) {

		if (taskMap.containsKey(keyUDT)) {
			return;
		}

		Runnable task = new ConnectorTaskUDT(//
				keyUDT, taskMap, readyQueue, remote);

		Runnable result = taskMap.putIfAbsent(keyUDT, task);

		if (result == null) {
			service.submit(task);
		}

	}

}
