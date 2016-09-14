package edu.featjam.generators;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.clustering.jot.algorithms.AlgorithmConstructor;
import edu.clustering.jot.interfaces.ClusteringAlgorithm;
import edu.clustering.jot.interfaces.Point;
import edu.featjam.core.DocumentSet;
import edu.featjam.core.FeatureName;
import edu.featjam.util.Logger;
import edu.wiki.util.db.ArticleTitleQueryOptimizer;

public abstract class AbstractClusterFeatureGenerator {
	public static String defaultFeatureSet(){
		return "clusters";
	}

	protected abstract Map<Integer, Cluster> getClusters(DocumentSet docs);
	protected abstract String sourceFeatureSet();

	Map<Integer, Cluster> clusters;
	Map<FeatureName, Map<Integer,Double>> featureCluster;

	protected int maxIterations = 25;
	protected double minDelta = 0.01;

	public void train(DocumentSet docs){
		clusters = getClusters(docs);

		// Calculate inverse map
		featureCluster = new HashMap<>();
		clusters.forEach((id,cluster)->{
			cluster.features.forEach((featureName,featureValue)->{
				if (!featureCluster.containsKey(featureName)){
					featureCluster.put(featureName, new HashMap<>());
				}
				featureCluster.get(featureName).put(id, featureValue);
			});
		});
		
		clusters.forEach((i, cluster)->{
			StringBuffer sb = new StringBuffer();
			sb.append("cluster " + i + ": ");
			cluster.features.forEach((name,d)->{
					String fname = ArticleTitleQueryOptimizer.getInstance()
							.doQuery(Integer.parseInt(name.name()));
					if (fname.length() > 20){
						fname = fname.substring(0, 20);
					}
					sb.append(fname + "; ");
			});
			Logger.writeToConsole(sb.toString());
		});
	}
	
	public void apply(DocumentSet docs){
		docs.forEach((docId,doc)->{
			doc.addFeatureSet(defaultFeatureSet());
			doc.fsStream().filter((fs)->sourceFeatureSet().equals(fs.getName()))
			.flatMap((fs)->fs.stream()).forEach((f)->{
				if(featureCluster.containsKey(f.id())){
					featureCluster.get(f.id()).forEach((clusterId, factor)->{
						doc.getFeatureSet(defaultFeatureSet()).put(
								clusterId.toString(), 
								f.value() * factor, (v)-> v + f.value() * factor);
					});
				}
			});
		});
		docs.stream().flatMap((doc)->doc.fsStream())
		.filter((fs)->defaultFeatureSet().equals(fs.getName()))
		.forEach((fs)->fs.compute((f,v)->Math.log(v)));
	}

	/**
	 * Helper function to work the kmeans++ algorithm
	 */

	protected Map<Integer,Cluster> KmeansPPHelper(int k, Map<String,Point> m){
		List<Point> l = Arrays.asList(m.values().toArray(new Point[0]));
		ClusteringAlgorithm<Point> clusterer = 
				AlgorithmConstructor.getKMeansPlusPlus(maxIterations, minDelta);

		clusterer.doClustering(k, k, l);
		List<Point> centroids = clusterer.getCentroids();
		
		Map<Integer,Cluster> clusters = new HashMap<>();
		// Create clusters
		for(int i = 0; i < centroids.size(); i++){
			clusters.put(i, new Cluster(i, centroids.get(i)));
		}

		// populate clusters
		m.forEach((name, point)->{
			double bestD = Double.MAX_VALUE;
			int bestClusterId = -1;
			for(int i = 0; i < centroids.size(); i++){
				double D = point.distance(clusters.get(i).centroid);
				if (D < bestD){
					bestD = D;
					bestClusterId = i;
				}
			}
			if (bestClusterId != -1){
				clusters.get(bestClusterId).features.put(new FeatureName(sourceFeatureSet(), name), 1.0);
			}
		});

		return clusters;
	}

	public class Cluster{
		public int id;
		public Point centroid;
		public Map<FeatureName,Double> features = new HashMap<>();
		
		public Cluster(int id, Point centroid){
			this.id = id;
			this.centroid = centroid;
		}
	}	
}
