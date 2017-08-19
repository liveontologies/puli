package org.liveontologies.puli.pinpointing;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class HashIdMap<E> implements IdMap<E> {

	BiMap<E, Integer> baseBiMap_;

	int nextId_ = 0;

	private HashIdMap() {
		baseBiMap_ = HashBiMap.create();
	}

	private HashIdMap(int expectedSize) {
		baseBiMap_ = HashBiMap.create(expectedSize);
	}

	public static <E> IdMap<E> create() {
		return new HashIdMap<E>();
	}

	public static <E> IdMap<E> create(int expectedSize) {
		return new HashIdMap<E>(expectedSize);
	}

	@Override
	public int getId(E element) {
		Integer result = baseBiMap_.get(element);
		if (result != null) {
			return result;
		}
		// else
		baseBiMap_.put(element, nextId_);
		return (nextId_++);
	}

	@Override
	public E getElement(int id) {
		return baseBiMap_.inverse().get(id);
	}

	@Override
	public Integer contains(Object o) {
		return baseBiMap_.get(o);
	}

}
