package it.geori.as.data;

import it.geori.as.data.interfaces.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Menu implements Identifier {
	private int versioneMenu;
	private String nomeMenu;
	private String dataCreazione;
	private Map<Integer,Prodotto> prodotti;
	
	public Menu(int menu, String ver, String data){
		versioneMenu = menu;
		nomeMenu = ver;
		dataCreazione = data;
		prodotti = new HashMap<Integer, Prodotto>();
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
	public Map<Integer,Prodotto> getListProdotti(){
		return prodotti;
	}
	public void addItemToMenu(Prodotto p){
		prodotti.put(p.getID(), p);
	}
	public void removeItemFromMenu(Prodotto p){
		prodotti.remove(p.getID());
	}
	public void removeItemFromMenu(int id){
		prodotti.remove(id);
	}

	@Override
	public int getID() {
		return getVersioneMenu();
	}
}
