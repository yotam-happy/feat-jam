package edu.featjam.modifiers;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import edu.featjam.core.DocumentSet;
import edu.featjam.core.FeatureName;
import edu.featjam.util.FeatjamUtils;

public class DFThreashhold {
	protected Set<String> targetFeatureSets;
	int minDF;
	
	Map<FeatureName, Integer> df;
	
	public DFThreashhold(String targetFeatureSet, int minDF){
		this(Collections.singleton(targetFeatureSet), minDF);
	}

	public DFThreashhold(Set<String> targetFeatureSets, int minDF){
		this.targetFeatureSets = targetFeatureSets;
		this.minDF = minDF;
	}
	
	public void train(DocumentSet docs){
		df = FeatjamUtils.getDFForFeatureSets(docs, targetFeatureSets); 
	}
	
	public void apply(DocumentSet docs){
		docs.stream().flatMap((doc)->doc.fsStream())
		.filter((fs)->targetFeatureSets.contains(fs.getName()))
		.forEach((fs)->fs.compute(
				(name,value)->df.get(name) == null || df.get(name) < minDF ? null : value));
	}
}