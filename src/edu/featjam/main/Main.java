package edu.featjam.main;

import edu.featjam.core.Experiment;
import edu.featjam.experiments.Categories;
import edu.featjam.experiments.ESABaseline;
import edu.featjam.experiments.ExperimentOnCategorySet;

public class Main {
	public static void main(String[] args) {
		//Experiment experiment = new BOWBaseline();
		Experiment experiment = new ESABaseline();
		//Experiment experiment = new Categories();
		ExperimentOnCategorySet.doExperiment(experiment, true);
	}

}
