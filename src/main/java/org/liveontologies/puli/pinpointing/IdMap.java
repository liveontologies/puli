package org.liveontologies.puli.pinpointing;

public interface IdMap<E> {

	public int getId(E element);

	public E getElement(int id);

	public Integer contains(Object o);

}
