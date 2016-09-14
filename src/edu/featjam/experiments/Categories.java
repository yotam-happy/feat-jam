package edu.featjam.experiments;

import edu.featjam.classifiers.LibSVMClassifier;
import edu.featjam.core.Classifier;
import edu.featjam.core.DocumentSet;
import edu.featjam.core.Evaluations;
import edu.featjam.core.Experiment;
import edu.featjam.core.FeatjamParams;
import edu.featjam.datasets.OhsumedReader;
import edu.featjam.generators.BOWFeatureGenerator;
import edu.featjam.generators.ClustersGenerator;
import edu.featjam.generators.ESAFeatureGenerator;
import edu.featjam.modifiers.DFThreashhold;
import edu.featjam.modifiers.TFIDFModifier;
import edu.featjam.selectors.InfoGainFactory;
import edu.featjam.selectors.RelevanceMeasureFeatureSelector;
import edu.featjam.util.Logger;
import edu.featjam.util.Tuple;

public class Categories implements Experiment{
	@Override
	public Tuple<DocumentSet, DocumentSet> loadDataSet() {
		Tuple<DocumentSet, DocumentSet> dataDump = loadDataDump();
		if(dataDump != null){
			return dataDump;
		}

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
		
		ESAFeatureGenerator esaGenerator = new ESAFeatureGenerator(false, 200);
		esaGenerator.apply(trainingSet);
		esaGenerator.apply(testingSet);
		
		saveDataDump(trainingSet, testingSet);
		
		return new Tuple<>(trainingSet, testingSet);
	}

	ClustersGenerator clustersGenerator = new ClustersGenerator(
			ESAFeatureGenerator.defaultFeatureSet(),
			"data/word2vec/dim300context_vecs",
			20);
	DFThreashhold dfThreashhold = new DFThreashhold(BOWFeatureGenerator.defaultFeatureSet(), 2);
	TFIDFModifier tfidfModifier = new TFIDFModifier(BOWFeatureGenerator.defaultFeatureSet(), "ltc");
	RelevanceMeasureFeatureSelector infogainFS = new RelevanceMeasureFeatureSelector(
			ESAFeatureGenerator.defaultFeatureSet(), 
			4000, 
			new InfoGainFactory());
	
	@Override
	public Classifier buildClassifier(DocumentSet trainingSet) {
		String category = (String)FeatjamParams.get(FeatjamParams.CATEGORY);
		Classifier classifier = new LibSVMClassifier();
		classifier.build(category, trainingSet);
		return classifier;
	}

	@Override
	public void processData(DocumentSet trainingSet, DocumentSet testingSet) {
		String category = (String)FeatjamParams.get(FeatjamParams.CATEGORY);

		// modifiers
		dfThreashhold.train(trainingSet);
		dfThreashhold.apply(trainingSet);
		dfThreashhold.apply(testingSet);

		tfidfModifier.train(trainingSet);
		tfidfModifier.apply(trainingSet);
		tfidfModifier.apply(testingSet);
		
		clustersGenerator.train(trainingSet);
		clustersGenerator.apply(trainingSet);
		clustersGenerator.apply(testingSet);
		
		// feature selection
		Logger.writeToConsole("Applying feature selection");
		infogainFS.train(trainingSet, category);
		infogainFS.apply(trainingSet);
		infogainFS.apply(testingSet);
	}

	@Override
	public Evaluations doExperiment(DocumentSet trainingSet, DocumentSet testingSet) {
		processData(trainingSet, testingSet);
		FeatjamParams.setGlobal(LibSVMClassifier.SVM_C, 0.1);
		//LibSVMClassifier.adjustParams(trainingSet, this);
		Classifier classifier = buildClassifier(trainingSet.deepCopy());
		return evaluate(testingSet.deepCopy(), classifier);
	}

}
