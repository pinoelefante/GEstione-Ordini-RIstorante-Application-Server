package it.geori.as.data;

import java.util.ArrayList;

public class Dettaglio {
	private int id;
	private int stato;
	private Prodotto prodotto;
	private ArrayList<Ingrediente> toAdd, toRem;

	public Dettaglio(int id,Prodotto p,int stato){
		setId(id);
		this.prodotto = p;
		setStato(stato);
		toAdd = new ArrayList<Ingrediente>(3);
		toRem = new ArrayList<Ingrediente>(3);
	}
	public Dettaglio(Prodotto p){
		this(0,p,Ordine.STATO_IN_CORSO);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStato() {
		return stato;
	}
	public void setStato(int stato) {
		this.stato = stato;
	}
	public ArrayList<Ingrediente> getToAdd() {
		return toAdd;
	}
	public ArrayList<Ingrediente> getToRem() {
		return toRem;
	}
	public Prodotto getProdotto(){
		return prodotto;
	}
	public void addToAdd(ArrayList<Ingrediente> ingr){
		for(int i=0;i<ingr.size();i++)
			toAdd.add(ingr.get(i));
	}
	public void addToRem(ArrayList<Ingrediente> ingr){
		for(int i=0;i<ingr.size();i++)
			toRem.add(ingr.get(i));
	}
}
