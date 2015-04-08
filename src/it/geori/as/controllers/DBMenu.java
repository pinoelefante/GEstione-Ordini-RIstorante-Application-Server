package it.geori.as.controllers;

import it.geori.as.data.Menu;
import it.geori.as.data.Prodotto;
import it.geori.as.data.interfaces.Identifier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;

public class DBMenu extends CacheManager {
	private static DBMenu instance;
	
	public static DBMenu getInstance(){
		if(instance==null)
			instance = new DBMenu();
		return instance;
	}
	
	private final static String TABLE_NAME="menu", 
			COLUMN_ID="versione_menu", 
			COLUMN_NOME="nome_menu", 
			COLUMN_DATA="data_creazione";
	
	private DBMenu(){
		super();
		loadAllMenu();
	}
	
	public boolean addMenu(Menu m){
		String query = "INSERT INTO "+TABLE_NAME + " ("+COLUMN_NOME+") VALUES (\""+m.getNomeMenu()+"\")";
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
					int id = rs.getInt(1);
					String data = DateFormat.getInstance().format(new Date());
					m.setVersioneMenu(id);
					m.setDataCreazione(data);
					con.commit();
					res = true;
					addItemToCache(m);
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
	public boolean removeMenu(int id){
		String query = "DELETE FROM "+TABLE_NAME + " WHERE "+COLUMN_ID+"="+id;
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
				con.commit();
				removeItemFromCache(id);
				res = true;
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
	public boolean updateMenu(Menu m){
		String query = "UPDATE "+TABLE_NAME+" SET "+COLUMN_NOME+"=\""+m.getNomeMenu()+"\" WHERE "+COLUMN_ID+"="+m.getID();
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
				updateItemToCache(m);
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
	private void loadAllMenu(){
		String query = "SELECT * FROM "+TABLE_NAME + " ORDER BY "+COLUMN_DATA+" DESC";
		Connection con;
		try {
			con = DBConnectionPool.getConnection();
			PreparedStatement st = con.prepareStatement(query);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				int vers = rs.getInt(COLUMN_ID);
				String nome = rs.getString(COLUMN_NOME);
				String data = rs.getTimestamp(COLUMN_DATA).toString();
				Menu m = new Menu(vers, nome, data);
				addItemToCache(m);
			}
			rs.close();
			st.close();
			DBConnectionPool.releaseConnection(con);
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public Menu getMenuByID(int id){
		Menu m = (Menu) getItem(id);
		if(m==null){
			String query = "SELECT * FROM "+TABLE_NAME + " WHERE "+COLUMN_ID+"="+id;
			try {
				Connection con = DBConnectionPool.getConnection();
				PreparedStatement st = con.prepareStatement(query);
				ResultSet rs = st.executeQuery();
				if(rs.next()){
					int idI = rs.getInt(COLUMN_ID);
					String nome = rs.getString(COLUMN_NOME);
					String data = rs.getString(COLUMN_DATA);
					m = new Menu(idI, nome, data);
					addItemToCache(m);
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
		return m;
	}
	public ArrayList<Menu> getList(){
		ArrayList<Menu> list = new ArrayList<Menu>();
		for(Entry<Integer, Identifier> i : getCache().entrySet()){
			Menu m = (Menu)(i.getValue());
			if(list.isEmpty())
				list.add(m);
			else {
				boolean insOK = false;
				for(int j=0;j<list.size();j++){
					if(m.getVersioneMenu()>list.get(j).getVersioneMenu()){
						list.add(j, m);
						insOK = true;
						break;
					}
				}
				if(!insOK)
					list.add(m);
			}
		}
		return list;
	}
	public boolean cloneMenuFrom(Menu oldMenu){
		//TODO
		return false;
	}
	public boolean addItemToMenu(Menu m, Prodotto p){
		//TODO
		return false;
	}
	public boolean removeItemFromMenu(Menu m, Prodotto p){
		//TODO
		return false;
	}
	protected ArrayList<Prodotto> getListProdottiMenu(int id){
		//TODO
		return null;
	}
}
