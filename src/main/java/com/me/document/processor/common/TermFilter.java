package com.me.document.processor.common;

import java.util.List;
import java.util.Map;

public interface TermFilter {

	void filter(Map<String, Term> terms);
	void filter(List<Term> terms);
}
