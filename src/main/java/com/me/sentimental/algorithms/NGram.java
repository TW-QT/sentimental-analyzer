package com.me.sentimental.algorithms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.me.document.processor.common.Term;

public class NGram {
			
	public NGram_Model train(Map<String, List<List<String>>> train_data) {
		Map<String, Map<String, Integer>> termFreq = new HashMap<String, Map<String, Integer>>(); 
		Iterator<Entry<String, List<List<String>>>> train_data_iter = train_data.entrySet().iterator();
		while(train_data_iter.hasNext()){
			Entry<String, List<List<String>>> entry = train_data_iter.next();
			if(!termFreq.containsKey(entry.getKey())){
				termFreq.put(entry.getKey(), new HashMap<String, Integer>());
			}
			String last = null;
			Iterator<List<String>> entryIter = entry.getValue().iterator();
			while(entryIter.hasNext()){
				List<String> doc = entryIter.next();
				Iterator<String> termIter = doc.iterator();
				
				while(termIter.hasNext()){
					String term = termIter.next();
					Integer count = termFreq.get(entry.getKey()).get(term);
					if(count == null){
						termFreq.get(entry.getKey()).put(term, 1);
					}else{
						termFreq.get(entry.getKey()).put(term, count + 1);
					}
					
					if(last != null){
						String pair = last + "#" +term;
						Integer c = termFreq.get(entry.getKey()).get(pair);
						if(c == null){
							termFreq.get(entry.getKey()).put(pair, 1);
						}else{
							termFreq.get(entry.getKey()).put(pair, c + 1);
						}
					}
					last = term;
				}
			}
			Integer end = termFreq.get(entry.getKey()).get(last+"#$");
			if(end == null){
				termFreq.get(entry.getKey()).put(last+"#$", 1);
			}else{
				end++;
			}
		}
		NGram_Model model = new NGram_Model();
		model.setTermFreq(termFreq);
		return model;
	}

	
	
	public static void main(String[] args) {
	}
}
