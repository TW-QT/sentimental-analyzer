package com.me.document.processor.common;

public interface Component {

	void fire();
	Component getNext();
	Component setNext(Component next);
}
