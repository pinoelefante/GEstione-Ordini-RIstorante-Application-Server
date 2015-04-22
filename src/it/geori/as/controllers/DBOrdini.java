package it.geori.as.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBOrdini extends CacheManager {
	private final static String 
		TABLE_NAME="ordine",
		COLUMN_ID="id_ordine",
		COLUMN_TAVOLO="tavolo",
		COLUMN_COPERTI="coperti",
		COLUMN_DATA_ORDINE="data_ordine",
		COLUMN_DATA_CHIUSURA="chiusura_ordine",
		COLUMN_SCONTO="sconto",
		COLUMN_TOTALE="totale_ordine",
		COLUMN_STATO_ORDINE="stato_ordine",
		COLUMN_SERVITO_DA="servito_da",
		COLUMN_GUEST_CODE="guestCodeAccess";
	
	private static DBOrdini instance;
	public static DBOrdini getInstance(){
		if(instance == null){
			instance = new DBOrdini();
		}
		return instance;
	}
	private DBOrdini(){
		super();
	}
	
	public boolean isOrderCodeExists(String orderCode){
		String query = "SELECT "+COLUMN_GUEST_CODE+" FROM "+TABLE_NAME+" WHERE "+COLUMN_GUEST_CODE+"=\""+orderCode+"\"";
		Connection con;
		try {
			con = DBConnectionPool.getConnection();
		}
		catch (SQLException e) {
			e.printStackTrace();
			return true;
		}
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(query);
			rs = st.executeQuery();
			if(rs.next())
				return true;
			else
				return false;
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			if(st!=null){
				try {
					st.close();
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(rs!=null){
				try {
					rs.close();
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
			DBConnectionPool.releaseConnection(con);
		}
		return true;
	}
	
	private final static String setNormalized = "abc0def1ghi2jk3lm4no5pqr6st7u8vwx9yz";
	private static String getRandomString(String set) {
		String indici = (System.currentTimeMillis()+"").substring(4); //8 indici
		StringBuilder sb = new StringBuilder();

		for (int loop = 0; loop < indici.length(); loop++) {
			int index = Integer.parseInt(indici.charAt(loop)+"");
			sb.append(set.charAt(index));
		}

		String nonce = sb.toString();
		return nonce;
	}
	
}
