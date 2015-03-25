package it.geori.as.controllers.tester;

import it.geori.as.controllers.DBTavoli;
import it.geori.as.data.Tavolo;

public class DBTavoliTester {

	public static void main(String[] args) {
		DBTavoli db = DBTavoli.getInstance();
		db.addTable(new Tavolo(0, 1.5, "Tavolo VIP"));
	}

}
