package edu.featjam.core;

public interface Classifier {
	void build(String category, DocumentSet docs);
	boolean classify(Document doc);
	String getCategory();
}
