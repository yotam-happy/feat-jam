package edu.featjam.datasets;

import java.io.FileNotFoundException;
import java.io.FileReader;

import edu.featjam.core.Document;
import edu.featjam.core.DocumentSet;
import edu.featjam.util.SimpleTagDoc;
import edu.featjam.util.SimpleTagDocReader;
import edu.featjam.util.Tuple;

public class Reuters21578Reader {
	protected String directory;
	protected WhatToUse whatToUse;
	
	public Reuters21578Reader(String directory, WhatToUse whatToUse) {
		this.directory = directory;
		this.whatToUse = whatToUse;
	}

	public Document buildDocument(SimpleTagDoc simpleDoc){
		// Do some validations
		if (!simpleDoc.getName().equals("REUTERS")) {
			return null;
		}
		
		// Using Modified Apte ("ModApte") Split
		if(simpleDoc.attr("LEWISSPLIT") == null ||
				(!simpleDoc.attr("LEWISSPLIT").equals("TRAIN") && !simpleDoc.attr("LEWISSPLIT").equals("TEST")) ||
				!simpleDoc.attr("TOPICS").equals("YES")) {
			return null;
		}

		String title = simpleDoc.child("TEXT").child("TITLE") == null ? "" :
				simpleDoc.child("TEXT").child("TITLE").getContent();
		String body = simpleDoc.child("TEXT").child("BODY") == null ? "" :
			simpleDoc.child("TEXT").child("BODY").getContent();
		
		String text = null;
		switch(whatToUse){
		case BODY:
			text = body;
			break;
		case TITLE:
			text = title;
			break;
		case TITLE_N_BODY:
			text = title + " " + body;
		}
		
		// Create a document object
		Document doc = Document.getNewDocument(simpleDoc.attr("NEWID"), text);

		// add expected categories
		if (simpleDoc.child("TOPICS") != null && 
				!simpleDoc.child("TOPICS").children("D").isEmpty()) {
			for(SimpleTagDoc child : simpleDoc.child("TOPICS").children("D")) {
				doc.getCategories().add(child.getContent());
			}
		}
		
		return doc;
	}
	
	public Tuple<DocumentSet,DocumentSet> getTrainTestSplit(){
		DocumentSet train = new DocumentSet();
		DocumentSet test = new DocumentSet();

		int currentFileIndex = 0;
		while (true){
			SimpleTagDocReader reader;
			try {
				reader = new SimpleTagDocReader(
						new FileReader(getCurrentFilePath(currentFileIndex)));
				currentFileIndex ++;
			} catch (FileNotFoundException e) {
				break;
			}
			
			SimpleTagDoc simpleDoc = reader.next(null);
			while(simpleDoc != null){
				Document doc = buildDocument(simpleDoc);
				if (simpleDoc.attr("LEWISSPLIT").equals("TRAIN")){
					train.add(doc);
				} else {
					test.add(doc);
				}
				simpleDoc = reader.next(null);
			}
		}
		
		return new Tuple<>(train, test);
	}
	
	protected String getCurrentFilePath(int currentFileIndex) {
		return directory + "/reut2-0" + (currentFileIndex < 10 ? "0" : "") + 
				new Integer(currentFileIndex).toString() + ".sgm";
	}
	
	public enum WhatToUse{
		BODY, TITLE, TITLE_N_BODY
	}
}
