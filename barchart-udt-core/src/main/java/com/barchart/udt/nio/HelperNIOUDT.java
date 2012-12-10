/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt.nio;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

class HelperNIOUDT {

	static <E> Set<E> ungrowableSet(final Set<E> s) {
		return new Set<E>() {

			@Override
			public int size() {
				return s.size();
			}

			@Override
			public boolean isEmpty() {
				return s.isEmpty();
			}

			@Override
			public boolean contains(Object o) {
				return s.contains(o);
			}

			@Override
			public Object[] toArray() {
				return s.toArray();
			}

			@Override
			public <T> T[] toArray(T[] a) {
				return s.toArray(a);
			}

			@Override
			public String toString() {
				return s.toString();
			}

			@Override
			public Iterator<E> iterator() {
				return s.iterator();
			}

			@Override
			public boolean equals(Object o) {
				return s.equals(o);
			}

			@Override
			public int hashCode() {
				return s.hashCode();
			}

			@Override
			public void clear() {
				s.clear();
			}

			@Override
			public boolean remove(Object o) {
				return s.remove(o);
			}

			@Override
			public boolean containsAll(Collection<?> coll) {
				return s.containsAll(coll);
			}

			@Override
			public boolean removeAll(Collection<?> coll) {
				return s.removeAll(coll);
			}

			@Override
			public boolean retainAll(Collection<?> coll) {
				return s.retainAll(coll);
			}

			@Override
			public boolean add(E o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean addAll(Collection<? extends E> coll) {
				throw new UnsupportedOperationException();
			}

		};

	}

}
