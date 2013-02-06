/**
 * Copyright (C) 2009-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

class UnmodifiableSet<E> implements Set<E> {

	private final Collection<E> values;

	UnmodifiableSet(final Collection<E> values) {
		this.values = values;
	}

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
		throw new UnsupportedOperationException();
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

}
