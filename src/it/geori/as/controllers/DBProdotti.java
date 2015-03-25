package it.geori.as.controllers;

import it.geori.as.data.Ingrediente;
import it.geori.as.data.Prodotto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

public class DBProdotti extends CacheManager {
	private static DBProdotti instance;
	
	public static DBProdotti getInstance(){
		if(instance==null)
			instance = new DBProdotti();
		return instance;
	}
	private DBProdotti(){
		super();
	}
	private final static String TABLE_NAME_PRODOTTI = "prodotti",
			COLUMN_PRODOTTO_ID="id_prodotto",
			COLUMN_PRODOTTO_CATEGORIA="categoria",
			COLUMN_PRODOTTO_NOME="nome_prodotto",
			COLUMN_PRODOTTO_PREZZO="prezzo_prodotto",
			COLUMN_PRODOTTO_DESCRIZIONE="descrizione";
	
	private final static String TABLE_NAME_PRODOTTI_DETT = "prodotti_dettagli",
			COLUMN_PRODOTTO_DETT_PRODOTTO="id_prodotto",
			COLUMN_PRODOTTO_DETT_INGREDIENTE="id_ingrediente";
	
	public boolean addProdotto(Prodotto p){
		String query = "INSERT INTO "+TABLE_NAME_PRODOTTI+" ("+COLUMN_PRODOTTO_CATEGORIA+","
				+COLUMN_PRODOTTO_DESCRIZIONE+","+COLUMN_PRODOTTO_NOME+","+COLUMN_PRODOTTO_PREZZO+") VALUES ("
				+p.getIdCategoria()+",\""+p.getDescrizione()+"\",\""+p.getNomeProdotto()+"\","+p.getPrezzo()+")";
		Connection con;
		Savepoint sp;
		try {
			con = DBConnectionPool.getConnection();
			sp = con.setSavepoint();
		} 
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		boolean res = false;
		try {
			PreparedStatement st = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			if(st.executeUpdate()>0){
				ResultSet rs = st.getGeneratedKeys();
				if(rs.next()){
					int idProdotto = rs.getInt(COLUMN_PRODOTTO_ID);
					p.setID(idProdotto);
					boolean insIngr = true;
					for(Ingrediente ing : p.getIngredienti()){
						String q = "INSERT INTO "+TABLE_NAME_PRODOTTI_DETT+" ("+COLUMN_PRODOTTO_DETT_PRODOTTO+","+COLUMN_PRODOTTO_DETT_INGREDIENTE+")"+
								" VALUES ("+p.getID()+","+ing.getID()+")";
						st = con.prepareStatement(q);
						if(st.executeUpdate()<=0){
							insIngr = false;
							break;
						}
					}
					if(insIngr){
						con.commit();
						addItemToCache(p);
						res = true;
					}
					else {
						con.rollback(sp);
						res = false;
					}
				}
				rs.close();
			}
			st.close();
		} 
		catch (SQLException e) {
			e.printStackTrace();
			try {
				con.rollback(sp);
			} 
			catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		DBConnectionPool.releaseConnection(con);
		return res;
	}
	public boolean removeProdotto(int id){
		String query = "DELETE FROM "+TABLE_NAME_PRODOTTI+" WHERE "+COLUMN_PRODOTTO_ID+"="+id;
		Connection con;
		Savepoint sp;
		try {
			con = DBConnectionPool.getConnection();
			sp = con.setSavepoint();
		} 
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		boolean res = false;
		try {
			PreparedStatement st = con.prepareStatement(query);
			if(st.executeUpdate()>0){
				res = true;
				con.commit();
				removeItemFromCache(id);
			}
			st.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			try {
				con.rollback(sp);
			} 
			catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		DBConnectionPool.releaseConnection(con);
		return res;
	}
}
