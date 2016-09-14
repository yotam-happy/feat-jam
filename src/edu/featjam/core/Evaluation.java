package edu.featjam.core;

public class Evaluation {
	public Document doc;
	public boolean predicted;
	public boolean actual;
	
	public Evaluation(Document doc, boolean predicted, boolean actual){
		this.doc = doc;
		this.actual = actual;
		this.predicted = predicted;
	}
}