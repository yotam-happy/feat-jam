package edu.featjam.core;

import java.io.Serializable;

public class FeatureName implements Serializable{
	private static final long serialVersionUID = -753737480072968105L;

	protected final String set;
	protected final String name;
	
	public FeatureName(String set, String name) {
		this.set = set;
		this.name = name;
	}
	
	public String name() {
		return name;
	}

	public String set() {
		return set;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FeatureName)){
			return false;
		}
		FeatureName other = (FeatureName)obj;
		return name.equals(other.name) && set.equals(other.set);
	}
  
	@Override
	public int hashCode() {
		return name.hashCode() * 257 + set.hashCode() * 137;
	}
  
	@Override
	public String toString() {
		return set + ":" + name;
	}
}
