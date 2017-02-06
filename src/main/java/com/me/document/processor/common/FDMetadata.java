package com.me.document.processor.common;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.me.document.processor.config.Configuration;
import com.me.document.processor.utils.CheckUtils;

public class FDMetadata {
	
	private static final Log LOG = LogFactory.getLog(FDMetadata.class);
	private final File inputRootDir;//训练数据或测试数据的根目录，即类标签所在目录的父目录
	private final File outputDir;//训练数据或测试数据，数据预处理后的结果保存目录
	private final String outputVectorFile;//训练数据或测试数据，数据预处理后的结果保存文件
	private final String fileExtensionName;//数据集文件扩展名
	private final File labelVectorFile;
	private final File chiTermVectorFile;//开方检验特征词向量
	
	public FDMetadata(ProcessorType processorType, Configuration configuration) {
		// initialize
		fileExtensionName = configuration.get("processor.dataset.file.extension", "");
		LOG.info("Train dataset file extension: name=" + fileExtensionName);
		String termsFile = configuration.get("processor.dataset.chi.term.vector.file");
		CheckUtils.checkNotNull(termsFile);
		chiTermVectorFile = new File(termsFile);
		
		if(processorType == ProcessorType.TRAIN) {
			String trainInputRootDir = configuration.get("processor.dataset.train.input.root.dir");
			String train = configuration.get("processor.dataset.train.svm.vector.file");
			String trainOutputDir = configuration.get("processor.dataset.train.svm.vector.output.dir");
			
			inputRootDir = new File(trainInputRootDir);
			outputVectorFile = train;
			outputDir = new File(trainOutputDir);
			
			// check existence: 
			// parent directory of term file MUST exist
//			CheckUtils.checkFile(chiTermVectorFile.getParentFile(), false);
			// term file MUST NOT exist
//			CheckUtils.checkFile(chiTermVectorFile, true);			
		} else if(processorType == ProcessorType.TEST) {
			String testInputRootDir = configuration.get("processor.dataset.test.input.root.dir");
			String test = configuration.get("processor.dataset.test.vector.file");
			String testOutputDir = configuration.get("processor.dataset.test.vector.output.dir");
			String stat = configuration.get("processor.dataset.test.stat");
			
			inputRootDir = new File(testInputRootDir);
			outputVectorFile = test;
			outputDir = new File(testOutputDir);
			
			CheckUtils.checkFile(chiTermVectorFile, false);
		} else {
			throw new RuntimeException("Undefined processor type!");
		}
		
		String labels = configuration.get("processor.dataset.label.vector.file");
		labelVectorFile = new File(labels);
		
		LOG.info("Vector input root directory: inputRootDir=" + inputRootDir);
		LOG.info("Vector output directory: outputDir=" + outputDir);
		LOG.info("Vector output file: outputFile=" + outputVectorFile);
	}

	public File getInputRootDir() {
		return inputRootDir;
	}

	public File getOutputDir() {
		return outputDir;
	}

	public String getOutputVectorFile() {
		return outputVectorFile;
	}

	public String getFileExtensionName() {
		return fileExtensionName;
	}

	public File getLabelVectorFile() {
		return labelVectorFile;
	}

	public File getChiTermVectorFile() {
		return chiTermVectorFile;
	}
}
