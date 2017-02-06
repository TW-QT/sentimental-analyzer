package com.me.document.processor.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import com.me.document.processor.common.Context;
import com.me.document.processor.common.Term;
import com.me.document.processor.common.TermFilter;
import com.me.document.processor.utils.ReflectionUtils;

public class AggregatedTermFilter implements TermFilter {

	private final List<TermFilter> filters = new ArrayList<TermFilter>(0);
	
	public AggregatedTermFilter(Context context) {
		String classes = context.getConfiguration().get("processor.document.filter.classes");
		if(classes != null) {
			String[] aClass = classes.split("[,;\\s\\|:-]+");
			for(String className : aClass) {
				filters.add(ReflectionUtils.getInstance(className, TermFilter.class));
			}
		}
	}
	
	@Override
	public void filter(Map<String, Term> terms) {
		for(TermFilter filter : filters) {
			filter.filter(terms);
		}
	}

	@Override
	public void filter(List<Term> terms) {
		for(TermFilter filter : filters) {
			filter.filter(terms);
		}
	}

}
