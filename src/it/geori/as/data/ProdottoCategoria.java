package it.geori.as.data;

import it.geori.as.data.interfaces.Identifier;

public class ProdottoCategoria implements Identifier {
	private Integer idCategoria;
	private String nomeCategoria;
	
	public ProdottoCategoria(Integer id, String nome){
		idCategoria = id;
		nomeCategoria = nome;
	}
	
	public Integer getIdCategoria() {
		return idCategoria;
	}
	public void setID(int id){
		idCategoria = id;
	}
	public String getNomeCategoria() {
		return nomeCategoria;
	}

	@Override
	public int getID() {
		return getIdCategoria();
	}
}
