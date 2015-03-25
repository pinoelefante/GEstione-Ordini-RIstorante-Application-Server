package it.geori.as.data;

import it.geori.as.data.interfaces.Identifier;

public class Tavolo implements Identifier{
	private int id_tavolo;
	private double costo_coperto;
	private String nome_tavolo;
	
	public Tavolo(int id, double coperto, String nome){
		this.id_tavolo = id;
		this.costo_coperto = coperto;
		this.nome_tavolo = nome;
	}
	
	public int getIDTavolo() {
		return id_tavolo;
	}
	public void setIDTavolo(int i){
		this.id_tavolo = i;
	}
	public double getCostoCoperto() {
		return costo_coperto;
	}
	public String getNomeTavolo() {
		return nome_tavolo;
	}

	@Override
	public int getID() {
		return getIDTavolo();
	}
}
