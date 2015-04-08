package it.geori.as.data;

import it.geori.as.data.interfaces.Identifier;

public class ProdottoCategoria implements Identifier {
	private int idCategoria;
	private String nomeCategoria;
	
	public ProdottoCategoria(int id, String nome){
		idCategoria = id;
		nomeCategoria = nome;
	}
	
	public int getIdCategoria() {
		return idCategoria;
	}
	public String getNomeCategoria() {
		return nomeCategoria;
	}

	@Override
	public int getID() {
		return getIdCategoria();
	}
}
