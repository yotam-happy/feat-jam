package edu.featjam.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.featjam.util.Tuple;

public interface Experiment {
	/**
	 * Contract:
	 * - Loads data
	 * - Does any one time processing that does not depend on individual experiments
	 * 	 that follow
	 */
	public Tuple<DocumentSet, DocumentSet> loadDataSet();
	
	/**
	 * Contract:
	 * - Can expect to get it's own copy of the dataset. can manipulate this copy in any way
	 * - Expected to do any processing that is dependent on individual experiment (such as
	 *   feature selection that is category dependent)
	 */
	public void processData(DocumentSet trainingSet, DocumentSet testingSet);
	
	/**
	 * Contract:
	 * - Expected to be state-less! (Should not change the underlying object, or datasets)
	 * - Should not change the dataset! any per task processing should be done in processData
	 * - Should just induce a classifier
	 * - Should not do any parameter selection, this should be implemented in doExperiment
	 * - might be run multiple times (for example by classifier parameter tuning)
	 */
	public Classifier buildClassifier(DocumentSet trainingSet);
	
	/**
	 * Runs a full experiment.
	 * - Expected to apply processData on data if relevant
	 * - Expected to do any parameter search
	 * - Can change state of object and/or datasets
	 */
	public Evaluations doExperiment(DocumentSet trainingSet, DocumentSet testingSet);
	
	/**
	 * Runs the underlying experiment on folds and reports average evaluation metric
	 * on folds
	 */
	default double crossValidation(Set<Tuple<DocumentSet, DocumentSet>> folds, boolean parallel){
		Stream<Tuple<DocumentSet, DocumentSet>> stream = 
				parallel ? folds.parallelStream() : folds.stream();
		
		List<Double> results = stream.map((fold)->{
			DocumentSet training = fold.x;
			DocumentSet testing = fold.y;
			Classifier classifier = buildClassifier(training);
			double s = evaluationMetric(evaluate(testing, classifier));
			return s;
		}).collect(Collectors.toList());
		return results.stream().mapToDouble((d)->Double.isNaN(d)?0:d).average().orElse(0);
	}

	/**
	 * Implements the main evaluation metric for other classes to assess parameter tuning
	 */
	default double evaluationMetric(Evaluations evals) {
		return evals.fMeasure();
	}

	/**
	 * Utility method. Maybe it shouldn't be here...
	 */
	default Evaluations evaluate(DocumentSet testingSet, Classifier classifier) {
		return Evaluations.evaluate(testingSet, classifier);
	}
	
	default Tuple<DocumentSet, DocumentSet> loadDataDump(){
		if(new File(this.getClass().getSimpleName() + ".data").exists()){
			try{
				FileInputStream fin = new FileInputStream(this.getClass().getSimpleName() + ".data");
				ObjectInputStream ois = new ObjectInputStream(fin);
				@SuppressWarnings("unchecked")
				Tuple<DocumentSet, DocumentSet> data = (Tuple<DocumentSet, DocumentSet>) ois.readObject();
				ois.close();
				return data;
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
		return null;
	}
	
	default void saveDataDump(DocumentSet trainingSet, DocumentSet testingSet){
		// save the data
		try{
			FileOutputStream fout = new FileOutputStream(this.getClass().getSimpleName() + ".data");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(new Tuple<DocumentSet, DocumentSet>(trainingSet, testingSet));
			oos.close();
			System.out.println("Done");			
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}
