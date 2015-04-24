package it.geori.as.controllers;

import it.geori.as.data.Ingrediente;
import it.geori.as.data.Ordine;
import it.geori.as.data.OrdineDettagli;
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
import java.util.HashMap;
import java.util.Map;

public class DBOrdini extends CacheManager {
	private final static String 
		TABLE_ORDINE_NAME="ordine",
		COLUMN_ORDINE_ID="id_ordine",
		COLUMN_ORDINE_TAVOLO="tavolo",
		COLUMN_ORDINE_COPERTI="coperti",
		COLUMN_ORDINE_DATA_ORDINE="data_ordine",
		COLUMN_ORDINE_DATA_CHIUSURA="chiusura_ordine",
		COLUMN_ORDINE_SCONTO="sconto",
		COLUMN_ORDINE_TOTALE="totale_ordine",
		COLUMN_ORDINE_STATO_ORDINE="stato_ordine",
		COLUMN_ORDINE_SERVITO_DA="servito_da",
		COLUMN_ORDINE_GUEST_CODE="guestCodeAccess";
	
	private final static String
		TABLE_ORDINE_DETTAGLI_NAME="ordine_dettagli",
		COLUMN_ORDINE_DETTAGLI_ID="id",
		COLUMN_ORDINE_DETTAGLI_ID_ORDINE="id_ordine",
		COLUMN_ORDINE_DETTAGLI_ID_PRODOTTO="id_prodotto",
		COLUMN_ORDINE_DETTAGLI_QUANTITA="quantita",
		COLUMN_ORDINE_DETTAGLI_UNITO_A="unito_a",
		COLUMN_ORDINE_DETTAGLI_STATO="stato",
		COLUMN_ORDINE_DETTAGLI_NOTE="note";
		
	
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
		String query = "SELECT "+COLUMN_ORDINE_GUEST_CODE+" FROM "+TABLE_ORDINE_NAME+" WHERE "+COLUMN_ORDINE_GUEST_CODE+"=\""+orderCode+"\"";
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
	public boolean addNewOrder(Ordine ordine){
		String guestCode;
		do {
			guestCode = getRandomString(setNormalized);
		}
		while(isOrderCodeExists(guestCode));
		ordine.setGuestCode(guestCode);
		String query = "INSERT INTO "+TABLE_ORDINE_NAME+" ("+COLUMN_ORDINE_TAVOLO+","+COLUMN_ORDINE_COPERTI+","+COLUMN_ORDINE_STATO_ORDINE+","+COLUMN_ORDINE_SERVITO_DA+","+COLUMN_ORDINE_GUEST_CODE+") VALUES ("+
				ordine.getTavolo()+","+ordine.getCoperti()+","+Ordine.STATO_CREATO+","+ordine.getServitoDa()+",\""+ordine.getGuestCode()+"\")";
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
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			if(st.executeUpdate()>0){
				res = true;
				con.commit();
				String data = DateFormat.getInstance().format(new Date());
				ordine.setDataCreazione(data);
				ResultSet rs = st.getGeneratedKeys();
				if(rs.next()){
					int id = rs.getInt(1);
					ordine.setId(id);
				}
				rs.close();
				addItemToCache(ordine);
			}
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
		finally {
			if(st!=null){
				try {
					st.close();
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
			DBConnectionPool.releaseConnection(con);
		}
		return res;
	}
	public Ordine getOrdine(int id){
		//
		Identifier order = getItem(id);
		if(order!=null)
			return (Ordine)order;
		else {
			String query = "SELECT * FROM "+TABLE_ORDINE_NAME+" WHERE "+COLUMN_ORDINE_ID+"="+id;
			Connection con = null;
			PreparedStatement st = null;
			ResultSet rs = null;
			try {
				con = DBConnectionPool.getConnection();
				st = con.prepareStatement(query);
				rs = st.executeQuery();
				if(rs.next()){
					int id_ordine = rs.getInt(COLUMN_ORDINE_ID);
					int tavolo = rs.getInt(COLUMN_ORDINE_TAVOLO);
					int coperti = rs.getInt(COLUMN_ORDINE_COPERTI);
					String data_ordine = rs.getString(COLUMN_ORDINE_DATA_ORDINE);
					String data_chiusura = rs.getString(COLUMN_ORDINE_DATA_CHIUSURA);
					int sconto = rs.getInt(COLUMN_ORDINE_SCONTO);
					float totale = rs.getFloat(COLUMN_ORDINE_TOTALE);
					int stato = rs.getInt(COLUMN_ORDINE_STATO_ORDINE);
					int servitoDa = rs.getInt(COLUMN_ORDINE_SERVITO_DA);
					String guestCode = rs.getString(COLUMN_ORDINE_GUEST_CODE);
					Ordine ordine = new Ordine(id_ordine, tavolo, coperti, sconto, servitoDa, stato, totale, data_ordine, data_chiusura, guestCode);
					if(getOrdineDettagli(con, ordine)){
						addItemToCache(ordine);
					}
					else
						throw new SQLException("errore getOrdineDettagli");
					
					return ordine;
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			finally {
				if(rs!=null)
					try {
						rs.close();
					}
					catch (SQLException e) {
						e.printStackTrace();
					}
				if(st!=null)
					try {
						st.close();
					}
					catch (SQLException e) {
						e.printStackTrace();
					}
				DBConnectionPool.releaseConnection(con);
			}
		}
		return null;
	}
	private boolean getOrdineDettagli(Connection con, Ordine ordine){
		String query = "SELECT * FROM "+TABLE_ORDINE_DETTAGLI_NAME+" WHERE "+COLUMN_ORDINE_DETTAGLI_ID_ORDINE+"="+ordine.getID();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(query);
			rs = st.executeQuery();
			ArrayList<OrdineDettagli> dettagli = new ArrayList<OrdineDettagli>();
			while(rs.next()){
				OrdineDettagli dett = parseOrdineDettaglio(con,rs);
				if(!isDettagliInserito(dettagli, dett.getID())){
					dettagli.add(dett);
				}
			}
			return true;
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			if(rs!=null)
				try {
					rs.close();
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
			if(st!=null)
				try {
					st.close();
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return false;
	}
	private Map<String,ArrayList<Ingrediente>> getOrdineModifiche(Connection con, int id){
		//TODO
		return null;
	}
	private OrdineDettagli parseOrdineDettaglio(Connection con, ResultSet rs) throws SQLException{
		int id = rs.getInt(COLUMN_ORDINE_DETTAGLI_ID);
		int id_prodotto = rs.getInt(COLUMN_ORDINE_DETTAGLI_ID_PRODOTTO);
		//int id_ordine = rs.getInt(COLUMN_ORDINE_DETTAGLI_ID_ORDINE);
		int quantita = rs.getInt(COLUMN_ORDINE_DETTAGLI_QUANTITA);
		Integer unito_a = rs.getInt(COLUMN_ORDINE_DETTAGLI_UNITO_A);
		int stato = rs.getInt(COLUMN_ORDINE_DETTAGLI_STATO);
		String note = rs.getString(COLUMN_ORDINE_DETTAGLI_NOTE);
		
		Prodotto prod = DBProdotti.getInstance().getProdottoByID(id_prodotto);
	
		if(unito_a!=null && unito_a>0){
			OrdineDettagli dett = getOrdineDettaglio(unito_a);
			Map<String,ArrayList<Ingrediente>> modifiche = getOrdineModifiche(con,id);
			dett.addModificheToProdotto(prod, modifiche);
			return dett;
		}
		else {
			Map<Prodotto, Map<String,ArrayList<Ingrediente>>> map = new HashMap<>();
			map.put(prod, getOrdineModifiche(con,id));
			OrdineDettagli dett = new OrdineDettagli(id, quantita, stato, note, map);
			return dett;
		}
	}
	private OrdineDettagli getOrdineDettaglio(int id){
		//TODO
		return null;
	}
	private boolean isDettagliInserito(ArrayList<OrdineDettagli> list, int id){
		//TODO
		return false;
	}
}
