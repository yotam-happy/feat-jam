package edu.featjam.generators;

import edu.featjam.core.DocumentSet;
import edu.featjam.util.ESAWrapper;
import edu.featjam.util.Logger;
import edu.wiki.api.concept.IConceptVector;
import edu.wiki.util.counting.Counting;
import edu.wiki.util.db.ArticleTitleQueryOptimizer;

public class ESAFeatureGenerator {
	public static String defaultFeatureSet(){
		return "esa";
	}
	public static String categoriesFeatureSet(){
		return "esaCats";
	}

	ESAWrapper esa;
	
	private boolean generateCategories;
	private int maxConeceptsPerDoc;

	public ESAFeatureGenerator(boolean generateCategories, int maxConeceptsPerDoc) {
		this.generateCategories = generateCategories;
		this.maxConeceptsPerDoc = maxConeceptsPerDoc;
		esa = ESAWrapper.getInstance();
	}

	public void apply(DocumentSet documents){
		Logger.writeToConsole("ESA feature generation");
		Counting counter = new Counting(10, "docs processed: ");
		documents.stream().parallel().forEach((doc)->{
			doc.addFeatureSet(defaultFeatureSet());
			IConceptVector concepts = esa.getMultiResolutionVector(doc.getText(), maxConeceptsPerDoc);
			if (concepts == null){
				return;
			}
			concepts.forEach((id,s)->
				doc.getFeatureSet(defaultFeatureSet()).put(Integer.toString(id), s));
			
			if (generateCategories){
				doc.addFeatureSet(categoriesFeatureSet());
				IConceptVector categories = esa.getCategoriesVector(concepts, maxConeceptsPerDoc);
				categories.forEach((id,s)->
					doc.getFeatureSet(categoriesFeatureSet()).put(Integer.toString(id), s));
			}
			counter.addOne();
		});
	}
	
	public void report(DocumentSet documents){
		documents.stream().forEach((doc)->{
			StringBuffer sb = new StringBuffer();
			sb.append(doc.getText().substring(0, doc.getText().length() > 50 ? 50 : doc.getText().length()));
			sb.append(": ");
			
			doc.getFeatureSet(defaultFeatureSet()).stream().sorted((f1,f2)->-f1.value.compareTo(f2.value))
			.forEach((f)->{
				int fid = Integer.parseInt(f.id.name());
				String fname = ArticleTitleQueryOptimizer.getInstance().doQuery(fid);
				if (fname.length() > 20){
					fname = fname.substring(0, 20);
				}
				sb.append(fname + ": " + f.value() + "; ");
			});
			Logger.writeToConsole(sb.toString());
		});
	}
}
