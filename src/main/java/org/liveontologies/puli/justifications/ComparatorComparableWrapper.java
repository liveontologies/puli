/*-
 * #%L
 * Proof Utility Library
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2017 Live Ontologies Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.liveontologies.puli.justifications;

import java.util.Comparator;

public class ComparatorComparableWrapper<T>
		implements ComparableWrapper<T, ComparatorComparableWrapper<T>> {

	private final T wrapped_;

	private final Comparator<? super T> comparator_;

	public ComparatorComparableWrapper(final T wrapped,
			final Comparator<? super T> comparator) {
		this.wrapped_ = wrapped;
		this.comparator_ = comparator;
	}

	@Override
	public int compareTo(final ComparatorComparableWrapper<T> other) {
		return comparator_.compare(wrapped_, other.getWrapped());
	}

	@Override
	public T getWrapped() {
		return wrapped_;
	}

	public static class Factory<T> implements
			ComparableWrapper.Factory<T, ComparatorComparableWrapper<T>> {

		private final Comparator<? super T> comparator_;

		public Factory(final Comparator<? super T> comparator) {
			this.comparator_ = comparator;
		}

		@Override
		public ComparatorComparableWrapper<T> wrap(final T delegate) {
			return new ComparatorComparableWrapper<T>(delegate, comparator_);
		}

	}

}
