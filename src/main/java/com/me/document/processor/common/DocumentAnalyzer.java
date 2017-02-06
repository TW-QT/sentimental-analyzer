package com.me.document.processor.common;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface DocumentAnalyzer {

	Map<String, Term> analyze(File file);
	List<Term> analyzeKeepSeq(File file);
}
