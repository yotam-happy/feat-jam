package edu.featjam.modifiers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.featjam.core.DocumentSet;
import edu.featjam.core.FeatureName;

public class NormalizePerFeatureModifier {
	protected Set<String> targetFeatureSets;
	Map<FeatureName, Double> mean;
	Map<FeatureName, Double> stddev;

	public NormalizePerFeatureModifier(String targetFeatureSet){
		this(Collections.singleton(targetFeatureSet));
	}

	public NormalizePerFeatureModifier(Set<String> targetFeatureSets){
		this.targetFeatureSets = targetFeatureSets;
	}
	

	public void train(DocumentSet docs){
		mean = new HashMap<>();
		stddev = new HashMap<>();

		docs.stream().flatMap((doc)->doc.fsStream())
		.filter((fs)->targetFeatureSets.contains(fs.getName()))
		.flatMap((fs)->fs.stream()).forEach(
				(f)->mean.merge(f.id(), f.value(), (v1,v2)->v1+v2));
		mean.replaceAll((name,v)->v / docs.size());
		
		docs.stream().flatMap((doc)->doc.fsStream())
		.filter((fs)->targetFeatureSets.contains(fs.getName()))
		.flatMap((fs)->fs.stream()).forEach(
				(f)->stddev.merge(f.id(), f.value(), (v1,v2)->v1+Math.pow(v2 - mean.get(f.id), 2)));
		stddev.replaceAll((name,v)->Math.sqrt(v));
	}

	public void apply(DocumentSet docs){
		docs.stream().flatMap((doc)->doc.fsStream())
		.filter((fs)->targetFeatureSets.contains(fs.getName()))
		.forEach((fs)->{
			fs.compute((name,v)-> {
				if (mean.get(name) == null){
					return v;
				}
				double r = (v - mean.get(name)) / (stddev.get(name) > 0 ? stddev.get(name) : 1);
				return r;
			});
		});
	}
}
