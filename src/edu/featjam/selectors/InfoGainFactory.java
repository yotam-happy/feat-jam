package edu.featjam.selectors;

import edu.featjam.core.FeatureRelevanceMeasure;
import edu.featjam.core.FeatureRelevanceMeasureFactory;
import edu.featjam.util.InfoGainCalc;

public class InfoGainFactory implements FeatureRelevanceMeasureFactory {

	int nLabels = 2;
	boolean recursive = true;
	
	public InfoGainFactory(){
	}

	public InfoGainFactory(int nLabels){
		this.nLabels = nLabels;
	}

	public InfoGainFactory(int nLabels, boolean recursive){
		this.nLabels = nLabels;
		this.recursive = recursive;
	}
	
	@Override
	public FeatureRelevanceMeasure getRelevanceMeasure() {
		return new InfoGainCalc(nLabels, recursive);
	}
}
