/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.EpollUDT;

/**
 * miscellaneous utilities
 */
public class HelpUDT {

	protected static final Logger log = LoggerFactory.getLogger(EpollUDT.class);

	public static long md5sum(final String text) {

		final byte[] defaultBytes = text.getBytes();

		try {

			final MessageDigest algorithm = MessageDigest.getInstance("MD5");

			algorithm.reset();

			algorithm.update(defaultBytes);

			final byte digest[] = algorithm.digest();

			final ByteBuffer buffer = ByteBuffer.wrap(digest);

			return buffer.getLong();

		} catch (final NoSuchAlgorithmException e) {

			log.error("md5 failed", e);

			return 0;

		}

	}

	/**
	 * direct integer buffer with proper native byte order
	 */
	public static final IntBuffer newDirectIntBufer(final int capacity) {
		/** java int is 4 bytes */
		return ByteBuffer. //
				allocateDirect(capacity * 4). //
				order(ByteOrder.nativeOrder()). //
				asIntBuffer();
	}

	public static <E> Set<E> ungrowableSet(final Set<E> set) {
		return new UngrowableSet<E>(set);
	}

	public static <E> Set<E> unmodifiableSet(final Collection<E> values) {
		return new UnmodifiableSet<E>(values);
	}

	private HelpUDT() {
	}

	public static final void checkBuffer(final ByteBuffer buffer) {
		if (buffer == null) {
			throw new IllegalArgumentException("buffer == null");
		}
		if (!buffer.isDirect()) {
			throw new IllegalArgumentException("must use DirectByteBuffer");
		}
	}

	public static final void checkArray(final byte[] array) {
		if (array == null) {
			throw new IllegalArgumentException("array == null");
		}
	}

	public static String constantFieldName(final Class<?> klaz,
			final Object instance) {

		final Field[] filedArray = klaz.getDeclaredFields();

		for (final Field field : filedArray) {

			final int modifiers = field.getModifiers();

			final boolean isConstant = true && //
					Modifier.isPublic(modifiers) && //
					Modifier.isStatic(modifiers) && //
					Modifier.isFinal(modifiers) //
			;

			if (isConstant) {
				try {
					if (instance == field.get(null)) {
						return field.getName();
					}
				} catch (final Throwable e) {
					log.debug("", e);
				}
			}

		}

		return "unknown";

	}

	public static void checkSocketAddress(final InetSocketAddress socketAddress) {
		if (socketAddress == null) {
			throw new IllegalArgumentException("socketAddress can't be null");
		}
		/** can not use in JNI ; internal InetAddress field is null */
		if (socketAddress.isUnresolved()) {
			throw new IllegalArgumentException("socketAddress is unresolved : "
					+ socketAddress + " : check your DNS settings");
		}
	}

}
