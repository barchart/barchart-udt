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

class UngrowableSet<E> implements Set<E> {

	private final Set<E> set;

	UngrowableSet(final Set<E> set) {
		this.set = set;
	}

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

}
