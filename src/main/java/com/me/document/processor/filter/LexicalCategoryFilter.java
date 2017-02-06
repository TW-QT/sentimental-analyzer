package com.me.document.processor.filter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.me.document.processor.common.Context;
import com.me.document.processor.common.Term;
import com.me.document.processor.common.TermFilter;

public class LexicalCategoryFilter implements TermFilter {

	private final Set<String> keptLexicalCategories = new HashSet<String>();
	
	public LexicalCategoryFilter(Context context) {
		// read configured lexical categories
		String lexicalCategories = 
				context.getConfiguration().get("processor.document.filter.kept.lexical.categories", "n");
		for(String category : lexicalCategories.split("\\s*,\\s*")) {
			keptLexicalCategories.add(category);
		}
	}
	
	@Override
	public void filter(Map<String, Term> terms) {
		Iterator<Entry<String, Term>> iter = terms.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Term> entry = iter.next();
			if(!keptLexicalCategories.contains(entry.getValue().getLexicalCategory())) {
				iter.remove();
			}
		}
	}

	@Override
	public void filter(List<Term> terms) {
		Iterator<Term> iter = terms.iterator();
		while(iter.hasNext()) {
			Term entry = iter.next();
			if(!keptLexicalCategories.contains(entry.getLexicalCategory())) {
				iter.remove();
			}
		}
	}

}
