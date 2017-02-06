package com.me.document.processor.component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.me.document.processor.common.AbstractComponent;
import com.me.document.processor.common.Context;
import com.me.document.processor.common.DocumentAnalyzer;
import com.me.document.processor.common.Term;
import com.me.document.processor.common.TermFilter;
import com.me.document.processor.utils.ReflectionUtils;

public class NGramDocumentWordsCollector extends AbstractComponent {
	
	private static final Log LOG = LogFactory.getLog(DocumentWordsCollector.class);
	private final DocumentAnalyzer analyzer;
	private Map<String, List<List<Term>>> termTableBySeq = new HashMap<String, List<List<Term>>>();
	/*
	 * 词性过滤、单字过滤、停用词过滤
	 */
	private final Set<TermFilter> filters = new HashSet<TermFilter>();
	
	public NGramDocumentWordsCollector(Context context) {
		super(context);
		String analyzerClass = context.getConfiguration().get("processor.document.analyzer.class");
		LOG.info("Analyzer class name: class=" + analyzerClass);
		analyzer = ReflectionUtils.getInstance(
				analyzerClass, DocumentAnalyzer.class, new Object[] { context.getConfiguration() });
		// load term filter classes
		String filterClassNames = context.getConfiguration().get("processor.document.filter.classes");
		if(filterClassNames != null) {
			LOG.info("Load filter classes: classNames=" + filterClassNames);
			String[] aClazz = filterClassNames.split("\\s*,\\s*");
			for(String clazz : aClazz) {
				TermFilter filter = ReflectionUtils.getInstance(
						clazz, TermFilter.class,  new Object[] { context });
				if(filter == null) {
					throw new RuntimeException("Fail to reflect: class=" + clazz);
				}
				filters.add(filter);
				LOG.info("Added filter instance: filter=" + filter);
			}
		}
	}
	
	@Override
	public void fire() {
		for(String label : context.getFDMetadata().getInputRootDir().list()) {
			LOG.info("Collect words for: label=" + label);
			File labelDir = new File(context.getFDMetadata().getInputRootDir(), label);
			File[] files = labelDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.getAbsolutePath().endsWith(context.getFDMetadata().getFileExtensionName());
				}
			});
			LOG.info("Prepare to analyze " + files.length + " files.");
			int n = 0;
			for(File file : files) {
				analyze(label, file);
				++n;
			}
			LOG.info("Analyzed files: count=" + n);
		}
		// output statistics
		stat();
		
		save_ngram_train();
	}
	
	private void save_ngram_train() {
		Map<String, Integer> labels = new LinkedHashMap<String, Integer>();
		Map<String, Integer> terms = new LinkedHashMap<String, Integer>();
		int lab = 0;
		int trm = 0;
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(new File(context.getConfiguration().get("ngram.train.file"))), "UTF-8"));
			Iterator<Entry<String, List<List<Term>>>> termTableBySeqIter = termTableBySeq.entrySet().iterator();
			while(termTableBySeqIter.hasNext()){
				Entry<String, List<List<Term>>> entry = termTableBySeqIter.next();
				Iterator<List<Term>> entryIter = entry.getValue().iterator();
				
				labels.put(entry.getKey(), lab);
				bw.append((lab++) + " ");
				
				while(entryIter.hasNext()){
					List<Term> termList = entryIter.next();
					bw.append("$ ");
					Iterator<Term> termIter = termList.iterator();
					while(termIter.hasNext()){
						Term term = termIter.next();
						if(terms.containsKey(term.getWord())){
							bw.append(terms.get(term.getWord())+" ");
						}else{
							terms.put(term.getWord(), trm);
							bw.append((trm++) + " ");
						}
					}
				}
				
				bw.append("$");
				bw.flush();
				bw.newLine();
			}
		}catch (Exception e) {
			
		}finally{
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		saveMap(terms, context.getConfiguration().get("ngram.train.terms.file"));
		saveMap(labels, context.getConfiguration().get("ngram.train.labels.file"));
	}

	private void saveMap(Map<String, Integer> map, String file) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(new File(file)), "UTF-8"));
			Iterator<Entry<String, Integer>> entryIter = map.entrySet().iterator();
			while(entryIter.hasNext()){
				Entry<String, Integer> entry = entryIter.next();
				bw.write(entry.getKey() + " " + entry.getValue());
				bw.newLine();
			}
		}catch (Exception e) {
		}finally{
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	protected void analyze(String label, File file) {
		String doc = file.getAbsolutePath();
		LOG.debug("Process document: label=" + label + ", file=" + doc);
		List<Term> terms = analyzer.analyzeKeepSeq(file);
		// filter terms
		filterTerms(terms);
		// construct memory structure
		List<List<Term>> docs = termTableBySeq.get(label);
		if(docs == null){
			docs = new LinkedList<List<Term>>();
			termTableBySeq.put(label, docs);
		}
		docs.add(terms);
		
		System.out.println(label + "," + doc + ":" + terms);
		LOG.debug("Done: file=" + file + ", termCount=" + terms.size());
		LOG.debug("Terms in a doc: terms=" + terms);
	}

	protected void filterTerms(List<Term> terms) {
		for(TermFilter filter : filters) {
			filter.filter(terms);
		}
	}

	private void stat() {
		LOG.info("STAT: totalDocCount=" + context.getVectorMetadata().getTotalDocCount());
		LOG.info("STAT: labelCount=" + context.getVectorMetadata().getLabelCount());
		
		Iterator<Entry<String, Map<String, Map<String, Term>>>> iter = context.getVectorMetadata().termTableIterator();
		while(iter.hasNext()) {
			Entry<String, Map<String, Map<String, Term>>> entry = iter.next();
			LOG.info("STAT: label=" + entry.getKey() + ", docCount=" + entry.getValue().size());
		}
	}

}
