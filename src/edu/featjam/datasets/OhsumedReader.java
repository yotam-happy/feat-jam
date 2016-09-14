package edu.featjam.datasets;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.featjam.core.Document;
import edu.featjam.core.DocumentSet;
import edu.featjam.util.Tuple;

public class OhsumedReader {

	protected String filename;
	protected boolean reatinOnlyWithoutAbstract;
	protected WhatToUse whatToUse;

	/**
	 * We follow Joachims (1998) split with which docs to keep and what is the train/test split
	 * @param filename	should be the 88-99 file. something like /.../trec9-test/ohsumed.88-91
	 */
	public OhsumedReader(String filename,
			boolean reatinOnlyWithoutAbstract, 
			WhatToUse whatToUse) {
		this.filename = filename;
		this.reatinOnlyWithoutAbstract = reatinOnlyWithoutAbstract;
		this.whatToUse = whatToUse;
	}

	public Document makeDocument(Map<String,String> props, String id) {
		if (reatinOnlyWithoutAbstract && props.containsKey("W") && !props.get("W").isEmpty()){
			return null;
		}
		
		String text = null;
		switch(whatToUse){
		case ABSTRACT:
			text = props.get("W");
			break;
		case TITLE:
			text = props.get("T");
			break;
		case TITLE_N_ABSTRACT:
			if (props.get("W") != null || props.get("T") != null){
				text = (props.get("W") != null ? props.get("W") : "") + " " + 
						(props.get("T") != null ? props.get("T") : "");
			}
		}

		if (text == null){
			return null;
		}
		
		// Create a document object
		Document doc = Document.getNewDocument(id, text);

		// add expected topics
		if ( props.get("M") != null){
			String[] topics = props.get("M").split(";");
			for(String topic : topics) {
				if(topic.contains("/")){
					topic = topic.substring(0,topic.indexOf('/'));
				}
				while(topic.endsWith(".")){
					topic = topic.substring(0, topic.length() - 1);
				}
				doc.getCategories().add(topic.trim());
			}
		}
		
		return doc;
	}
	
	public Tuple<DocumentSet,DocumentSet> getTrainTestSplit(){
		DocumentSet train = new DocumentSet();
		DocumentSet test = new DocumentSet();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename)); 
			String l = br.readLine();
			String nextId = l.substring(3).trim();
			int n = 0;
			
			while(l != null){
				Map<String,String> props = new HashMap<>();
				l = br.readLine();
				while(l != null && !l.startsWith(".I")){
					if(!l.startsWith(".") || l.length() != 2){
						br.close();
						throw new RuntimeException("File does not match OHSUMED expected format");
					}
					props.put(l.substring(1,2), br.readLine().trim());
					l = br.readLine();
				}
				Document doc = makeDocument(props, nextId);
				
				if(doc != null && props.get("U").substring(0,2).equals("91")){ // "91" follows Joachims (1998) split
					
					doc.setOrder((long)n); // OHSUMED is sorted by publication date
					if(n < 10000){
						train.add(doc);
					}else{
						test.add(doc);
					}
					
					n++;
					if(n >= 20000){ // follows Joachims (1998) split
						br.close();
						return new Tuple<>(train, test);
					}
				}
				nextId = l.substring(3).trim();
			}
			br.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		throw new RuntimeException("something went wrong");
	}

	public enum WhatToUse{
		ABSTRACT, TITLE, TITLE_N_ABSTRACT
	}

}
