package edu.featjam.experiments;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import edu.featjam.core.DocumentSet;
import edu.featjam.core.Evaluations;
import edu.featjam.core.Experiment;
import edu.featjam.core.FeatjamParams;
import edu.featjam.core.MacroEvaluations;
import edu.featjam.util.Logger;
import edu.featjam.util.Tuple;

public class ExperimentOnCategorySet {
	public static MacroEvaluations doExperiment(Experiment experiment, boolean writeToLogger) {
		Tuple<DocumentSet, DocumentSet> t = experiment.loadDataSet();
		
		// get list of topics with document count per topic
		List<Entry<String, Long>> topicCounts = t.x.stream()
				// map to target features of each document
				.flatMap((doc)->doc.getCategories().stream())
				// count documents per each target feature
				.collect(Collectors.groupingBy(String::toString, Collectors.counting()))
				// sort features by doc count
				.entrySet().stream().sorted((e1,e2)->e2.getValue().compareTo(e1.getValue()))
				.collect(Collectors.toList());
		
		String[] cats = new String[]{"State Medicine", "Portraits", "Physicians", "Publishing", "Lung Neoplasms",
				"HIV-1", "Support, U.S. Gov't, Non-P.H.S", "Blood Pressure", "Pediatrics", "Smoking", "Kidney Failure, Chronic",
				"Aging", "Forecasting", "Molecular Sequence Data", "Pregnancy Complications", "Cells, Cultured",
				"Rats, Inbred Lew", "Periodicals", "Methods", "Mutation", "Antibiotics", "Hemodynamics", "Surgery",
				"Delivery of Health Care", "Health Policy", "Cerebrovascular Disorders", "Recombinant Proteins",
				"Equipment Failure", "Hemodialysis", "DNA", "Intubation, Intratracheal", "Emergencies", "Bone Marrow Transplantation",
				"Cytomegalic Inclusion Disease", "Substance Abuse", "Tissue Donors", "Brain Diseases", "Skin Diseases",
				"Data Collection", "Questionnaires", "Coronary Artery Bypass", "Cardiovascular Diseases", 
				"Polymerase Chain Reaction", "Education, Medical", "Heart", "Pregnancy Complications, Infectious",
				"Developing Countries", "Drug Industry", "Lymphocyte Transformation", "War"}; 

		if(writeToLogger){
			Logger.writeToResults("---------------------------------");
			Logger.writeToResults("Starting test " + 
					new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));
			Logger.writeToResults("---------------------------------");
		}

		MacroEvaluations allEvals = new MacroEvaluations();
		for(String category : cats){
			DocumentSet trainingSet = t.x.deepCopy();
			DocumentSet testingSet = t.y.deepCopy();
//			Entry<String,Long> e = topicCounts.get(i);
//			String category = e.getKey();

			FeatjamParams.setGlobal(FeatjamParams.CATEGORY, category);

			Evaluations evals = experiment.doExperiment(trainingSet, testingSet);
			allEvals.addExperimentResults(category, evals);
			if(writeToLogger){
				Logger.writeToResults(category + "\t " + evals.fMeasure() + "\t" + evals.precision() + "\t" + evals.recall());
			}
		}		
		if(writeToLogger){
			Logger.writeToResults("Macro F1-measure: " + allEvals.macroF());
			Logger.writeToResults("Micro F1-measure: " + allEvals.microF());
		}
		return allEvals;
	}
	
}
