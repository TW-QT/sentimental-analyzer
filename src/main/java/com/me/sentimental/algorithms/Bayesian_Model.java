package com.me.sentimental.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Bayesian_Model {
	//Map<类别, 先验概率>
	Map<Integer, Double> labelProba = null;
	//Map<类别, Map<词, 均值>>
	Map<Integer, Map<Integer, Double>> meanVector = null;
	
	//Map<类别, Map<词, 方差>>
	Map<Integer, Map<Integer, Double>> sDevVector = null;
	
	public Bayesian_Model() {
		labelProba = new HashMap<Integer, Double>();
		meanVector = new HashMap<Integer, Map<Integer, Double>>();
		sDevVector = new HashMap<Integer, Map<Integer, Double>>();
	}
	
	public void save_model(String modelSavePath) {
		BufferedWriter bw = null;
		
		try {
			bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(new File(modelSavePath)), "UTF-8"));
			
			//写先验概率
			Iterator<Entry<Integer, Double>> labelProbaIter = labelProba.entrySet().iterator();
			while(labelProbaIter.hasNext()){
				Entry<Integer, Double> entry = labelProbaIter.next();
				bw.write(entry.getKey() +":" +entry.getValue());
				bw.newLine();
			}
			
			//写后验概率
			Iterator<Entry<Integer, Map<Integer, Double>>> meanVecorIter = meanVector.entrySet().iterator();
			while(meanVecorIter.hasNext()){
				Entry<Integer, Map<Integer, Double>> entry1 = meanVecorIter.next();
				Iterator<Entry<Integer, Double>> entry1Iter = entry1.getValue().entrySet().iterator();
				while(entry1Iter.hasNext()){
					Entry<Integer, Double> entry2 = entry1Iter.next();
					bw.write(entry1.getKey() + " " + entry2.getKey() + ":" + entry2.getValue() + " " + sDevVector.get(entry1.getKey()).get(entry2.getKey()));
					bw.newLine();
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
