package it.geori.as.controllers;

import it.geori.as.data.interfaces.Identifier;

import java.util.ArrayList;

public class CacheManager {
	private ArrayList<Identifier> cache;
	
	public CacheManager(){
		cache = new ArrayList<Identifier>();
	}
	
	public ArrayList<Identifier> getCache(){
		return cache;
	}
	
	public void addItemToCache(Identifier i){
		cache.add(i);
	}
	public void updateItemToCache(Identifier t){
		for(int i=0;i<cache.size();i++){
			if(cache.get(i).getID()==t.getID()){
				cache.set(i, t);
				break;
			}
		}
	}
	public void removeItemFromCache(int id){
		for(int i=0;i<cache.size();i++){
			if(cache.get(i).getID()==id){
				cache.remove(i);
				break;
			}
		}
	}
}
