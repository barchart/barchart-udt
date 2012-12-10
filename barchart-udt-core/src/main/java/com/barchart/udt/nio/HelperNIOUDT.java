/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4';VERSION='1.0.2-SNAPSHOT';TIMESTAMP='2011-01-11_09-30-59';
 *
 * Copyright (C) 2009-2011, Barchart, Inc. (http://www.barchart.com/)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *     * Neither the name of the Barchart, Inc. nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Developers: Andrei Pozolotin;
 *
 * =================================================================================
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
