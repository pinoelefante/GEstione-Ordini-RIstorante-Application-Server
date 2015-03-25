package it.geori.as.controllers;

import it.geori.as.data.Ingrediente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

public class DBIngredienti extends CacheManager {
	private static DBIngredienti instance;
	
	public static DBIngredienti getInstance(){
		if(instance == null){
			instance = new DBIngredienti();
		}
		return instance;
	}
	
	private DBIngredienti(){
		super();
		loadAllIngredienti();
	}
	
	private final static String TABLE_NAME_INGREDIENTI = "ingredienti",
			COLUMN_INGREDIENTI_ID="id_ingrediente",
			COLUMN_INGREDIENTI_NOME="nome_ingrediente",
			COLUMN_INGREDIENTI_PREZZO="prezzo_ingrediente";
	
	public boolean addIngrediente(Ingrediente i){
		String query = "INSERT INTO "+TABLE_NAME_INGREDIENTI+" ("+COLUMN_INGREDIENTI_NOME+","+COLUMN_INGREDIENTI_PREZZO+") VALUES (\""+i.getNome()+"\","+i.getPrezzo()+")";
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
			if(st.executeUpdate(query, Statement.RETURN_GENERATED_KEYS)>0){
				ResultSet rs = st.getGeneratedKeys();
				if(rs.next()){
					int idIngrediente = rs.getInt(1);
					i.setID(idIngrediente);
					res = true;
					con.commit();
					addItemToCache(i);
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
	public boolean removeIngrediente(int id){
		String query = "DELETE FROM "+TABLE_NAME_INGREDIENTI+ " WHERE "+COLUMN_INGREDIENTI_ID+"="+id;
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
	public boolean updateIngrediente(Ingrediente ing){
		String query = "UPDATE "+TABLE_NAME_INGREDIENTI+ " SET "+COLUMN_INGREDIENTI_NOME+"=\""+ing.getNome()+"\","+COLUMN_INGREDIENTI_PREZZO+"="+ing.getPrezzo()+" WHERE "+COLUMN_INGREDIENTI_ID+"="+ing.getId();
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
				updateItemToCache(ing);
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
	private void loadAllIngredienti(){
		String q = "SELECT * FROM "+TABLE_NAME_INGREDIENTI+" ORDER BY LOWER("+COLUMN_INGREDIENTI_NOME+") ASC";
		Connection con;
		try {
			con = DBConnectionPool.getConnection();
		}
		catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		try {
			PreparedStatement st = con.prepareStatement(q);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				int id = rs.getInt(COLUMN_INGREDIENTI_ID);
				String nome = rs.getString(COLUMN_INGREDIENTI_NOME);
				double prezzo = rs.getDouble(COLUMN_INGREDIENTI_PREZZO);
				Ingrediente ing = new Ingrediente(id, nome, prezzo);
				addItemToCache(ing);
			}
			rs.close();
			st.close();
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		DBConnectionPool.releaseConnection(con);
	}
}
