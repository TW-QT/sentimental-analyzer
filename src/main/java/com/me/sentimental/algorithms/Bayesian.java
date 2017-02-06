package com.me.sentimental.algorithms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class Bayesian {
	private boolean isClassfication[];  //用于对属性的离散还是连续做判断
	
	private ArrayList<Double> lblClass = new ArrayList<Double>(); // 存储目标值的类别
	private ArrayList<Integer> lblCount = new ArrayList<Integer>(); // 存储每个类别的样本个数
	private ArrayList<Float> lblProba = new ArrayList<Float>(); // 存储每个类别占样本总数的概率（先验概率）
	private CountProbility countlblPro;
	
	/* @ClassListBasedLabel是将训练数组按照 label的顺序来分类存储 */
	private ArrayList<ArrayList<ArrayList<Double>>> ClassListBasedLabel = new ArrayList<ArrayList<ArrayList<Double>>>();

	public Bayesian() {
	}
	
	

	/**
	 * @train主要完成求一些概率
	 * 1.labels中的不同取值的概率f(Yi);
	 * 2.将训练数组按目标值分类存储
	 * isCategory:对应着每一个label，表示其是否为离散或连续，true or false
	 * */
	public void train(boolean[] isCategory, double[][] features, double[] labels) {
		isClassfication = isCategory;
		countlblPro = new CountProbility(isCategory, features, labels);
		countlblPro.getlblClass(lblClass, lblCount, lblProba);
		ArrayList<ArrayList<Double>> trainingList = countlblPro.UnionFeaLbl(
				features, labels); // union the features[][] and labels[]
		ClassListBasedLabel = countlblPro.getClassListBasedLabel(lblClass,
				trainingList);
	}
	
	
	
	public void load_stat_data(String train_stat_file_name) throws FileNotFoundException{
		BufferedReader br = new BufferedReader(new FileReader(train_stat_file_name));
		
	}
	
	/**3.在Y的条件下，计算Xi的概率 f(Xi/Y)；
	 * 4.返回使得Yi*Xi*…概率最大的那个label的取值
	 * */
	public double predict(double[] features) {
		int max_index; // 用于记录使概率取得最大的那个索引
		int index = 0; // 这个索引是 标识不同的labels 所对应的概率
		ArrayList<Double> pro_ = new ArrayList<Double>(); // 这个概率数组是存储features[]

		for (ArrayList<ArrayList<Double>> elements : ClassListBasedLabel) // 依次取不同的label值对应的元祖集合
		{
			ArrayList<Double> pro = new ArrayList<Double>(); // 存同一个label对应的所有概率，之后其中的元素自乘
			double probility = 1.0; // 计算概率的乘积
			for (int i = 0; i < features.length; i++) {
				if (isClassfication[i]) // 用于对属性的离散还是连续做判断
				{
					int count = 0;
					for (ArrayList<Double> element : elements) // 依次取labels中的所有元祖
					{
						if (element.get(i).equals(features[i])) // 如果这个元祖的第index数据和b相等，那么就count就加1
							count++;
					}
					if (count == 0) {
						pro.add(1 / (double) (elements.size() + 1));
					} else
						pro.add(count / (double) elements.size()); // 统计完所有之后
				} else {
					double Sdev;
					double Mean;
					double probi = 1.0;
					Mean = countlblPro.getMean(elements, i);

					Sdev = countlblPro.getSdev(elements, i);
					if (Sdev != 0) {
						probi *= ((1 / (Math.sqrt(2 * Math.PI) * Sdev)) * (Math
								.exp(-(features[i] - Mean)
										* (features[i] - Mean)
										/ (2 * Sdev * Sdev))));
						pro.add(probi);
					} else
						pro.add(1.5);
				}
			}

			for (double pi : pro)
				probility *= pi; // 将所有概率相乘
			probility *= lblProba.get(index); // 最后再乘以一个 Yi
			pro_.add(probility); // 放入pro_ 至此 一个循环结束，
			index++;
		}
		double max_pro = pro_.get(0);
		max_index = 0;

		for (int i = 1; i < pro_.size(); i++) {
			if (pro_.get(i) >= max_pro) {
				max_pro = pro_.get(i);
				max_index = i;
			}
		}

		return lblClass.get(max_index);
	}

	public class CountProbility {
		boolean[] isCatory;
		double[][] features;
		private double[] labels;

		public CountProbility(boolean[] isCategory, double[][] features,
				double[] labels) {
			this.isCatory = isCategory;
			this.features = features;
			this.labels = labels;
		}

		// 获取label中取值情况
		public void getlblClass(ArrayList<Double> lblClass,
				ArrayList<Integer> lblCount, ArrayList<Float> lblProba) {
			int j = 0;
			for (double i : labels) {
				// 如果当前的label不存在于lblClass则加入
				if (!lblClass.contains(i)) {
					lblClass.add(j, i);
					lblCount.add(j++, 1);
				}

				else // 如果label中已经存在，就将其计数加1
				{
					int index = lblClass.indexOf(i);
					int count = lblCount.get(index);
					lblCount.set(index, ++count);
				}
			}

			for (int i = 0; i < lblClass.size(); i++) {
				// System.out.println("值为"+lblClass.get(i)+"的个数有"+lblCount.get(i)+"概率是"+lblCount.get(i)/(float)labels.length);
				lblProba.add(i, lblCount.get(i) / (float) labels.length);
			}
		}

		// 将label[]和features[][]合并
		public ArrayList<ArrayList<Double>> UnionFeaLbl(double[][] features,
				double[] labels) {
			ArrayList<ArrayList<Double>> traingList = new ArrayList<ArrayList<Double>>();

			for (int i = 0; i < features.length; i++) {
				ArrayList<Double> elements = new ArrayList<Double>();
				for (int j = 0; j < features[i].length; j++) {
					elements.add(j, features[i][j]);
				}

				elements.add(features[i].length, labels[i]);
				traingList.add(i, elements);
			}
			return traingList;
		}

		/* 将测试数组按label的值分类存储 */
		public ArrayList<ArrayList<ArrayList<Double>>> getClassListBasedLabel(
				ArrayList<Double> lblClass,
				ArrayList<ArrayList<Double>> trainingList) {
			ArrayList<ArrayList<ArrayList<Double>>> ClassListBasedLabel = new ArrayList<ArrayList<ArrayList<Double>>>();

			for (double num : lblClass) {
				ArrayList<ArrayList<Double>> elements = new ArrayList<ArrayList<Double>>();
				for (ArrayList<Double> element : trainingList) {
					if (element.get(element.size() - 1).equals(num))
						elements.add(element);
				}
				ClassListBasedLabel.add(elements);
			}

			return ClassListBasedLabel;
		}

		public double getMean(ArrayList<ArrayList<Double>> elements, int index) {
			double sum = 0.0;
			double Mean;

			for (ArrayList<Double> element : elements) {
				sum += element.get(index);
			}

			Mean = sum / (double) elements.size();
			return Mean;
		}

		public double getSdev(ArrayList<ArrayList<Double>> elements, int index) {
			double dev = 0.0;
			double Mean;

			Mean = getMean(elements, index);

			for (ArrayList<Double> element : elements) {
				dev += Math.pow((element.get(index) - Mean), 2);
			}

			dev = Math.sqrt(dev / elements.size());
			return dev;
		}
	}
	
	public static void main(String[] args) {
//		boolean[] isCategory = new boolean[]{false,true,true,false};
//		double[][] features = new double[][]{{1,2,3},{6,3,8},{3,1,4},{3,4,1}};
//		double[] labels = new double[]{1,0,1,1};
//		
//		Bayesian gb = new Bayesian();
//		gb.train(isCategory, features, labels);
//		gb.predict(new double[]{3,2,5});
		
//		Bayesian bayes = new Bayesian();
	}




	public Bayesian_Model train(
			Map<Integer, List<Map<Integer, Double>>> train_data) {
		Bayesian_Model model = new Bayesian_Model();
		int totalDocCount = 0;
		Iterator<Entry<Integer, List<Map<Integer,Double>>>> dataIter = train_data.entrySet().iterator();
		while(dataIter.hasNext()){
			Entry<Integer, List<Map<Integer,Double>>> entry = dataIter.next();
			
			model.labelProba.put(entry.getKey(), (double) entry.getValue().size());
			totalDocCount += entry.getValue().size();
			
			Map<Integer, Double> mean = getMean(entry.getValue());
			Map<Integer, Double> sDev = getSDev(entry.getValue(), mean);
			
			model.meanVector.put(entry.getKey(), mean);
			model.sDevVector.put(entry.getKey(), sDev);
		}
		
		Iterator<Entry<Integer, Double>> labelProbaIter = model.labelProba.entrySet().iterator();
		while(labelProbaIter.hasNext()){
			Entry<Integer, Double> entry = labelProbaIter.next();
			entry.setValue(entry.getValue() / totalDocCount);
		}
		return model;
	}
	
	private Map<Integer, Double> getSDev(List<Map<Integer, Double>> list,
			Map<Integer, Double> mean) {
		Map<Integer, Double> sDev = new HashMap<Integer, Double>();
		Iterator<Map<Integer, Double>> iter = list.iterator();
		double dev = 0.0;
		while(iter.hasNext()){
			Iterator<Entry<Integer, Double>> entryIter = iter.next().entrySet().iterator();
			while(entryIter.hasNext()){
				Entry<Integer, Double> entry = entryIter.next();
				dev = Math.pow(entry.getValue() - mean.get(entry.getKey()), 2);
				if(sDev.containsKey(entry.getKey())){
					sDev.put(entry.getKey(), sDev.get(entry.getKey()) + dev);
				}else{
					sDev.put(entry.getKey(), dev);
				}
			}
		}
		
		Iterator<Entry<Integer, Double>> sDevIter = sDev.entrySet().iterator();
		while(sDevIter.hasNext()){
			Entry<Integer, Double> entry = sDevIter.next();
			entry.setValue(Math.sqrt(entry.getValue() / list.size()));
		}
		return sDev;
	}

	private Map<Integer, Double> getMean(List<Map<Integer, Double>> list) {
		Map<Integer, Double> mean = new HashMap<Integer, Double>();
		Iterator<Map<Integer, Double>> iter = list.iterator();
		while(iter.hasNext()){
			Iterator<Entry<Integer, Double>> entryIter = iter.next().entrySet().iterator();
			while(entryIter.hasNext()){
				Entry<Integer, Double> entry = entryIter.next();
				if(mean.containsKey(entry.getKey())){
					mean.put(entry.getKey(), mean.get(entry.getKey()) + entry.getValue() / list.size());
				}else{
					mean.put(entry.getKey(), entry.getValue() / list.size());
				}
			}
		}
		return mean;
	}
}