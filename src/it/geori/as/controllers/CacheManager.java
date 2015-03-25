package it.geori.as.controllers;

import it.geori.as.data.interfaces.Identifier;

import java.util.HashMap;
import java.util.Map;

public class CacheManager {
	private HashMap<Integer, Identifier> cache;
	
	public CacheManager(){
		cache = new HashMap<Integer, Identifier>();
	}
	
	public Map<Integer, Identifier> getCache(){
		return cache;
	}
	
	public void addItemToCache(Identifier i){
		cache.put(i.getID(), i);
	}
	public void updateItemToCache(Identifier t){
		cache.put(t.getID(), t);
	}
	public void removeItemFromCache(int id){
		cache.remove(id);
	}
	public Identifier getItem(int id){
		return cache.get(id);
	}
}
