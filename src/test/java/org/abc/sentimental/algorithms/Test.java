package org.abc.sentimental.algorithms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Test {
	private static String path = "D:\\douban";
	private static String good = "D:\\train\\好评";
	private static String bad = "D:\\train\\差评";

	public static void main(String[] args) {
		int i = 1;
		int j = 1;
		for(File file: new File(path).listFiles()){
			BufferedReader br = null;
			BufferedWriter bw = null;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
				String line = null;
				while((line = br.readLine()) != null) {
					if(line.startsWith("(5星)	 ") || line.startsWith("(4星)	 ")){
						line = line.substring(line.indexOf("星)	 ") + "星)	 ".length());
						bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(good + File.separator + (i++) + ".txt")), "UTF-8"));
						bw.write(line);
					}else if(line.startsWith("(3星)	 ") || line.startsWith("(2星)	 ") || line.startsWith("(1星)	 ")){
						line = line.substring(line.indexOf("星)	 ") + "星)	 ".length());
						bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(bad + File.separator + (j++) + ".txt")), "UTF-8"));
						bw.write(line);
					}
					if(bw != null){
						bw.close();
					}
				}
				if(br != null){
					br.close();
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
