package com.me.document.processor;


import com.me.document.processor.common.Component;
import com.me.document.processor.common.Context;
import com.me.document.processor.common.ProcessorType;
import com.me.document.processor.component.BasicInformationCollector;
import com.me.document.processor.component.DocumentTFIDFComputation;
import com.me.document.processor.component.DocumentWordsCollector;
import com.me.document.processor.component.train.FeatureTermVectorSelector;
import com.me.document.processor.component.train.OutputtingQuantizedTrainData;

/**
 * The driver for starting components to process TRAIN data set.
 * It includes the following 5 components:
 * <ol>
 * <li>{@link BasicInformationCollector}</li>
 * <li>{@link DocumentWordsCollector}</li>
 * <li>{@link FeatureTermVectorSelector}</li>
 * <li>{@link DocumentTFIDFComputation}</li>
 * <li>{@link OutputtingQuantizedTrainData}</li>
 * </ol>
 * Executing above components in order can output the normalized
 * data for feeding libSVM classifier developed by <code>Lin Chih-Jen</code>
 * (<a href="www.csie.ntu.edu.tw/~cjlin/libsvm/‎">www.csie.ntu.edu.tw/~cjlin/libsvm/‎</a>)</br>
 * It can produce 2 files represented by the specified properties:
 * <ol>
 * <li>a term vector file property: <code>processor.dataset.chi.term.vector.file</code></li>
 * <li>a label vector file property: <code>processor.dataset.label.vector.file</code></li>
 * </ol>
 * which are used by {@link TestDocumentProcessorDriver} to produce TEST vector data.
 * 
 * @author me
 */
public class TrainDocumentProcessorDriver extends AbstractDocumentProcessorDriver {

	@Override
	public void process() {
		Context context = new Context(ProcessorType.TRAIN, "config-train.properties");
		// for train data
		Component[]	chain = new Component[] {
				new BasicInformationCollector(context),/*统计样本量、类别标签、（类别、样本量）*/
				new DocumentWordsCollector(context),/*分词，词性过滤、单字过滤、停用词过滤，（标签、文档、词集信息），（词，标签、文档）*/
				new FeatureTermVectorSelector(context), /*特征词选取*/
				new DocumentTFIDFComputation(context),/*TF-idf权重计算*/
				new OutputtingQuantizedTrainData(context)/*输出量化后的训练数据*/
			};
		run(chain);
	}
	
	public static void main(String[] args) {
		AbstractDocumentProcessorDriver.start(
				TrainDocumentProcessorDriver.class);	
	}
}
