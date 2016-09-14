package edu.featjam.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.featjam.util.Tuple;

public class DocumentSet implements Serializable {
	private static final long serialVersionUID = 5194816601338895853L;
	
	Map<Long, Document> documents = new HashMap<Long, Document>();
	
	public void serialize(ObjectOutputStream oos){
		try {
			oos.writeObject(documents);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void deserialize(ObjectInputStream ois){
		try {
			documents = (Map<Long, Document>)ois.readObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}
	
	public DocumentSet() {
	}

	public Stream<Document> stream() {
		return documents.values().stream();
	}

	public void forEach(BiConsumer<Long, Document> action) {
		documents.forEach(action);
	}

	public Document get(long id) {
		return documents.get(id);
	}

	public void add(Document doc) {
		documents.put(doc.getId(), doc);
	}
	
	public int size() {
		return documents.size();
	}

	/**
	 * @return list of tuples: x is train, y is test
	 */
	public Set<Tuple<DocumentSet,DocumentSet>> getCrossValidationSplit(int folds){
		
		List<Tuple<DocumentSet,DocumentSet>> split = new ArrayList<>();
		for(int i = 0; i < folds; i++){
			split.add(new Tuple<>(new DocumentSet(),new DocumentSet()));
		}

		Random rnd = new Random();
		documents.forEach((id,doc)->{
			int fold = rnd.nextInt(folds);
			for(int i = 0; i < folds; i++){
				if(i == fold){
					split.get(i).y.add(doc);
				} else {
					split.get(i).x.add(doc);
				}
			}
		});
		return new HashSet<>(split);
	}
	
	public Tuple<DocumentSet,DocumentSet> getSplitByNaturalOrder(double holdoutFrac){
		List<Document> ordered = stream().sorted((d1, d2) -> d1.getOrder().compareTo(d2.getOrder()))
				.collect(Collectors.toList());
		
		DocumentSet train = new DocumentSet();
		DocumentSet holdout = new DocumentSet();
		for(int i = 0; i < ordered.size(); i++){
			if ((double)i / ordered.size() > holdoutFrac){
				train.add(ordered.get(i));
			}else{
				holdout.add(ordered.get(i));
			}
		}
		
		return new Tuple<>(train, holdout);
	}
	
	public DocumentSet deepCopy(){
		DocumentSet docs = new DocumentSet();
		forEach((id,doc)->docs.add(doc.deepCopy()));
		return docs;
	}
}
