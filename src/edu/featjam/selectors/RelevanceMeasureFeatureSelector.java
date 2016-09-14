package edu.featjam.selectors;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.featjam.core.DocumentSet;
import edu.featjam.core.FeatureName;
import edu.featjam.core.FeatureRelevanceMeasure;
import edu.featjam.core.FeatureRelevanceMeasureFactory;
import edu.featjam.util.Logger;
import edu.wiki.util.Tuple;

public class RelevanceMeasureFeatureSelector {
	
	protected Set<String> sourceFeatureSets;
	protected int keepNFeatures;
	protected FeatureRelevanceMeasureFactory frmFactory;

	Set<FeatureName> toKeep;
	
	public RelevanceMeasureFeatureSelector(Set<String> sourceFeatureSets,
			int keepNFeatures, 
			FeatureRelevanceMeasureFactory frmFactory) {
		this.sourceFeatureSets = sourceFeatureSets;
		this.keepNFeatures = keepNFeatures;
		this.frmFactory = frmFactory;
	}
	
	public RelevanceMeasureFeatureSelector(String sourceFeatureSet,
			int keepNFeatures, 
			FeatureRelevanceMeasureFactory frmFactory) {
		this.sourceFeatureSets = new HashSet<>();
		sourceFeatureSets.add(sourceFeatureSet);
		this.keepNFeatures = keepNFeatures;
		this.frmFactory = frmFactory;
	}

	public void train(DocumentSet docs, String targetCategory){
		double[] expectedWeightPerLabel = new double[2];
		// class counts
		docs.forEach((docId, doc)->{
			int cls = doc.getCategories().contains(targetCategory) ? 1 : 0;
			expectedWeightPerLabel[cls] += 1;
		});

		// get all features
		Map<FeatureName,FeatureRelevanceMeasure> featureRelevance = new HashMap<>(); 
		docs.forEach((docId, doc)->{
			doc.fsStream().filter((fs)->sourceFeatureSets.contains(fs.getName()))
				.flatMap((fs)->fs.stream())
				.forEach((f)->{
					if(!featureRelevance.containsKey(f.id())){
						FeatureRelevanceMeasure c = frmFactory.getRelevanceMeasure();
						c.setExpectedWeightPerLabelHint(expectedWeightPerLabel);
						featureRelevance.put(f.id(), c);
					}
				});
		});
		
		Logger.writeToConsole("Doing infogain feature selection with " + featureRelevance.size() + " features");

		// get feature statistics
		docs.forEach((docId, doc)->{
			int category = doc.getCategories().contains(targetCategory) ? 1 : 0;
			doc.fsStream().filter((fs)->sourceFeatureSets.contains(fs.getName()))
			.flatMap((fs)->fs.stream()).forEach((f)->{
				featureRelevance.get(f.id()).addSample(category, f.value());
			});
		});
		
		// calculate final info gains
		List<Tuple<Double,FeatureName>> l = featureRelevance.entrySet().stream()
			.map((e)->new Tuple<Double,FeatureName>(e.getValue().estimateRelevance(), e.getKey()))
			.sorted((e1,e2)->-Double.compare(e1.x.doubleValue(),e2.x.doubleValue()))
			.collect(Collectors.toList());
		Collections.sort(l, (e1,e2)->-Double.compare(e1.x.doubleValue(),e2.x.doubleValue()));
		
		
		toKeep = new HashSet<>();
		for(int i = 0; i < keepNFeatures && i < l.size(); i++){
			toKeep.add(l.get(i).y);
		}
	}
	
	public void apply(DocumentSet docs) {
		docs.stream().flatMap((doc)->doc.fsStream())
		.filter((fs)->sourceFeatureSets.contains(fs.getName()))
		.forEach((fs)->{
			fs.compute((fn, v)->toKeep.contains(fn) ? v : null);
		});
	}
}