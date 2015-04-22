package it.geori.as.controllers;

import it.geori.as.data.Tavolo;
import it.geori.as.data.interfaces.Identifier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map.Entry;

public class DBTavoli extends CacheManager {
	private static DBTavoli instance;
	
	public static DBTavoli getInstance(){
		if(instance == null)
			instance = new DBTavoli();
		return instance;
	}
	
	private final static String TABLE_NAME="tavolo", 
			COLUMN_ID = "id_tavolo", 
			COLUMN_NOMETAVOLO = "nome_tavolo", 
			COLUMN_COPERTO = "costo_coperto";
	
	private DBTavoli(){
		super();
		loadAllTables();
	}
	
	private void loadAllTables(){
		String query = "SELECT * FROM "+TABLE_NAME;
		try {
			Connection con = DBConnectionPool.getConnection();
			PreparedStatement st = con.prepareStatement(query);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				int id = rs.getInt(COLUMN_ID);
				String nome = rs.getString(COLUMN_NOMETAVOLO);
				double prezzo = rs.getDouble(COLUMN_COPERTO);
				Tavolo t = new Tavolo(id, prezzo, nome);
				addItemToCache(t);
			}
			rs.close();
			st.close();
			DBConnectionPool.releaseConnection(con);
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public boolean addTable(Tavolo t){
		String query = "INSERT INTO "+TABLE_NAME+" ("+COLUMN_NOMETAVOLO+","+COLUMN_COPERTO+") VALUES (\""+t.getNomeTavolo()+"\","+t.getCostoCoperto()+")";
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
		boolean ret = false;
		
		try {
			PreparedStatement st = con.prepareStatement(query);
			if(st.executeUpdate(query, Statement.RETURN_GENERATED_KEYS)>0){
				ResultSet gkeys = st.getGeneratedKeys();
				if(gkeys.next()){
					int idTavolo = gkeys.getInt(1); 
					t.setIDTavolo(idTavolo);
					addItemToCache(t);
					ret = true;
					con.commit();
				}
				gkeys.close();
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
		return ret;
	}
	public boolean removeTable(int id){
		String query = "DELETE FROM "+TABLE_NAME+" WHERE "+COLUMN_ID+"="+id;
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
		boolean ret = false;
		try {
			PreparedStatement st = con.prepareStatement(query);
			if(st.executeUpdate()>0){
				con.commit();
				ret = true;
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
		return ret;
	}
	
	public boolean updateTable(Tavolo t){
		String query = "UPDATE "+TABLE_NAME+" SET "+COLUMN_NOMETAVOLO+"=\""+t.getNomeTavolo()+"\", "+COLUMN_COPERTO+"="+t.getCostoCoperto()+" WHERE "+COLUMN_ID+"="+t.getIDTavolo();
		Connection con;
		try {
			con = DBConnectionPool.getConnection();
		} 
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		boolean ret = false;
		try {
			PreparedStatement st = con.prepareStatement(query);
			if(st.executeUpdate(query)>0){
				updateItemToCache(t);
				ret = true;
				con.commit();
			}
			st.close();
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}
	public ArrayList<Tavolo> getList(){
		ArrayList<Tavolo> list = new ArrayList<Tavolo>();
		for(Entry<Integer, Identifier> e : getCache().entrySet()){
			Tavolo t = (Tavolo) e.getValue();
			if(list.isEmpty()){
				list.add(t);
			}
			else {
				boolean insOK = false;
				for(int i=0;i<list.size();i++){
					if(t.getID()<list.get(i).getID()){
						list.add(i, t);
						insOK = true;
						break;
					}
				}
				if(!insOK)
					list.add(t);
			}
		}
		return list;
	}
}
