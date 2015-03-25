package it.geori.as.data;

import it.geori.as.data.interfaces.Identifier;

public class Ingrediente implements Identifier {
	private int id;
	private String nome;
	private double prezzo;
	public Ingrediente(int i, String s, double p){
		id = i;
		nome = s;
		prezzo = p;
	}
	public int getId() {
		return id;
	}
	public String getNome() {
		return nome;
	}
	public double getPrezzo() {
		return prezzo;
	}
	public void setID(int i){
		id = i;
	}
	@Override
	public int getID() {
		return getId();
	}
}
