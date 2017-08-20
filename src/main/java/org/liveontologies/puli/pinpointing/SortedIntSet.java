package org.liveontologies.puli.pinpointing;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import com.google.common.primitives.Ints;

public class SortedIntSet extends AbstractSet<Integer> {

	private final int[] elements_; // sorted!

	SortedIntSet(int[] elements) {
		this.elements_ = elements;
	}

	int[] getElements() {
		return elements_;
	}

	@Override
	public boolean contains(Object o) {
		if (o instanceof Integer) {
			return Arrays.binarySearch(elements_, (Integer) o) >= 0;
		}
		// else
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if (c instanceof SortedIntSet) {
			SortedIntSet other = (SortedIntSet) c;
			return SortedIdSet.containsAll(elements_, other.elements_);
		}
		return super.containsAll(c);
	}

	@Override
	public Iterator<Integer> iterator() {
		return Ints.asList(elements_).iterator();
	}

	@Override
	public int size() {
		return elements_.length;
	}

}
