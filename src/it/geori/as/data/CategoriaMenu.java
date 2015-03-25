package it.geori.as.data;

import it.geori.as.data.interfaces.Identifier;

public class CategoriaMenu implements Identifier {
	private int idCategoria, versioneMenu;
	private String nomeCategoria;
	
	public CategoriaMenu(int id, int vers, String nome){
		idCategoria = id;
		versioneMenu = vers;
		nomeCategoria = nome;
	}
	
	public int getIdCategoria() {
		return idCategoria;
	}
	public int getVersioneMenu() {
		return versioneMenu;
	}
	public String getNomeCategoria() {
		return nomeCategoria;
	}

	@Override
	public int getID() {
		return getIdCategoria();
	}
}
