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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
		COLUMN_ORDINE_GUEST_CODE="guestAccessCode";
	
	private final static String
		TABLE_ORDINE_DETTAGLI_NAME="ordine_dettagli",
		COLUMN_ORDINE_DETTAGLI_ID="id",
		COLUMN_ORDINE_DETTAGLI_ID_ORDINE="id_ordine",
		COLUMN_ORDINE_DETTAGLI_ID_PRODOTTO="id_prodotto",
		COLUMN_ORDINE_DETTAGLI_QUANTITA="quantita",
		COLUMN_ORDINE_DETTAGLI_UNITO_A="unito_a",
		COLUMN_ORDINE_DETTAGLI_STATO="stato",
		COLUMN_ORDINE_DETTAGLI_NOTE="note";
	
	private final static String
		TABLE_ORDINE_MODIFICHE_NAME = "ordine_modifiche",
		COLUMN_ORDINE_MODIFICHE_ID_DETTAGLIO = "ordine_dettaglio",
		COLUMN_ORDINE_MODIFICHE_ID_INGREDIENTE = "ingrediente",
		COLUMN_ORDINE_MODIFICHE_MODIFICA = "tipo";
		
	private final static long UN_GIORNO_MS = 60*60*24*1000;
	
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
					/*
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
					*/
					Ordine ordine = parseOrdine(rs);//new Ordine(id_ordine, tavolo, coperti, sconto, servitoDa, stato, totale, data_ordine, data_chiusura, guestCode);
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
			ordine.addDettagliOrdine(dettagli);
			while(rs.next()){
				OrdineDettagli dett = parseOrdineDettaglio(con,rs, ordine);
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
	private Map<String,ArrayList<Ingrediente>> getOrdineModifiche(Connection con, int id) {
		String query = "SELECT * FROM "+TABLE_ORDINE_MODIFICHE_NAME+ " WHERE "+COLUMN_ORDINE_MODIFICHE_ID_DETTAGLIO+"="+id;
		Map<String,ArrayList<Ingrediente>> res = new HashMap<>();
		res.put("+", new ArrayList<Ingrediente>());
		res.put("-", new ArrayList<Ingrediente>());
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(query);
			rs = st.executeQuery();
			while(rs.next()){
				int idIngr = rs.getInt(COLUMN_ORDINE_MODIFICHE_ID_INGREDIENTE);
				String tipo = rs.getString(COLUMN_ORDINE_MODIFICHE_MODIFICA);
				Ingrediente ingr = DBIngredienti.getInstance().getIngredienteByID(idIngr);
				res.get(tipo).add(ingr);
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
		}
		return res;
	}
	private Ordine parseOrdine(ResultSet rs) throws SQLException{
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
		return ordine;
	}
	private OrdineDettagli parseOrdineDettaglio(Connection con, ResultSet rs, Ordine ord) throws SQLException{
		int id = rs.getInt(COLUMN_ORDINE_DETTAGLI_ID);
		int id_prodotto = rs.getInt(COLUMN_ORDINE_DETTAGLI_ID_PRODOTTO);
		//int id_ordine = rs.getInt(COLUMN_ORDINE_DETTAGLI_ID_ORDINE);
		int quantita = rs.getInt(COLUMN_ORDINE_DETTAGLI_QUANTITA);
		Integer unito_a = rs.getInt(COLUMN_ORDINE_DETTAGLI_UNITO_A);
		int stato = rs.getInt(COLUMN_ORDINE_DETTAGLI_STATO);
		String note = rs.getString(COLUMN_ORDINE_DETTAGLI_NOTE);
		
		Prodotto prod = DBProdotti.getInstance().getProdottoByID(id_prodotto);
	
		if(unito_a!=null && unito_a>0){
			OrdineDettagli dett = getOrdineDettaglio(ord, unito_a);
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
	private OrdineDettagli getOrdineDettaglio(Ordine ordine, int idDettaglio){
		for(OrdineDettagli dett : ordine.getDettagliOrdine()){
			if(dett.getID()==idDettaglio)
				return dett;
		}
		return null;
	}
	private boolean isDettagliInserito(ArrayList<OrdineDettagli> list, int id){
		for(OrdineDettagli o : list){
			if(o.getID()==id)
				return true;
		}
		return false;
	}
	private double calcolaTotale(Ordine ordine){
		double totale = 0f;
		for(OrdineDettagli od : ordine.getDettagliOrdine()){
			Map<Prodotto,Map<String,ArrayList<Ingrediente>>> prodotti = od.getProdotti();
			int divisioneOrdine = prodotti.size();
			float parziale = 0f;
			int quantita = od.getQuantita();
			for(Entry<Prodotto, Map<String, ArrayList<Ingrediente>>> dett : prodotti.entrySet()){
				parziale += dett.getKey().getPrezzo();
				for(Ingrediente ingr : dett.getValue().get("+")){
					parziale+=ingr.getPrezzo();
				}
				for(Ingrediente ingr : dett.getValue().get("-")){
					parziale-=ingr.getPrezzo();
				}
			}
			parziale = (parziale/divisioneOrdine)*quantita;
			totale += parziale;
		}
		return totale;
	}
	public boolean calcolaTotale(int id){
		Ordine ordine = getOrdine(id);
		double totale = calcolaTotale(ordine);
		ordine.setCostoTotale(totale);
		return updateOrdine(ordine);
	}
	public boolean updateOrdine(Ordine o){
		String query = "UPDATE "+TABLE_ORDINE_NAME+" SET "+
				COLUMN_ORDINE_COPERTI+"="+o.getCoperti()+ " "+
				COLUMN_ORDINE_DATA_CHIUSURA+"=\""+o.getDataChiusura()+"\" "+
				COLUMN_ORDINE_SCONTO+"="+o.getSconto()+" "+
				COLUMN_ORDINE_STATO_ORDINE+"="+o.getStatoOrdine()+" "+
				COLUMN_ORDINE_TAVOLO+"="+o.getTavolo()+" "+
				COLUMN_ORDINE_TOTALE+"="+o.getCostoTotale()+" "+
				"WHERE "+COLUMN_ORDINE_ID+"="+o.getID();
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
			res = st.executeUpdate() > 0;
			st.close();
			if(res){
				updateItemToCache(o);
				con.commit();
			}
			return res;
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
			DBConnectionPool.releaseConnection(con);
		}
		return res;
	}
	public boolean chiudiOrdine(int id){
		Ordine ord = getOrdine(id);
		if(setStatoOrdineDettagliAll(ord, Ordine.STATO_PAGATO)){
			int lastStatus = ord.getStatoOrdine();
			String lastGuestCode = ord.getGuestCode();
			ord.setStatoOrdine(Ordine.STATO_PAGATO);
			ord.setDataChiusura(new Timestamp(System.currentTimeMillis()).toString());
			ord.setGuestCode(null);
			if(updateOrdine(ord)){
				removeItemFromCache(ord.getID());
				return true;
			}
			else {
				ord.setStatoOrdine(lastStatus);
				ord.setGuestCode(lastGuestCode);
			}
		}
		return false;
	}
	private boolean setStatoOrdineDettagliAll(Ordine o, int newStato){
		String query = "UPDATE "+TABLE_ORDINE_DETTAGLI_NAME+" SET "+COLUMN_ORDINE_DETTAGLI_STATO+"="+newStato+" WHERE "+COLUMN_ORDINE_DETTAGLI_ID_ORDINE+"="+o.getID();
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
			res = st.executeUpdate()>0;
			if(res){
				con.commit();
				for(OrdineDettagli dett : o.getDettagliOrdine()){
					dett.setStato(newStato);
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
		finally {
			DBConnectionPool.releaseConnection(con);
		}
		return res;
	}
	public boolean updateOrdineDettaglio(OrdineDettagli o){
		String query = "UPDATE "+TABLE_ORDINE_DETTAGLI_NAME+" SET "+COLUMN_ORDINE_DETTAGLI_QUANTITA+"="+o.getQuantita()+" "+
				COLUMN_ORDINE_DETTAGLI_NOTE+"\""+o.getNote()+"\" "+COLUMN_ORDINE_DETTAGLI_STATO+"="+o.getStato();
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
			res = st.executeUpdate()>0;
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
		finally {
			DBConnectionPool.releaseConnection(con);
		}
		return res;
	}
	public boolean removeOrdine(int id){
		String query = "DELETE FROM "+TABLE_ORDINE_NAME+" WHERE "+COLUMN_ORDINE_ID+"="+id;
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
			res = st.executeUpdate()>0;
			if(res){
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
		finally {
			DBConnectionPool.releaseConnection(con);
		}
		return res;
	}
	public ArrayList<Ordine> getOrdiniUltime24OreAperti(){
		String query = "SELECT * FROM "+TABLE_ORDINE_NAME+" WHERE "+COLUMN_ORDINE_STATO_ORDINE+"!="+Ordine.STATO_PAGATO
				+" AND "+COLUMN_ORDINE_DATA_ORDINE+">"+(new Timestamp(System.currentTimeMillis()-UN_GIORNO_MS))+" ORDER BY "+COLUMN_ORDINE_DATA_ORDINE+" DESC";
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		ArrayList<Ordine> ordini = new ArrayList<>();
		try {
			con = DBConnectionPool.getConnection();
			st = con.prepareStatement(query);
			rs = st.executeQuery();
			while(rs.next()){
				Ordine o = parseOrdine(rs);
				ordini.add(o);
			}
			return ordini;
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
		return ordini;
	}
	public ArrayList<Ordine> getOrdiniUltime24Ore(){
		String query = "SELECT * FROM "+TABLE_ORDINE_NAME+" WHERE "+COLUMN_ORDINE_DATA_ORDINE+">"+(new Timestamp(System.currentTimeMillis()-UN_GIORNO_MS))+" ORDER BY "+COLUMN_ORDINE_DATA_ORDINE+" DESC";
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		ArrayList<Ordine> ordini = new ArrayList<>();
		try {
			con = DBConnectionPool.getConnection();
			st = con.prepareStatement(query);
			rs = st.executeQuery();
			while(rs.next()){
				Ordine o = parseOrdine(rs);
				ordini.add(o);
			}
			return ordini;
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
		return ordini;
	}
	public ArrayList<Ordine> getOrdineByTavolo(int tavolo){
		String query = "SELECT * FROM "+TABLE_ORDINE_NAME+" WHERE "+COLUMN_ORDINE_TAVOLO+"="+tavolo+/*" AND "+COLUMN_ORDINE_STATO_ORDINE+"!="+Ordine.STATO_PAGATO+*/" ORDER BY "+COLUMN_ORDINE_DATA_ORDINE+" DESC LIMIT 3";
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		ArrayList<Ordine> ordini = new ArrayList<>();
		try {
			con = DBConnectionPool.getConnection();
			st = con.prepareStatement(query);
			rs = st.executeQuery();
			while(rs.next()){
				Ordine o = parseOrdine(rs);
				if(!getOrdineDettagli(con, o)){
					System.err.println("getOrdineByTavolo("+tavolo+") -> getOrdineDettagli()");
				}
				ordini.add(o);
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
		return ordini;
	}
	public boolean chiudiOrdiniDiOggi(){
		//TODO
		//fare con query o con getOrdini24OreAperti???
		return false;
	}
}
