package it.geori.as.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DBUtenti {
	private static DBUtenti instance;
	
	public final static String TABLE_NAME="dipendenti", 
			COLUMN_USERNAME="username", 
			COLUMN_PASSWORD="password",
			COLUMN_ID="id",
			COLUMN_NOME="nome",
			COLUMN_COGNOME="cognome",
			COLUMN_LIVELLO="livello_autorizzazione";
	
	public static DBUtenti getInstance(){
		if(instance == null)
			instance = new DBUtenti();
		return instance;
	}
	private DBUtenti(){}
	
	public Map<String,String> login(String username, String password){
		String query = "SELECT * FROM "+TABLE_NAME+" WHERE "+COLUMN_USERNAME+"=\""+username+"\" AND "+COLUMN_PASSWORD+"=\""+password+"\"";
		Connection con;
		try {
			con = DBConnectionPool.getConnection();
		} 
		catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		Map<String, String> ret = null;
		try {
			PreparedStatement st = con.prepareStatement(query);
			ResultSet rs = st.executeQuery();
			if(rs.next()){
				ret = new HashMap<String, String>();
				ret.put("nome", rs.getString(COLUMN_NOME)+" "+rs.getString(COLUMN_COGNOME));
				ret.put("livello", rs.getInt(COLUMN_LIVELLO)+"");
			}
			rs.close();
			st.close();
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		DBConnectionPool.releaseConnection(con);
		return ret;
	}
	public boolean registraUtente(String nome, String cognome, String username, String password, int livello){
		String query = "INSERT INTO "+TABLE_NAME+" ("+COLUMN_NOME+","+COLUMN_COGNOME+","+COLUMN_USERNAME+","+COLUMN_PASSWORD+","+COLUMN_LIVELLO+") VALUES ("+
				"\""+nome+"\",\""+cognome+"\",\""+username+"\",\""+password+"\","+livello+")";
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
}
