package edu.featjam.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.function.BiConsumer;

public class Word2VecReader {
	String fname;

	public Word2VecReader(String fname){
		this.fname = fname;
	}

	public void forEach(BiConsumer<String, double[]> consumer){
		try {
			FileInputStream fis = new FileInputStream(this.fname);
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		 
			String[] t2 = br.readLine().split(" ");
			int embeddingSize = Integer.parseInt(t2[1]);

			String line = null;
			int k = 0;
			while ((line = br.readLine()) != null) {
				k++;
				if(k % 100000 == 0){
					System.out.println("Loading... " + k);
				}
				String[] t = line.split(" ");
				if(t.length != embeddingSize + 1){
					throw new RuntimeException("Embedding file incorrect format");
				}
				double[] e = new double[embeddingSize];
				for(int i = 0; i < embeddingSize; i++){
					e[i] = Double.parseDouble(t[i+1]);
				}
				consumer.accept(t[0], e);
			}
		 
			//normalizeStandardDev();
			
			br.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
