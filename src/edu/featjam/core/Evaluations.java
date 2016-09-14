package edu.featjam.core;

import java.util.HashMap;
import java.util.Map;

public class Evaluations {
	Map<String, Evaluation> evaluations = new HashMap<>();
	
	public static Evaluations evaluate(DocumentSet docs, Classifier classifier){
		Evaluations evaluations = new Evaluations();
		docs.forEach((id,doc)->{
			boolean actual = doc.getCategories().contains(classifier.getCategory());
			boolean predicted = classifier.classify(doc);
			evaluations.add(new Evaluation(doc, predicted, actual));
		});
		return evaluations;
	}
	
	public void add(String name, Evaluation eval){
		evaluations.put(name, eval);
	}
	public void add(Evaluation eval){
		evaluations.put(eval.doc.getName(), eval);
	}
	public int size(){
		return evaluations.size();
	}
	public double countTP(){
		return evaluations.values().stream().mapToDouble((e)->e.actual && e.predicted ? 1.0 : 0.0).sum();
	}
	public double countFP(){
		return evaluations.values().stream().mapToDouble((e)->!e.actual && e.predicted ? 1.0 : 0.0).sum();
	}
	public double countTN(){
		return evaluations.values().stream().mapToDouble((e)->!e.actual && !e.predicted ? 1.0 : 0.0).sum();
	}
	public double countFN(){
		return evaluations.values().stream().mapToDouble((e)->e.actual && !e.predicted ? 1.0 : 0.0).sum();
	}
	
	public double precision(){
		double tp = countTP();
		double fp = countFP();
		return tp + fp == 0 ? 1 : tp / (tp + fp);
	}
	public double recall(){
		double tp = countTP();
		double fn = countFN();
		return tp + fn == 0 ? 1 : tp / (tp + fn);
	}
	public double fMeasure(){
		double f = (2 * precision() * recall()) / (precision() + recall());
		return Double.isFinite(f) ? f : 0;
	}
	public double rate(){
		return (countTP()+countTN())/evaluations.size();
	}
}
