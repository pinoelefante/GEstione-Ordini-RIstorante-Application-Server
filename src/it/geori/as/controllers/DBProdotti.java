package it.geori.as.controllers;

import it.geori.as.data.Ingrediente;
import it.geori.as.data.Prodotto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;

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
					boolean insIngr = addProdottiDettagli(p.getID(), p.getIngredienti());
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
	public boolean updateProdotto(Prodotto p){
		String query = "UPDATE "+TABLE_NAME_PRODOTTI+" SET "+COLUMN_PRODOTTO_CATEGORIA+"="+p.getIdCategoria()+","
				+COLUMN_PRODOTTO_DESCRIZIONE+"=\""+p.getDescrizione()+"\","
				+COLUMN_PRODOTTO_NOME+"=\""+p.getNomeProdotto()+"\","
				+COLUMN_PRODOTTO_PREZZO+"="+p.getPrezzo()+
				" WHERE "+COLUMN_PRODOTTO_ID+"="+p.getID();
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
				Prodotto p_cache = getProdottoByID(p.getID());
				if(p_cache==null){
					res = false;
					con.rollback(sp);
				}
				else {
					ArrayList<Ingrediente> toAdd = getIngredientiToAdd(p_cache, p);
					ArrayList<Ingrediente> toRemove = getIngredientiToRemove(p_cache, p);
					boolean add = addProdottiDettagli(p.getID(), toAdd);
					boolean toRem = removeProdottiDettagli(p.getID(), toRemove);
					if(add && toRem){
						con.commit();
						res = true;
					}
					else {
						con.rollback(sp);
						res = false;
					}
				}
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
	private ArrayList<Ingrediente> getIngredientiToRemove(Prodotto p1, Prodotto p2){
		return null;
	}
	private ArrayList<Ingrediente> getIngredientiToAdd(Prodotto p1, Prodotto p2){
		return null;
	}
	public Prodotto getProdottoByID(int id){
		Prodotto p = (Prodotto) getItem(id);
		if(p==null){
			try {
				String query = "SELECT * FROM "+TABLE_NAME_PRODOTTI+" WHERE "+COLUMN_PRODOTTO_ID+"="+id;
				Connection con = DBConnectionPool.getConnection();
				PreparedStatement st = con.prepareStatement(query);
				ResultSet rs = st.executeQuery();
				if(rs.next()){
					int idP = rs.getInt(COLUMN_PRODOTTO_ID);
					int idCat = rs.getInt(COLUMN_PRODOTTO_CATEGORIA);
					String nome = rs.getString(COLUMN_PRODOTTO_NOME);
					double prezzo = rs.getDouble(COLUMN_PRODOTTO_PREZZO);
					String desc = rs.getString(COLUMN_PRODOTTO_DESCRIZIONE);
					p = new Prodotto(idCat, idP, nome, desc, prezzo);
					addItemToCache(p);
					String q2 = "SELECT dettagli.id_ingrediente FROM prodotti AS prod JOIN prodotti_dettagli AS dettagli WHERE prod.id_prodotto=dettagli.id_prodotto";
					PreparedStatement stP = con.prepareStatement(q2);
					ResultSet rsP = stP.executeQuery();
					while(rsP.next()){
						int idIngr = rsP.getInt(COLUMN_PRODOTTO_DETT_INGREDIENTE);
						Ingrediente ingr = DBIngredienti.getInstance().getIngredienteByID(idIngr);
						if(ingr!=null){
							p.getIngredienti().add(ingr);
						}
					}
					rsP.close();
					stP.close();
				}
				rs.close();
				st.close();
				DBConnectionPool.releaseConnection(con);
			} 
			catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}
		return p;
	}
	private boolean addProdottiDettagli(int idProdotto, ArrayList<Ingrediente> ingr){
		Connection con;
		Savepoint sp;
		try {
			con = DBConnectionPool.getConnection();
			sp = con.setSavepoint();
			PreparedStatement st;
			boolean insIngr = true;
			for(Ingrediente ing : ingr){
				String q = "INSERT INTO "+TABLE_NAME_PRODOTTI_DETT+" ("+COLUMN_PRODOTTO_DETT_PRODOTTO+","+COLUMN_PRODOTTO_DETT_INGREDIENTE+")"+
						" VALUES ("+idProdotto+","+ing.getID()+")";
				st = con.prepareStatement(q);
				if(st.executeUpdate()<=0){
					insIngr = false;
					break;
				}
			}
			if(!insIngr){
				con.rollback(sp);
			}
			return insIngr;
		} 
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	private boolean removeProdottiDettagli(int idProdotto, ArrayList<Ingrediente> ingr){
		Connection con;
		Savepoint sp;
		try {
			con = DBConnectionPool.getConnection();
			sp = con.setSavepoint();
			PreparedStatement st;
			boolean insIngr = true;
			for(Ingrediente ing : ingr){
				String q = "DELETE FROM "+TABLE_NAME_PRODOTTI_DETT+" WHERE "+COLUMN_PRODOTTO_DETT_INGREDIENTE+"="+ing.getId()+
						" AND "+COLUMN_PRODOTTO_DETT_PRODOTTO+"="+idProdotto;
				st = con.prepareStatement(q);
				if(st.executeUpdate()<=0){
					insIngr = false;
					break;
				}
			}
			if(!insIngr){
				con.rollback(sp);
			}
			return insIngr;
		} 
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
