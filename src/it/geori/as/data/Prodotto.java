package it.geori.as.data;

import it.geori.as.data.interfaces.Identifier;

import java.util.ArrayList;

public class Prodotto implements Identifier{
	private Integer idCategoria=null, idProdotto;
	private String nomeProdotto, descrizione;
	private double prezzo;
	private ArrayList<Ingrediente> ingredienti;
	
	public Prodotto(int c, int p, String n, String d, double pr){
		idCategoria = c;
		idProdotto = p;
		nomeProdotto = n;
		descrizione = d;
		prezzo = pr;
		ingredienti = new ArrayList<Ingrediente>(5);
	}
	
	public Integer getIdCategoria() {
		return idCategoria;
	}
	public int getIdProdotto() {
		return idProdotto;
	}
	public String getNomeProdotto() {
		return nomeProdotto;
	}
	public String getDescrizione() {
		return descrizione;
	}
	public double getPrezzo() {
		return prezzo;
	}
	public ArrayList<Ingrediente> getIngredienti(){
		return ingredienti;
	}
	public void setID(int i){
		this.idProdotto = i;
	}

	@Override
	public int getID() {
		return getIdProdotto();
	}
}
