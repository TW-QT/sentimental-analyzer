package com.me.document.processor.filter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.me.document.processor.common.Context;
import com.me.document.processor.common.Term;
import com.me.document.processor.common.TermFilter;

public class SingleWordTermFilter implements TermFilter {

	public SingleWordTermFilter(Context context) {
		super();
	}
	
	@Override
	public void filter(Map<String, Term> terms) {
		Iterator<Entry<String, Term>> iter = terms.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Term> entry = iter.next();
			if(entry.getValue().getWord() == null 
					|| entry.getValue().getWord().length() <= 1) {
				iter.remove();
			}
		}
	}

	@Override
	public void filter(List<Term> terms) {
		Iterator<Term> iter = terms.iterator();
		while(iter.hasNext()) {
			Term entry = iter.next();
			if(entry.getWord() == null 
					|| entry.getWord().length() <= 1) {
				iter.remove();
			}
		}
	}

}
