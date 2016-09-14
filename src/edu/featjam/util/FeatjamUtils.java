package edu.featjam.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.featjam.core.DocumentSet;
import edu.featjam.core.FeatureName;

public class FeatjamUtils {
	public static Map<FeatureName, Integer> getDFForFeatureSets(DocumentSet docs, Set<String> featureSets){
		Map<FeatureName, Integer> df = new HashMap<>();
		docs.stream().flatMap((doc)->doc.fsStream())
		.filter((fs)->featureSets.contains(fs.getName()))
		.flatMap((fs)->fs.stream()).forEach(
				(f)->df.merge(f.id(), 1, (v1,v2)->v1+v2));
		return df;
	}
}
