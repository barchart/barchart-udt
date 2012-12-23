/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.barchart.udt.HelperUDT;

/**
 * miscellaneous utilities
 */
public class HelpUDT {

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

			HelperUDT.log.error("md5 failed", e);

			return 0;

		}

	}

	public static <E> Set<E> unmodifiableSet(final Collection<E> values) {

		return new Set<E>() {

			@Override
			public int size() {
				return values.size();
			}

			@Override
			public boolean isEmpty() {
				return values.isEmpty();
			}

			@Override
			public boolean contains(final Object o) {
				return values.contains(o);
			}

			@Override
			public Iterator<E> iterator() {
				return values.iterator();
			}

			@Override
			public Object[] toArray() {
				return values.toArray();
			}

			@Override
			public <T> T[] toArray(final T[] a) {
				return values.toArray(a);
			}

			@Override
			public boolean add(final E e) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean remove(final Object o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean containsAll(final Collection<?> c) {
				return values.containsAll(c);
			}

			@Override
			public boolean addAll(final Collection<? extends E> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean retainAll(final Collection<?> c) {
				return values.retainAll(c);
			}

			@Override
			public boolean removeAll(final Collection<?> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void clear() {
				throw new UnsupportedOperationException();
			}

		};

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

}
