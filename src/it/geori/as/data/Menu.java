package it.geori.as.data;

import it.geori.as.data.interfaces.Identifier;

public class Menu implements Identifier {
	private int versioneMenu;
	private String nomeMenu;
	private String dataCreazione;
	
	public Menu(int menu, String ver, String data){
		versioneMenu = menu;
		nomeMenu = ver;
		dataCreazione = data;
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

	@Override
	public int getID() {
		return getVersioneMenu();
	}
}
