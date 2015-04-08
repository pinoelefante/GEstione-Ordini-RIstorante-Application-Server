package it.geori.as.data;

import it.geori.as.data.interfaces.Identifier;

import java.util.ArrayList;

public class Menu implements Identifier {
	private int versioneMenu;
	private String nomeMenu;
	private String dataCreazione;
	private ArrayList<Prodotto> prodotti;
	
	public Menu(int menu, String ver, String data){
		versioneMenu = menu;
		nomeMenu = ver;
		dataCreazione = data;
		prodotti = new ArrayList<Prodotto>();
	}
	
	public int getVersioneMenu() {
		return versioneMenu;
	}
	public void setVersioneMenu(int v){
		versioneMenu = v;
	}
	public String getNomeMenu() {
		return nomeMenu;
	}
	public String getDataCreazione() {
		return dataCreazione;
	}
	public void setDataCreazione(String d){
		this.dataCreazione = d;
	}
	public ArrayList<Prodotto> getListProdotti(){
		return prodotti;
	}

	@Override
	public int getID() {
		return getVersioneMenu();
	}
}
