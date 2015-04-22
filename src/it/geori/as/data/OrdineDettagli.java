package it.geori.as.data;

import it.geori.as.data.interfaces.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrdineDettagli implements Identifier{
	private int id;
	private Map<Prodotto, Map<String,ArrayList<Ingrediente>>> prodotti;
	private int quantita;
	private String note;
	
	public OrdineDettagli(Prodotto prod){
		prodotti = new HashMap<Prodotto, Map<String,ArrayList<Ingrediente>>>();
		Map<String,ArrayList<Ingrediente>> modifiche = new HashMap<String, ArrayList<Ingrediente>>(2);
		prodotti.put(prod, modifiche);
	}
	public OrdineDettagli(){
		prodotti = new HashMap<Prodotto, Map<String,ArrayList<Ingrediente>>>();
	}
	public void addProdotto(Prodotto p, int quantita){
		Map<String,ArrayList<Ingrediente>> modifiche = new HashMap<String, ArrayList<Ingrediente>>(2);
		prodotti.put(p, modifiche);
		this.quantita = quantita;
	}
	public void setQuantita(int newQuantita){
		if(newQuantita==0){
			prodotti.clear();
			quantita = 0;
		}
		else
			quantita = newQuantita;
	}
	public int getQuantita(){
		return quantita;
	}
	@Override
	public int getID() {
		return id;
	}
	public void setNote(String note){
		this.note = note;
	}
	public String getNote(){
		return note;
	}
	public void ingredientiToRemove(Prodotto p, Ingrediente ingr){
		if(isToRemove(p, ingr)){
			ArrayList<Ingrediente> l=prodotti.get(p).get("-");
			for(int i=0;i<l.size();i++){
				if(l.get(i).getID()==ingr.getID()){
					l.remove(i);
					break;
				}
			}	
		}
	}
	private boolean isToRemove(Prodotto p, Ingrediente ingr){
		ArrayList<Ingrediente> l=prodotti.get(p).get("-");
		for(int i=0;i<l.size();i++){
			if(l.get(i).getID()==ingr.getID())
				return true;
		}
		return false;
	}
	public void ingredientiToAdd(Prodotto p, Ingrediente ingr){
		if(isToAdd(p, ingr)){
			ArrayList<Ingrediente> l=prodotti.get(p).get("+");
			l.add(ingr);	
		}
	}
	private boolean isToAdd(Prodotto p, Ingrediente ingr){
		ArrayList<Ingrediente> l=prodotti.get(p).get("+");
		for(int i=0;i<l.size();i++){
			if(l.get(i).getID()==ingr.getID())
				return false;
		}
		return true;
	}
}
