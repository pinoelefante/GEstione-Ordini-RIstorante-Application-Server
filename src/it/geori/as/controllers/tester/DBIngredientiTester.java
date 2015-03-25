package it.geori.as.controllers.tester;

import it.geori.as.controllers.DBIngredienti;
import it.geori.as.data.Ingrediente;
import it.geori.as.data.interfaces.Identifier;

public class DBIngredientiTester {

	public static void main(String[] args) {
		DBIngredienti db = DBIngredienti.getInstance();
		
		for(Identifier ing : db.getCache()){
			Ingrediente ing1 = (Ingrediente)ing;
			System.out.println(ing1.getID()+" - " +ing1.getNome());
		}
	}

}
