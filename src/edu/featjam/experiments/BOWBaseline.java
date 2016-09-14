package edu.featjam.experiments;

import java.util.Collections;

import edu.featjam.classifiers.LibSVMClassifier;
import edu.featjam.core.Classifier;
import edu.featjam.core.DocumentSet;
import edu.featjam.core.Evaluations;
import edu.featjam.core.Experiment;
import edu.featjam.core.FeatjamParams;
import edu.featjam.datasets.OhsumedReader;
import edu.featjam.generators.BOWFeatureGenerator;
import edu.featjam.modifiers.DFThreashhold;
import edu.featjam.modifiers.TFIDFModifier;
import edu.featjam.util.Logger;
import edu.featjam.util.Tuple;

public class BOWBaseline implements Experiment{

	@Override
	public Tuple<DocumentSet, DocumentSet> loadDataSet() {
		Logger.writeToConsole("Reading dataset");
		OhsumedReader reader = new OhsumedReader("data/ohsu-trec/trec9-test/ohsumed.88-91", 
				true, OhsumedReader.WhatToUse.TITLE);
		Tuple<DocumentSet, DocumentSet> setsT = reader.getTrainTestSplit();
		DocumentSet trainingSet = setsT.x;
		DocumentSet testingSet = setsT.y;

		// apply basic generators
		Logger.writeToConsole("Applying feature generation");
		BOWFeatureGenerator bowGenerator = new BOWFeatureGenerator(1, "data/stopwords/stopwords.txt");
		bowGenerator.apply(trainingSet);
		bowGenerator.apply(testingSet);
		
		return new Tuple<>(trainingSet, testingSet);
	}

	DFThreashhold dfThreashhold = new DFThreashhold(BOWFeatureGenerator.defaultFeatureSet(), 2);
	TFIDFModifier tfidfModifier = new TFIDFModifier(BOWFeatureGenerator.defaultFeatureSet(), "ltc");
	
	@Override
	public Classifier buildClassifier(DocumentSet trainingSet) {
		String category = (String)FeatjamParams.get(FeatjamParams.CATEGORY);
		Classifier classifier = new LibSVMClassifier();
		classifier.build(category, trainingSet);
		return classifier;
	}

	@Override
	public void processData(DocumentSet trainingSet, DocumentSet testingSet) {
		dfThreashhold.train(trainingSet);
		dfThreashhold.apply(trainingSet);
		dfThreashhold.apply(testingSet);

		tfidfModifier.train(trainingSet);
		tfidfModifier.apply(trainingSet);
		tfidfModifier.apply(testingSet);
	}

	@Override
	public Evaluations doExperiment(DocumentSet trainingSet, DocumentSet testingSet) {
		processData(trainingSet, testingSet);
		FeatjamParams.setGlobal(LibSVMClassifier.SVM_C, 0.1);
		LibSVMClassifier.adjustParams(Collections.singleton(trainingSet.getSplitByNaturalOrder(0.5)), this);
		Classifier classifier = buildClassifier(trainingSet.deepCopy());
		return evaluate(testingSet.deepCopy(), classifier);
	}
}
