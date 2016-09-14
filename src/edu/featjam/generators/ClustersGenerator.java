package edu.featjam.generators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.clustering.jot.datapoint.DenseEucledianPoint;
import edu.clustering.jot.datapoint.DenseEucledianPoint.Metric;
import edu.clustering.jot.interfaces.Point;
import edu.featjam.core.DocumentSet;
import edu.featjam.core.FeatjamParams;
import edu.featjam.core.FeatureName;
import edu.featjam.util.Word2VecReader;

public class ClustersGenerator extends AbstractClusterFeatureGenerator{
	public static final String TEMP_FILE = "concet_w2v_vectors.dump";

	protected String sourceFeatureSet;
	protected String word2vecFile;
	protected int k;

	public ClustersGenerator(String sourceFeatureSet, String word2vecFile, int k){
		this.sourceFeatureSet = sourceFeatureSet;
		this.k = k;
		this.word2vecFile = word2vecFile;
	}

	public static String defaultFeatureSet(){
		return "clusters";
	}

	@Override
	protected Map<Integer, Cluster> getClusters(DocumentSet docs) {
		String category = (String)FeatjamParams.get(FeatjamParams.CATEGORY);
		
		List<FeatureName> featuresTfidfSorted = 
				featureTfidf(docs, category).entrySet().stream()
				.sorted((e1,e2)->-e1.getValue().compareTo(e2.getValue()))
				.map((e)->e.getKey()).collect(Collectors.toList()); 
		
		Set<String> featureNamesToCluster = new HashSet<>();
		for(int i = 0; i < featuresTfidfSorted.size() / 2; i++){
			featureNamesToCluster.add(featuresTfidfSorted.get(i).name());
		}
		
		Map<String, Point> vecs = getW2VVectors(featureNamesToCluster);

		Map<Integer, Cluster> clusters = new HashMap<>();
		KmeansPPHelper(k, vecs).forEach(
				(i,c)->clusters.put(clusters.size(), c));
		return clusters;
	}

	/**
	 * Tries to add very similar concepts to clusters
	 */
	public void inflateClusters(Map<Integer, Cluster> clusters){
		Map<Integer, Map<String,Double>> candidates = new HashMap<>();
		clusters.forEach((id,c)->candidates.put(id, new HashMap<>()));
		
		new Word2VecReader(word2vecFile).forEach((name,vec)->{
			Point p = new DenseEucledianPoint(vec, Metric.CosineDistnce);
			candidates.forEach((clusterId, cands)->{
				cands.put(name, p.distance(clusters.get(clusterId).centroid));
			});
		});
		
		clusters.forEach((clusterId, cluster)->{
			//Map<String,Double> cands = candidates.get(key)
		});
		
	}
	
	public Map<FeatureName, Double> featureTfidf(DocumentSet docs, String category){
		Map<FeatureName, Double> df = new HashMap<>();
		docs.stream().flatMap((doc)->doc.fsStream())
		.filter((fs)->sourceFeatureSet.equals(fs.getName()))
		.flatMap(fs->fs.stream()).forEach((f)->df.compute(f.id, (fn,v)->(v != null ? v : 0) + f.value()));
		
		Map<FeatureName, Double> tf = new HashMap<>();
		docs.stream().filter((doc)->doc.getCategories().contains(category))
		.flatMap((doc)->doc.fsStream())
		.filter((fs)->sourceFeatureSet.equals(fs.getName()))
		.flatMap(fs->fs.stream()).forEach((f)->tf.compute(f.id, (fn,v)->(v != null ? v : 0) + f.value()));
		
		Map<FeatureName, Double> tfidf = new HashMap<>();
		tf.forEach((fn,v)->tfidf.put(fn, v / Math.log(1 + df.get(fn))));
		
		return tfidf;
	}
	public Map<FeatureName, Double> featureCategoryTfidf(DocumentSet docs, String category){
		// get list of topics with document count per topic
		Map<String, Long> topicCounts = docs.stream()
				// map to target features of each document
				.flatMap((doc)->doc.getCategories().stream())
				// count documents per each target feature
				.collect(Collectors.groupingBy(String::toString, Collectors.counting()));
		
		Map<FeatureName, Double> df = new HashMap<>();
		docs.forEach((docId,doc)->{
			doc.getCategories().stream().filter((cat)->topicCounts.get(cat) >= 10)
			.forEach((docCat)->{
				doc.fsStream().filter((fs)->sourceFeatureSet.equals(fs.getName()))
				.flatMap(fs->fs.stream()).forEach((f)->df.compute(f.id, (fn,v)->(v != null ? v : 0) + f.value()));
			});
		});
		
		Map<FeatureName, Double> tf = new HashMap<>();
		docs.stream().filter((doc)->doc.getCategories().contains(category))
		.flatMap((doc)->doc.fsStream())
		.filter((fs)->sourceFeatureSet.equals(fs.getName()))
		.flatMap(fs->fs.stream()).forEach((f)->tf.compute(f.id, (fn,v)->(v != null ? v : 0) + f.value()));
		
		Map<FeatureName, Double> tfidf = new HashMap<>();
		tf.forEach((fn,v)->tfidf.put(fn, v / Math.log(1 + df.get(fn))));
		
		return tfidf;
	}

	
	
//	@SuppressWarnings("unchecked")
	protected Map<String, Point> getW2VVectors(Set<String> conceptFilter){
		// This is essentially a one off task so we can save results somewhere
/*		if (new File(TEMP_FILE).exists()){
			try{
				FileInputStream fin = new FileInputStream(TEMP_FILE);
				@SuppressWarnings("resource")
				ObjectInputStream ois = new ObjectInputStream(fin);
				return (Map<String, Point>) ois.readObject();
			}catch(IOException | ClassNotFoundException e){
				// Just do it again
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}*/

		Map<String, Point> map = new HashMap<>();
		new Word2VecReader(word2vecFile).forEach((name,vec)->{
			if(conceptFilter.contains(name)){
				map.put(name,  new DenseEucledianPoint(vec, Metric.CosineDistnce));
			}
		});

/*		try{
			FileOutputStream fout = new FileOutputStream(TEMP_FILE);
			@SuppressWarnings("resource")
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(map);
		}catch(IOException e){
			// Just do it again next time	
			System.out.println(e.getMessage());
			e.printStackTrace();
		}*/
		
		return map;
	}
	
	@Override
	protected String sourceFeatureSet() {
		// TODO Auto-generated method stub
		return sourceFeatureSet;
	}
}
