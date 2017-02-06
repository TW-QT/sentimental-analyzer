package com.me.sentimental.algorithms;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class NGram_Train {
	Map<String, List<List<String>>> train_data = new HashMap<String, List<List<String>>>(); 
	
	NGram_Model model;
	/**
	 * argv[]{train_file, doCross, model_file}
	 * @param argv
	 * @throws IOException
	 */
	public static void main(String argv[]) throws IOException
	{
		NGram_Train t = new NGram_Train();
		String[] params = new String[]{"output\\train\\ngram-train.txt"};
		t.run(params);
	}

	private void run(String[] argv) {
		if(argv.length <= 0){
			System.err.print("ERROR: error parameters\n");
			System.exit(1);
		}
		load_train_data(argv[0]);
		if(argv.length == 1){
			//交叉验证
			do_cross_validation();
		}else if(argv.length == 2){
			//训练得到模型
			model = new NGram().train(train_data);
			model.save_model(argv[1]);
		}
	}
	
	private void do_cross_validation() {
		List<Map<String, List<List<String>>>> tenMaps = new ArrayList<Map<String, List<List<String>>>>();
		int totalCount = getTotalCount(train_data);
		int eachMapSize = totalCount / 10;
		for (int i = 0; i < 9; i++) {
			tenMaps.add(generateOneMap(train_data, eachMapSize));
		}
		tenMaps.add(train_data);
		System.out.println("NGram算法：");
		for (int i = 0; i < 10; i++) {
			model = new NGram().train(generateTrainData(tenMaps, i));
			
			System.out.println("第"+i+"重验证，precision = " + (1 - validate(tenMaps.get(i))));
		}
	}
	
	private double validate(Map<String, List<List<String>>> data) {
		int totalCount = getTotalCount(data);
		int precision = 0;
		String[] labels = data.keySet().toArray(new String[]{});
		for (int i = 0; i < labels.length; i++) {
			Iterator<List<String>> iter = data.get(labels[i]).iterator();
			while(iter.hasNext()){
				if(predictOne(iter.next(), labels[i])){
					precision++;
				}
			}
		}
		return precision * 1.0 / totalCount;
	}

	private boolean predictOne(List<String> one, String trueLabel) {
		Iterator<String> iter = one.iterator();
		String last = null;
		String[] labels = model.getTermFreq().keySet().toArray(new String[]{});
		double maxPro = 0;
		int maxLabel = -1;
		
		
		for (int i = 0; i < labels.length; i++) {
			double p = 1;
			while(iter.hasNext()){
				String term = iter.next();
				if(last != null){
					int count = 1;
					if(model.termFreq.get(labels[i]).containsKey(last + "#" +term)){
						count += model.termFreq.get(labels[i]).get(last + "#" +term);
					}
					int pre = 1;
					if(model.termFreq.get(labels[i]).containsKey(last)){
						pre += model.termFreq.get(labels[i]).get(last);
					}else{
						pre += model.termFreq.get(labels[i]).size();
					}
					p *= count * 1.0 / pre;
				}
				last = term;
			}
			if(p > maxPro){
				maxPro = p;
				maxLabel = i;
			}
		}
		
		return labels[maxLabel].equals(trueLabel);
	}

	private Map<String, List<List<String>>> generateTrainData(
			List<Map<String, List<List<String>>>> tenMaps, int indexTest) {
		Map<String, List<List<String>>> data = new HashMap<String, List<List<String>>>(); 
		for (int j = 0; j < 10; j++) {
			if(j != indexTest){
				combine(data, tenMaps.get(j));
			}
		}
		return data;
	}

	private void combine(Map<String, List<List<String>>> data,
			Map<String, List<List<String>>> map) {
		String[] labels = map.keySet().toArray(new String[]{});
		for (int i = 0; i < labels.length; i++) {
			if(data.containsKey(labels[i])){
				data.get(labels[i]).addAll(map.get(labels[i]));
			}else{
				data.put(labels[i], map.get(labels[i]));
			}
		}
	}

	private Map<String, List<List<String>>> generateOneMap(
			Map<String, List<List<String>>> data, int eachMapSize) {
		Map<String, List<List<String>>> oneMap = new HashMap<String, List<List<String>>>();
		String[] labels = data.keySet().toArray(new String[]{});
		Random rand = new Random();
		
		for (int i = 0; i < eachMapSize; i++) {
			int x = rand.nextInt(labels.length);
			if(data.containsKey(labels[x]) && data.get(labels[x]).size() == 1){
				List<List<String>> docs = oneMap.get(labels[x]);
				if(docs == null){
					docs = new LinkedList<List<String>>();
					oneMap.put(labels[x], docs);
				}
				docs.add(data.get(labels[x]).remove(0));
				data.remove(labels[x]);
			}else if(data.containsKey(labels[x]) && data.get(labels[x]).size() > 1){
				int y = rand.nextInt(data.get(labels[x]).size());
				
				List<List<String>> docs = oneMap.get(labels[x]);
				if(docs == null){
					docs = new LinkedList<List<String>>();
					oneMap.put(labels[x], docs);
				}
				docs.add(data.get(labels[x]).remove(y));
			}
		}
		return oneMap;
	}

	private int getTotalCount(Map<String, List<List<String>>> data) {
		int totalCount = 0;
		String[] labels = data.keySet().toArray(new String[]{});
		for (int i = 0; i < labels.length; i++) {
			totalCount += data.get(labels[i]).size();
		}
		return totalCount;
	}

	private void load_train_data(String train_file) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(train_file));
			String line = null;
			while((line = br.readLine()) != null){
				if(!line.equals("")){
					String[] arry = line.split(" ");
					List<List<String>> docs = new LinkedList<List<String>>();
					List<String> doc = null;
					for (int i = 1; i < arry.length; i++) {
						if(arry[i].equals("$")){
							if(doc != null){
								docs.add(doc);
							}
							doc = new LinkedList<String>();
						}
						doc.add(arry[i]);
					}
					train_data.put(arry[0], docs);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
