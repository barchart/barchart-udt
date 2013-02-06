/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper around the UDT CCCFactory class
 * 
 * @see <a href="http://udt.sourceforge.net/udt4/doc/ccc.htm">reference</a>
 * @see <a href="http://udt.sourceforge.net/udt4/doc/t-cc.htm">tutorial</a>
 * @see CCC
 * 
 * @author CCob
 */
public class FactoryUDT<C> implements FactoryInterfaceUDT {

	C classType;
	final Class<C> clazz;

	Logger log = LoggerFactory.getLogger(FactoryUDT.class);

	boolean doInit = false;
	boolean doClose = false;
	boolean doOnACK = false;
	boolean doOnLoss = false;
	boolean doOnTimeout = false;

	public FactoryUDT(final Class<C> clazz) {

		this.clazz = clazz;

		if (!CCC.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException(
					"Generic argument 'C' must be 'CCC' class or extension");
		}

		try {

			if (clazz.getMethod("init").getDeclaringClass() != CCC.class)
				doInit = true;

			if (clazz.getMethod("close").getDeclaringClass() != CCC.class)
				doClose = true;

			if (clazz.getMethod("onACK", int.class).getDeclaringClass() != CCC.class)
				doOnACK = true;

			if (clazz.getMethod("onLoss", int[].class).getDeclaringClass() != CCC.class)
				doOnLoss = true;

			if (clazz.getMethod("onTimeout").getDeclaringClass() != CCC.class)
				doOnTimeout = true;

		} catch (final SecurityException e) {
			log.error("Error setting up class factory", e);
		} catch (final NoSuchMethodException e) {
			log.error("Expected CCC method doesn't exsit", e);
		}
	}

	@Override
	public CCC create() {

		try {
			final Object cccObj = clazz.newInstance();
			return (CCC) cccObj;
		} catch (final InstantiationException e) {
			log.error("Failed to instansiate CCC class", e);
		} catch (final IllegalAccessException e) {
			log.error("Failed to instansiate CCC class", e);
		}

		return null;
	}

	@Override
	public FactoryInterfaceUDT cloneFactory() {
		return new FactoryUDT<C>(clazz);
	}

}
