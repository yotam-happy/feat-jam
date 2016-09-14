package edu.featjam.classifiers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.featjam.core.Classifier;
import edu.featjam.core.Document;
import edu.featjam.core.DocumentSet;
import edu.featjam.core.Experiment;
import edu.featjam.core.FeatjamParams;
import edu.featjam.core.FeatureName;
import edu.featjam.util.Logger;
import edu.featjam.util.Tuple;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class LibSVMClassifier implements Classifier {
	public static final String SVM_C = "svm_c";
	public static final String SVM_GAMMA = "svm_gamma";

	protected svm_model model;
	protected String category; 
	protected List<FeatureName> features;

	public static svm_parameter getBasicSvmParameters(double C, double gamma){
		svm_parameter param = new svm_parameter();
		// default values
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.LINEAR;
		param.degree = 3;
		param.gamma = gamma;
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 100;
		param.C = C;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 0;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];
		return param;
	}

	public void build(String category, 
			DocumentSet docs) {
		build(category, docs, null);
	}
	
	public void build(String category, 
			DocumentSet docs,
			svm_parameter param) {
		
		this.category = category;
		
		Set<FeatureName> fs = new HashSet<>();
		docs.stream().flatMap((doc)->doc.fsStream()).flatMap((set)->set.stream()).forEach((f)->fs.add(f.id));
		features = new ArrayList<>(fs);

		svm_parameter p;
		if (param != null){
			p = param;
		}else{
			p = getBasicSvmParameters(
					(Double)FeatjamParams.get(LibSVMClassifier.SVM_C, 0.1), 
					(Double)FeatjamParams.get(LibSVMClassifier.SVM_GAMMA, 1.0 / features.size()));
		}

		svm_problem prob = getSvmProblem(docs);

		String error_msg = svm.svm_check_parameter(prob,p);
		if (error_msg != null){
			Logger.writeToConsole("LibSVM parameters invalid:");
			Logger.writeToConsole(error_msg);
			throw new RuntimeException("LibSVM parameters invalid");
		}
		
		svm.svm_set_print_string_function(new libsvm.svm_print_interface(){
		    @Override public void print(String s) {} // Disables svm output
		});
		try {
			model = svm.svm_train(prob,p);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean classify(Document doc) {
		Tuple<svm_node[],Double> t = getInstance(doc);
		double predicted = svm.svm_predict(model,t.x);
		return predicted > 0 ? true : false;
	}

	public svm_problem getSvmProblem(DocumentSet docs){
		svm_problem prob = new svm_problem();
		List<Document> list = docs.stream().collect(Collectors.toList());
		prob.l = list.size();
		prob.x = new svm_node[prob.l][];
		prob.y = new double[prob.l];

		for(int i = 0; i < list.size(); i++){
			Tuple<svm_node[],Double> t = getInstance(list.get(i));
			prob.x[i] = t.x;
			prob.y[i] = t.y;
		}
		return prob;
	}
	
	public Tuple<svm_node[],Double> getInstance(Document doc){
		int s = 0;
		for(FeatureName f : features){
			if(doc.getFeatureValue(f) != null){
				s++;
			}
		}
		
		svm_node[] x = new svm_node[s];
		int k = 0;
		for(int j = 0; j < features.size(); j++){
			if(doc.getFeatureValue(features.get(j)) != null){
				x[k] = new svm_node();
				x[k].index = j;
				x[k].value = doc.getFeatureValue(features.get(j));
				k++;
			}
		}
		
		double y = doc.getCategories().contains(category) ? 1.0 : -1.0;
		
		return new Tuple<>(x, y);
	}

	@Override
	public String getCategory() {
		return category;
	}
		
	public static void adjustParams(Set<Tuple<DocumentSet, DocumentSet>> folds, Experiment experiment) {
		List<Tuple<Double,Double>> best = IntStream.range(-6, 7).parallel().mapToObj((i)->{
			double C = Math.pow(2, i);
			FeatjamParams.setLocal(LibSVMClassifier.SVM_C, C);
			double score = experiment.crossValidation(folds, false);
			Logger.writeToConsole("SVM C Param: " + C + " score: " + score);
			return new Tuple<Double,Double>(score,(double)i);
		}).sorted((t1,t2)->{
			if (t1.x == t2.x){
				return -t1.y.compareTo(t2.y);
			}else{
				return t1.x.compareTo(t2.x);
			}
		}).collect(Collectors.toList());
		
		double max = best.stream().mapToDouble((t)->t.x).max().orElse(0);
		
		
		if(max > 0){
			int c = best.stream().mapToInt((t)->t.x == max ? 1 : 0).sum();
			double bestAvg = best.stream().mapToDouble((t)->t.x == max ? t.y : 0).sum() / c;
			bestAvg = Math.pow(2, bestAvg);
			Logger.writeToConsole("Best SVM C Param: " + bestAvg + " (" + max + ")");
			FeatjamParams.setGlobal(LibSVMClassifier.SVM_C, bestAvg);
		} else {
			Logger.writeToConsole("All SVM C Param seemed wrong, using defaul 0.1");
			FeatjamParams.setGlobal(LibSVMClassifier.SVM_C, 0.1);
		}
	}
}
