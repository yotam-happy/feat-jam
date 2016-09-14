package edu.featjam.modifiers;

import java.util.Collections;
import java.util.Set;

import edu.featjam.core.DocumentSet;

public class NormalizePerDocumentModifier {
	protected Set<String> targetFeatureSets;

	public NormalizePerDocumentModifier(String targetFeatureSet){
		this(Collections.singleton(targetFeatureSet));
	}

	public NormalizePerDocumentModifier(Set<String> targetFeatureSets){
		this.targetFeatureSets = targetFeatureSets;
	}
	

	public void apply(DocumentSet docs){
		docs.forEach((id,doc)->{
			double norm = Math.sqrt(
					doc.fsStream().filter((fs)->targetFeatureSets.contains(fs.getName()))
					.flatMap((fs)->fs.stream()).mapToDouble((f)->f.value * f.value).sum());
			doc.fsStream().filter((fs)->targetFeatureSets.contains(fs.getName()))
			.forEach((fs)->fs.compute((n,v)-> v / norm));
		});
	}
}
