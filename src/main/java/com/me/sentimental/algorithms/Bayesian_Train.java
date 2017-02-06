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
import java.util.Map.Entry;
import java.util.Random;
import java.util.StringTokenizer;

import libsvm.svm;

public class Bayesian_Train {
	Bayesian_Model model;
	
	//Map<类别, List<Map<词序, 词权重>>>
	Map<Integer, List<Map<Integer,Double>>> train_data = new HashMap<Integer, List<Map<Integer, Double>>>();

	/**
	 * argv[]{train_file, doCross, model_file}
	 * @param argv
	 * @throws IOException
	 */
	public static void main(String argv[]) throws IOException
	{
		Bayesian_Train t = new Bayesian_Train();
		String[] params = new String[]{"output\\train\\train.txt"};
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
			model = new Bayesian().train(train_data);
			model.save_model(argv[1]);
		}
	}
	
	private void do_cross_validation() {
		List<Map<Integer, List<Map<Integer,Double>>>> tenMaps = new ArrayList<Map<Integer, List<Map<Integer,Double>>>>();
		int totalCount = getTotalCount(train_data);
		int eachMapSize = totalCount / 10;
		for (int i = 0; i < 9; i++) {
			tenMaps.add(generateOneMap(train_data, eachMapSize));
		}
		tenMaps.add(train_data);
		
		for (int i = 0; i < 10; i++) {
			model = new Bayesian().train(generateTrainData(tenMaps, i));
			System.out.println("朴素贝叶斯算法：");
			System.out.println("第"+i+"重验证，precision = " + validate(tenMaps.get(i)));
		}
	}

	private double validate(Map<Integer, List<Map<Integer, Double>>> map) {
		int totalCount = 0;
		int precisionCount = 0;
		
		Iterator<Entry<Integer, List<Map<Integer, Double>>>> mapIter = map.entrySet().iterator();
		while(mapIter.hasNext()){
			Entry<Integer, List<Map<Integer, Double>>> entry = mapIter.next();
			totalCount += entry.getValue().size();
			Iterator<Map<Integer, Double>> listIter = entry.getValue().iterator();
			while(listIter.hasNext()){
				if(validateOne(listIter.next(), entry.getKey())){
					precisionCount += 1;
				}
			}
		}
		return precisionCount * 1.0 / totalCount;
	}

	private boolean validateOne(Map<Integer, Double> one, Integer trueLabel) {
		Integer predLabel = -1;
		double maxPro = 0;
		Integer[] labels = model.labelProba.keySet().toArray(new Integer[]{});
		for (Integer label: labels) {
			double p = model.labelProba.get(label);
			Iterator<Entry<Integer, Double>> oneIter = one.entrySet().iterator();
			while(oneIter.hasNext()){
				Entry<Integer, Double> oneEntry = oneIter.next();
				if(model.meanVector.get(label).containsKey(oneEntry.getKey())){
					p *= GaussPro(oneEntry.getValue(), model.meanVector.get(label).get(oneEntry.getKey()), model.sDevVector.get(label).get(oneEntry.getKey()));
				}else{
					p *= 0.1;
				}
				
			}
			if(p > maxPro){
				maxPro = p;
				predLabel = label;
			}
		}
		return predLabel == trueLabel;
	}

	private double GaussPro(double x, double mean, double sDev) {
		return ((1 / (Math.sqrt(2 * Math.PI) * sDev)) * (Math
				.exp(-(x - mean)
						* (x - mean)
						/ (2 * sDev * sDev))));
	}

	private Map<Integer, List<Map<Integer, Double>>> generateTrainData(
			List<Map<Integer, List<Map<Integer, Double>>>> tenMaps, int testIndex) {
		Map<Integer, List<Map<Integer,Double>>> train_data_combined = new HashMap<Integer, List<Map<Integer, Double>>>();
		for (int i = 0; i < 10; i++) {
			if(i != testIndex){
				combineTwoData(train_data_combined, tenMaps.get(i));
			}
		}
		return train_data_combined;
	}

	private void combineTwoData(
			Map<Integer, List<Map<Integer, Double>>> train_data_combined,
			Map<Integer, List<Map<Integer, Double>>> map) {
		Iterator<Entry<Integer, List<Map<Integer, Double>>>> mapIter = map.entrySet().iterator();
		while(mapIter.hasNext()){
			Entry<Integer, List<Map<Integer, Double>>> entry = mapIter.next();
			if(train_data_combined.containsKey(entry.getKey())){
				train_data_combined.get(entry.getKey()).addAll(entry.getValue());
			}else{
				train_data_combined.put(entry.getKey(), entry.getValue());
			}
		}
	}

	private Map<Integer, List<Map<Integer, Double>>> generateOneMap(
			Map<Integer, List<Map<Integer, Double>>> data, int eachMapSize) {
		Map<Integer, List<Map<Integer, Double>>> oneMap = new HashMap<Integer, List<Map<Integer, Double>>>();
		
		Random rand = new Random();
		for (int i = 0; i < eachMapSize; ) {
			Integer[] labels = data.keySet().toArray(new Integer[]{});
			int x = rand.nextInt(data.size());
			if(data.get(labels[x]).size() == 1){
				if(oneMap.containsKey(labels[x])){
					oneMap.get(labels[x]).add(data.get(labels[x]).get(0));
				}else{
					oneMap.put(labels[x], data.get(labels[x]));
				}
				i++;
				data.remove(labels[x]);
			}else if(data.get(labels[x]).size() > 1){
				int y = rand.nextInt(data.get(labels[x]).size());
				if(oneMap.containsKey(labels[x])){
					oneMap.get(labels[x]).add(data.get(labels[x]).remove(y));
				}else{
					List<Map<Integer, Double>> list = new ArrayList<Map<Integer, Double>>();
					list.add(data.get(labels[x]).remove(y));
					oneMap.put(labels[x], list);
				}
				i++;
			}
			
			
		}
		return oneMap;
	}

	private int getTotalCount(
			Map<Integer, List<Map<Integer, Double>>> data) {
		int totalCount = 0;
		Iterator<Entry<Integer, List<Map<Integer, Double>>>> iter = data.entrySet().iterator();
		while(iter.hasNext()){
			totalCount += iter.next().getValue().size();
		}
		return totalCount;
	}

	public void load_train_data(String train_file_name) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(train_file_name));
			String line = null;
			while((line = br.readLine()) != null){
				StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");
				Integer label = Integer.valueOf(st.nextToken());
				
				int m = st.countTokens()/2;
				Map<Integer, Double> terms = new HashMap<Integer, Double>();
				for(int j = 0; j < m; j++){
					terms.put(Integer.valueOf(st.nextToken()), Double.valueOf(st.nextToken()));
				}
				
				if(!train_data.containsKey(label)){
					train_data.put(label, new LinkedList<Map<Integer,Double>>());
				}
				train_data.get(label).add(terms);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
