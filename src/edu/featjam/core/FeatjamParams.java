package edu.featjam.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class FeatjamParams {
	public static final String CATEGORY = "category";

	protected static FeatjamParams global = new FeatjamParams();
	protected static Map<Thread, FeatjamParams> perThreadParams = Collections.synchronizedMap(new HashMap<>());
		
	protected Map<String,Object> params = Collections.synchronizedMap(new HashMap<>());
	
	protected static synchronized void clearRedundant(){
		if(perThreadParams.size() > 30){
			HashSet<Thread> toRemove = new HashSet<>();
			perThreadParams.forEach((k,v)->{
				if(!k.isAlive()){
					toRemove.add(k);
				}
			});
			toRemove.forEach((k)->perThreadParams.remove(k));
		}
	}
	
	public static synchronized Object get(String key){
		clearRedundant();
		if(perThreadParams.containsKey(Thread.currentThread()) &&
				perThreadParams.get(Thread.currentThread()).params.containsKey(key)){
			return perThreadParams.get(Thread.currentThread()).params.get(key);
		}else{
			return global.params.get(key);
		}
	}

	public static synchronized Object get(String key, Object defaultValue){
		clearRedundant();
		if(perThreadParams.containsKey(Thread.currentThread()) &&
				perThreadParams.get(Thread.currentThread()).params.containsKey(key)){
			return perThreadParams.get(Thread.currentThread()).params.getOrDefault(key, defaultValue);
		}else{
			return global.params.getOrDefault(key, defaultValue);
		}
	}
	
	public static synchronized void setLocal(String key, Object value){
		if(!perThreadParams.containsKey(Thread.currentThread())){
			perThreadParams.put(Thread.currentThread(), new FeatjamParams());
		}
		perThreadParams.get(Thread.currentThread()).params.put(key, value);
	}

	public static void setGlobal(String key, Object value){
		setLocal(key, value);
		global.params.put(key, value);
	}
	public static void setGlobalIfNotSet(String key, Object value){
		setLocal(key, value);
		if(global.params.get(key)==null){
			global.params.put(key, value);
		}
	}
}
