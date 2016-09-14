package edu.featjam.core;

import java.io.Serializable;

public class Feature implements Serializable{
	private static final long serialVersionUID = 2976034890447604993L;

	public final FeatureName id;
	public final Double value;
	
	public Feature(FeatureName id, Double value) {
		this.id = id;
		this.value = value;
	}
	
	public FeatureName id() {
		return id;
	}

	public Double value() {
		return value;
	}
}
