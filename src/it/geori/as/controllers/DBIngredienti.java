package it.geori.as.controllers;

import it.geori.as.data.Ingrediente;
import it.geori.as.data.interfaces.Identifier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map.Entry;

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
	
	protected final static String TABLE_NAME_INGREDIENTI = "ingrediente",
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
	public Ingrediente getIngredienteByID(int id){
		Ingrediente ingr = (Ingrediente) getItem(id);
		if(ingr == null){
			String query = "SELECT * FROM "+TABLE_NAME_INGREDIENTI + " WHERE "+COLUMN_INGREDIENTI_ID+"="+id;
			try {
				Connection con = DBConnectionPool.getConnection();
				PreparedStatement st = con.prepareStatement(query);
				ResultSet rs = st.executeQuery();
				if(rs.next()){
					int idI = rs.getInt(COLUMN_INGREDIENTI_ID);
					String nome = rs.getString(COLUMN_INGREDIENTI_NOME);
					double prezzo = rs.getDouble(COLUMN_INGREDIENTI_PREZZO);
					ingr = new Ingrediente(idI, nome, prezzo);
					addItemToCache(ingr);
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
		return ingr;
	}
	public ArrayList<Ingrediente> getList(){
		ArrayList<Ingrediente> list = new ArrayList<Ingrediente>();
		for(Entry<Integer, Identifier> i : getCache().entrySet()){
			Ingrediente ing = (Ingrediente)(i.getValue());
			if(list.isEmpty())
				list.add(ing);
			else {
				boolean insOK = false;
				for(int j=0;j<list.size();j++){
					if(ing.getNome().compareToIgnoreCase(list.get(j).getNome())<=0){
						list.add(j, ing);
						insOK = true;
						break;
					}
				}
				if(!insOK)
					list.add(ing);
			}
		}
		return list;
	}
}
