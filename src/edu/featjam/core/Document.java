package edu.featjam.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class Document implements Serializable{
	private static final long serialVersionUID = 1490230548216251575L;

	protected Map<String,FeatureSet> featureSets = new HashMap<>();
	protected Set<String> categories = new HashSet<>();

	// ordering for datasets with natural order
	protected Long order;
	
	
	// internal id used by feat-jam
	protected long id;
	
	// the dataset reader should set some unique, consistent, identifier here
	protected String name; 

	// the actual of the document 
	protected String text;

	private Document(long id, String name, String text) {
		this.id = id;
		this.name = name;
		this.text = text;
	}
	
	static long idCounter = 0;
	public static synchronized Document getNewDocument(String name, String text) {
		return new Document(idCounter++, name, text);
	}
	
	public FeatureSet getFeatureSet(String name) {
		return featureSets.get(name);
	}

	public void addFeatureSet(String name) {
		if(name.contains("_")){
			throw new RuntimeException("Feature set name must not contain '_' char!");
		}
		featureSets.put(name, new FeatureSet(name));
	}

	public Stream<FeatureSet> fsStream() {
		return featureSets.values().stream();
	}

	public Feature getFeature(String featureSet, String name) {
		Double val = getFeatureValue(featureSet, name);
		return val == null ? null : new Feature(new FeatureName(featureSet, name), val);
	}

	public Double getFeatureValue(FeatureName n) {
		return getFeatureValue(n.set(), n.name());
	}
	public Double getFeatureValue(String featureSet, String name) {
		FeatureSet fs = getFeatureSet(featureSet);
		if (fs == null) {
			return null;
		}
		Double val = fs.get(name);
		if (val == null) {
			return null;
		}
		return val;
	}

	public Set<String> getCategories() {
		return categories;
	}

	public void setCategories(Set<String> categories) {
		this.categories = new HashSet<>(categories);
	}

	public long getId() {
		return id;
	}

	public Long getOrder() {
		return order;
	}

	public void setOrder(Long order) {
		this.order = order;
	}
	
	public String getText() {
		return text;
	}
	
	public String getName() {
		return name;
	}
	
	public Document deepCopy(){
		Document doc = new Document(id, name, text);
		doc.setOrder(getOrder());
		
		doc.categories.addAll(categories);
		featureSets.forEach((name,fs)->doc.featureSets.put(name, fs.deepCopy()));
		return doc;
	}
}
