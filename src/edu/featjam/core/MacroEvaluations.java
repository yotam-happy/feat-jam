package edu.featjam.core;

import java.util.HashMap;
import java.util.Map;

public class MacroEvaluations {
	Map<String,Evaluations> experiments = new HashMap<>();
	
	public void addExperimentResults(String name, Evaluations evals){
		experiments.put(name, evals);
	}
	
	public double macroF(){
		return experiments.values().stream().mapToDouble((e)->e.fMeasure()).average().orElse(0);
	}
	public double microF(){
		Evaluations e = new Evaluations();
		experiments.forEach((expName,exp)->{
			exp.evaluations.forEach(
					(eName,ev)->e.add(
							expName + "_" + ev.doc.getName(), 
							new Evaluation(ev.doc, ev.predicted, ev.actual)));
		});
		return e.fMeasure();
	}
}
