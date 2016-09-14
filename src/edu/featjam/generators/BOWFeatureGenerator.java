package edu.featjam.generators;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import edu.featjam.core.DocumentSet;
import weka.core.stemmers.SnowballStemmer;
import weka.core.stemmers.Stemmer;
import weka.core.tokenizers.NGramTokenizer;

public class BOWFeatureGenerator {
	
	public static String defaultFeatureSet(){
		return "bow";
	}
	
	public static String defaultFeatureSet(int ngram){
		return "bow" + (ngram == 1 ? "" : "_" + ngram + "gram"); 
	}
	
	private int maxNGram;
	Set<String> stopWords = new HashSet<>();
	Stemmer stemmer;

	public BOWFeatureGenerator(int maxNGram, String stopWordsFileName) {
		this.maxNGram = maxNGram;
		this.stemmer = new SnowballStemmer();
		stopWords = getStopWords(stopWordsFileName);
	}
	
	public void apply(DocumentSet documents){
		documents.forEach((id, doc)->{
			for(int i = 1; i <= maxNGram; i++){
				doc.addFeatureSet(defaultFeatureSet(i));
			}
			forEachNGram(doc.getText(), maxNGram, (ngram, ngramSz)->{
				doc.getFeatureSet(defaultFeatureSet(ngramSz)).put(ngram, 1, (x)->x+1);
			});
			return;
		});
	}

	private boolean tokenValidation(String token) {
		if(stopWords.contains(token)){
			return false;
		}
		if(token.matches(".*\\d+.*")){
			return false;
		}
		return true;
	}
	
	public void forEachNGram(String text, int maxNGram, BiConsumer<String,Integer> f){
		String[] cyclicArr = new String[maxNGram-1];
		int cyclicArrPos = 0;

		// Set the tokenizer 
		NGramTokenizer tokenizer = new NGramTokenizer(); 
		tokenizer.setNGramMinSize(1); 
		tokenizer.setNGramMaxSize(1); 
		tokenizer.setDelimiters("\\W");
		tokenizer.tokenize(text);

		while (tokenizer.hasMoreElements()) {
			String token = ((String)tokenizer.nextElement()).toLowerCase();
			if(!tokenValidation(token)){
				continue;
			}
			String stemmed = stemmer.stem(token);
			if (stemmed.isEmpty()) {
				continue;
			}
			
			f.accept(stemmed,1);

			// Add NGrams to BOW
			for (int i = 0; i < maxNGram; i++) {
				String ngram = stemmed;
				for (int j = 0; j < i - 1; j++) {
					ngram+= "_" + cyclicArr[(cyclicArrPos - j) % maxNGram];
					
					f.accept(ngram, j+1);
				}
			}

			// Add to cyclic array (for n-gram)
			if (maxNGram > 1) {
				cyclicArr[cyclicArrPos] = stemmed;
				cyclicArrPos = (cyclicArrPos++) % maxNGram;
			}
		}
	}
	
	private static Set<String> getStopWords(String stopWordsFileName){
		try {
			List<String> lines = Files.readAllLines(Paths.get(stopWordsFileName), Charset.defaultCharset());
			Set<String> stopWords = new HashSet<>();
			lines.forEach((line)->stopWords.add(line.trim()));
			return stopWords;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
