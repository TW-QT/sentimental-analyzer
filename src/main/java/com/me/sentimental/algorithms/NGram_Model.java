package com.me.sentimental.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class NGram_Model {
	Map<String, Map<String, Integer>> termFreq = null;
	
	public Map<String, Map<String, Integer>> getTermFreq() {
		return termFreq;
	}

	public void setTermFreq(Map<String, Map<String, Integer>> termFreq) {
		this.termFreq = termFreq;
	}

	public NGram_Model(){
		termFreq = new HashMap<String, Map<String, Integer>>(); 
	}
	
	public void save_model(String model_file) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(new File(model_file)), "UTF-8"));
			Iterator<Entry<String, Map<String, Integer>>> termFreqIter = termFreq.entrySet().iterator();
			while(termFreqIter.hasNext()){
				Entry<String, Map<String, Integer>> entry = termFreqIter.next();
				
				Iterator<Entry<String, Integer>> entryIter = entry.getValue().entrySet().iterator();
				while(entryIter.hasNext()){
					Entry<String, Integer> inEntry = entryIter.next();
					bw.write(entry.getKey() + " " + inEntry.getKey() + ":" + inEntry.getValue());
					bw.newLine();
				}
			}
			
		}catch (Exception e) {
			
		}finally{
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
