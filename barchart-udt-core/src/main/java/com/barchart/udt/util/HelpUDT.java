/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;
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

	/** direct integer buffer with proper native byte order */
	public static final IntBuffer newDirectIntBufer(final int capacity) {
		/** java int is 4 bytes */
		return ByteBuffer. //
				allocateDirect(capacity * 4). //
				order(ByteOrder.nativeOrder()). //
				asIntBuffer();
	}

	public static <E> Set<E> ungrowableSet(final Set<E> set) {

		return new Set<E>() {

			@Override
			public boolean add(final E o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean addAll(final Collection<? extends E> coll) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void clear() {
				set.clear();
			}

			@Override
			public boolean contains(final Object o) {
				return set.contains(o);
			}

			@Override
			public boolean containsAll(final Collection<?> coll) {
				return set.containsAll(coll);
			}

			@Override
			public boolean equals(final Object o) {
				return set.equals(o);
			}

			@Override
			public int hashCode() {
				return set.hashCode();
			}

			@Override
			public boolean isEmpty() {
				return set.isEmpty();
			}

			@Override
			public Iterator<E> iterator() {
				return set.iterator();
			}

			@Override
			public boolean remove(final Object o) {
				return set.remove(o);
			}

			@Override
			public boolean removeAll(final Collection<?> coll) {
				return set.removeAll(coll);
			}

			@Override
			public boolean retainAll(final Collection<?> coll) {
				return set.retainAll(coll);
			}

			@Override
			public int size() {
				return set.size();
			}

			@Override
			public Object[] toArray() {
				return set.toArray();
			}

			@Override
			public <T> T[] toArray(final T[] a) {
				return set.toArray(a);
			}

			@Override
			public String toString() {
				return set.toString();
			}

		};

	}

	public static <E> Set<E> unmodifiableSet(final Collection<E> values) {

		return new Set<E>() {

			@Override
			public boolean add(final E e) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean addAll(final Collection<? extends E> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void clear() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean contains(final Object o) {
				return values.contains(o);
			}

			@Override
			public boolean containsAll(final Collection<?> c) {
				return values.containsAll(c);
			}

			@Override
			public boolean isEmpty() {
				return values.isEmpty();
			}

			@Override
			public Iterator<E> iterator() {
				return values.iterator();
			}

			@Override
			public boolean remove(final Object o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean removeAll(final Collection<?> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean retainAll(final Collection<?> c) {
				return values.retainAll(c);
			}

			@Override
			public int size() {
				return values.size();
			}

			@Override
			public Object[] toArray() {
				return values.toArray();
			}

			@Override
			public <T> T[] toArray(final T[] a) {
				return values.toArray(a);
			}

		};

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

}
