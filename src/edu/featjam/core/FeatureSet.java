package edu.featjam.core;

import java.io.Serializable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public class FeatureSet implements Serializable{
	private static final long serialVersionUID = -6874480113462806006L;

	String name;
	TObjectDoubleHashMap<String> features = new TObjectDoubleHashMap<String>(1, 0.75f, Double.NaN);
	
	public FeatureSet(String name) {
		this.name = name;
	}
	
	public Double get(String name) {
		Double v = features.get(name);
		return v.isNaN() ? null : v;
	}

	public Stream<Feature> stream() {
		if(features.size() == 0){
			return Stream.empty();
		}
		TObjectDoubleIterator<String> iter = features.iterator();
		return Stream.generate(()->{
			iter.advance();
			Feature f = new Feature(new FeatureName(name, iter.key()),iter.value());
			return f;
		}).limit(features.size());
	}
	
	public void forEach(BiConsumer<FeatureName,Double> action){
		features.forEachEntry((name,value)->{
			action.accept(new FeatureName(getName(), name),value);
			return true;
		});
	}

	public void put(String name, double value) {
		features.put(name, (Double)value);
	}

	public void put(String name, double value, Function<Double, Double> f) {
		if (!features.contains(name)){
			features.put(name, value);
		} else {
			features.put(name, f.apply(value));
		}
	}

	public void remove(String name) {
		features.remove(name);
	}

	public void removeAll() {
		features.clear();
	}

	public void compute(BiFunction<FeatureName, Double, Double> function) {
		if(features.size() == 0){
			return;
		}
		TObjectDoubleIterator<String> iter = features.iterator();
		while(iter.hasNext()){
			iter.advance();
			Double d = function.apply(new FeatureName(name, iter.key()), iter.value());
			if (d == null){
				iter.remove();
			} else {
				iter.setValue(d);
			}
		}
	}

	public String getName() {
		return name;
	}
	
	public FeatureSet deepCopy() {
		FeatureSet fs = new FeatureSet(this.getName());
		fs.features.putAll(features);
		return fs;
	}
}
