/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;

class ConnectorExecutorsUDT {

	private static final RejectedExecutionHandler defaultHandler = new AbortPolicy();

	public static ExecutorService newDynamicThreadPool( //
			int maximumPoolSize, //
			long keepAliveTime, // 
			TimeUnit keepAliveUnit, //
			ThreadFactory threadFactory) {

		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();

		ThreadPoolExecutor pool = new ThreadPoolExecutor(//
				maximumPoolSize, //
				maximumPoolSize, //
				keepAliveTime, //
				keepAliveUnit, //
				workQueue, //
				threadFactory, //
				defaultHandler);

		pool.allowCoreThreadTimeOut(true);

		return pool;

	}

}
