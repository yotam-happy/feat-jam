package edu.featjam.modifiers;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import edu.featjam.core.DocumentSet;
import edu.featjam.core.FeatureName;
import edu.featjam.util.FeatjamUtils;
import edu.featjam.util.Logger;

public class TFIDFModifier {
	protected Set<String> targetFeatureSets;
	public String scheme;
	
	Map<FeatureName, Integer> df;
	double nTrainDocs;
	
	public TFIDFModifier(String targetFeatureSet, String scheme){
		this(Collections.singleton(targetFeatureSet), scheme);
	}

	public TFIDFModifier(Set<String> targetFeatureSets, String scheme){
		this.targetFeatureSets = targetFeatureSets;
		this.scheme = scheme;
	}
	
	public void train(DocumentSet docs){
		df = FeatjamUtils.getDFForFeatureSets(docs, targetFeatureSets); 
		df.forEach((n,f)->{
			if(f < 2){
				Logger.writeToConsole("DF1 : " + n);
			}
		});
		nTrainDocs = docs.size();
	}
	
	public void apply(DocumentSet documents){
		documents.forEach((id,doc)->{
			double norm;
			if (scheme.charAt(2) == 'c'){
				norm = Math.sqrt(doc.fsStream()
						.filter((fs)->targetFeatureSets.contains(fs.getName()))
						.flatMap((fs)->fs.stream())
						.mapToDouble((f)->f.value() * f.value()).sum());
			} else {
				norm = 1.0;
			}

			Double aux;
			switch(scheme.charAt(0)){
			case 'a':
				aux = doc.fsStream()
						.filter((fs)->targetFeatureSets.contains(fs.getName()))
						.flatMap((fs)->fs.stream())
						.mapToDouble((f)->f.value()).max().orElse(0.0);
				break;
			case 'L':
				aux = doc.fsStream()
				.filter((fs)->targetFeatureSets.contains(fs.getName()))
				.flatMap((fs)->fs.stream())
				.mapToDouble((f)->f.value()).average().orElse(0.0);
				break;
			default:
				aux = 0.0;
			}

			doc.fsStream().filter((fs)->targetFeatureSets.contains(fs.getName()))
			.forEach((fs)->{
				fs.compute((name,value)->{
					if(df.get(name) == null){
						// didn't appear in the training set, removing!
						return null;
					}
					Double docFreq;
					switch(scheme.charAt(1)){
					case 'n': docFreq = 1.0; break;
					case 't': docFreq = Math.log(nTrainDocs / df.get(name)); break;
					case 'p': docFreq = Math.max(0, Math.log((nTrainDocs - df.get(name)) / df.get(name))); break;
					default: docFreq = 1.0;
					}

					Double tf = 0.0;
					switch(scheme.charAt(0)){
					case 'n': tf = value; break;
					case 'l': tf = 1 + Math.log(value); break;
					case 'a': tf = 0.5 + 0.5 * value / aux; break;
					case 'b': tf = value > 0.1 ? 1.0 : 0.0; break;
					case 'L': tf = (1 + Math.log(value)) / (1 + Math.log(aux)); break;
					}

					return tf * docFreq / norm;
				});
			});
		});
	}
}
