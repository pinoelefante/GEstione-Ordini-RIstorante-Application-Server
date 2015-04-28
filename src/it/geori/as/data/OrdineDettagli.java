package it.geori.as.data;

import it.geori.as.data.interfaces.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrdineDettagli implements Identifier{
	public final static int 
		STATO_IN_CODA=0,
		STATO_IN_PREPARAZIONE=1,
		STATO_PREPARATO=2,
		STATO_INGREDIENTI_NON_PRESENTI=3,
		STATO_PAGATO = 4;
	private int id;
	private Map<Prodotto, Map<String,ArrayList<Ingrediente>>> prodotti;
	private int quantita;
	private String note;
	private int stato;
	
	public OrdineDettagli(int id, int quantita, int stato, String note, Map<Prodotto, Map<String,ArrayList<Ingrediente>>> prodotti){
		this.id = id;
		this.quantita = quantita;
		this.stato = stato;
		this.note = note;
		if(prodotti!=null)
			this.prodotti = prodotti;
		else
			prodotti = new HashMap<Prodotto, Map<String,ArrayList<Ingrediente>>>();
	}
	public OrdineDettagli(Prodotto prod){
		prodotti = new HashMap<Prodotto, Map<String,ArrayList<Ingrediente>>>();
		Map<String,ArrayList<Ingrediente>> modifiche = new HashMap<String, ArrayList<Ingrediente>>(2);
		prodotti.put(prod, modifiche);
	}
	public Map<Prodotto, Map<String, ArrayList<Ingrediente>>> getProdotti() {
		return prodotti;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void addProdotto(Prodotto p){
		Map<String,ArrayList<Ingrediente>> modifiche = new HashMap<String, ArrayList<Ingrediente>>(2);
		prodotti.put(p, modifiche);
	}
	public void addModificheToProdotto(Prodotto p, Map<String, ArrayList<Ingrediente>> mod){
		prodotti.put(p, mod);
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
	public int getStato() {
		return stato;
	}
	public void setStato(int stato) {
		this.stato = stato;
	}
	public boolean isRemovable(){
		switch(getStato()){
			case STATO_IN_CODA:
			case STATO_INGREDIENTI_NON_PRESENTI:
				return true;
		}
		return false;
	}
	public boolean isModificable(){
		switch(getStato()){
			case STATO_IN_CODA:
			case STATO_INGREDIENTI_NON_PRESENTI:
				return true;
		}
		return false;
	}
}
