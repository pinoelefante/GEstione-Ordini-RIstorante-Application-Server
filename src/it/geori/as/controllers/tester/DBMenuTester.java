package it.geori.as.controllers.tester;

import it.geori.as.controllers.DBMenu;
import it.geori.as.data.Menu;

public class DBMenuTester {

	public static void main(String[] args) {
		DBMenu db = DBMenu.getInstance();
		Menu m = new Menu(0, "Menu della casa", "");
		db.addMenu(m);
		System.out.println(m.getID()+" - "+m.getNomeMenu()+" - "+m.getDataCreazione());
	}

}
