package it.geori.as.controllers;

import it.geori.as.data.Ingrediente;
import it.geori.as.data.Prodotto;
import it.geori.as.data.ProdottoCategoria;
import it.geori.as.data.interfaces.Identifier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DBProdotti extends CacheManager {
	private static DBProdotti instance;
	
	public static DBProdotti getInstance(){
		if(instance==null)
			instance = new DBProdotti();
		return instance;
	}
	private DBProdotti(){
		super();
		loadListProdottoCategoria();
	}
	private final static String TABLE_NAME_PRODOTTI = "prodotto",
			COLUMN_PRODOTTO_ID="id_prodotto",
			COLUMN_PRODOTTO_CATEGORIA="categoria",
			COLUMN_PRODOTTO_NOME="nome_prodotto",
			COLUMN_PRODOTTO_PREZZO="prezzo_prodotto",
			COLUMN_PRODOTTO_DESCRIZIONE="descrizione";
	
	private final static String TABLE_NAME_PRODOTTI_DETT = "prodotto_dettagli",
			COLUMN_PRODOTTO_DETT_PRODOTTO="id_prodotto",
			COLUMN_PRODOTTO_DETT_INGREDIENTE="id_ingrediente";
	
	private final static String TABLE_NAME_CATEGORIE = "prodotto_categorie",
			COLUMN_CATEGORIE_ID="id_categoria",
			COLUMN_CATEGORIE_NOME="nome_categoria";
	
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
					int idProdotto = rs.getInt(1);
					p.setID(idProdotto);
					con.commit();
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
						updateItemToCache(p);
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
		ArrayList<Ingrediente> list = new ArrayList<Ingrediente>();
		ArrayList<Ingrediente> p1List = p1.getIngredienti();
		ArrayList<Ingrediente> p2List = p2.getIngredienti();
		for(Ingrediente i1 : p1List){
			boolean found = false;
			for(int i=0;i<p2List.size();i++){
				Ingrediente i2 = p1List.get(i);
				if(i2.getID()==i1.getID()){
					found = true;
					break;
				}
			}
			if(!found){
				list.add(i1);
			}
		}
		return list;
	}
	private ArrayList<Ingrediente> getIngredientiToAdd(Prodotto p1, Prodotto p2){
		ArrayList<Ingrediente> list = new ArrayList<Ingrediente>();
		ArrayList<Ingrediente> p1List = p1.getIngredienti();
		ArrayList<Ingrediente> p2List = p1.getIngredienti();
		
		for(Ingrediente i2 : p2List){
			boolean found = false;
			for(int i=0;i<p1List.size();i++){
				Ingrediente i1 = p1List.get(i);
				if(i2.getID()==i1.getID()){
					found = true;
					break;
				}
			}
			if(!found){
				list.add(i2);
			}
		}
		return list;
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
					String q2 = "SELECT dettagli."+DBIngredienti.COLUMN_INGREDIENTI_ID+" FROM "+TABLE_NAME_PRODOTTI+" AS prod JOIN "+TABLE_NAME_PRODOTTI_DETT+" AS dettagli WHERE prod."+COLUMN_PRODOTTO_ID+"=dettagli."+COLUMN_PRODOTTO_DETT_PRODOTTO+" AND prod."+COLUMN_PRODOTTO_ID+"="+p.getID();
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
				System.out.println(q);
				st = con.prepareStatement(q);
				if(st.executeUpdate()<=0){
					insIngr = false;
					break;
				}
			}
			if(!insIngr){
				con.rollback(sp);
			}
			else {
				con.commit();
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
	private Map<Integer, ProdottoCategoria> cacheCategorie = new HashMap<>();
	private void loadListProdottoCategoria(){
		String query = "SELECT * FROM "+TABLE_NAME_CATEGORIE + " ORDER BY "+COLUMN_CATEGORIE_NOME;
		Connection con;
		try {
			con = DBConnectionPool.getConnection();
		}
		catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		try(PreparedStatement st = con.prepareStatement(query); ResultSet rs = st.executeQuery())
		{
			while(rs.next()){
				int id = rs.getInt(COLUMN_CATEGORIE_ID);
				String nome = rs.getString(COLUMN_CATEGORIE_NOME);
				ProdottoCategoria cat = new ProdottoCategoria(id, nome);
				cacheCategorie.put(id, cat);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally
		{
			DBConnectionPool.releaseConnection(con);
		}
	}
	public ArrayList<ProdottoCategoria> getListProdottoCategoria(){
		ArrayList<ProdottoCategoria> list = new ArrayList<ProdottoCategoria>(cacheCategorie.size());
		for(ProdottoCategoria cat : cacheCategorie.values()){
			int index = -1;
			for(int i = 0;i<list.size();i++){
				if(cat.getNomeCategoria().compareTo(list.get(i).getNomeCategoria())<0){
					index = i;
					break;
				}
			}
			if(index<0)
				list.add(cat);
			else
				list.add(index, cat);
		}
		return list;
	}
	public ProdottoCategoria getProdottoCategoria(int id){
		if(cacheCategorie.containsKey(id))
			return cacheCategorie.get(id);
		else {
			String query = "SELECT * FROM "+TABLE_NAME_CATEGORIE+" WHERE "+COLUMN_CATEGORIE_ID+"="+id;
			Connection con;
			try {
				con = DBConnectionPool.getConnection();
			} 
			catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
			ProdottoCategoria pc = null;
			try {
				PreparedStatement st = con.prepareStatement(query);
				ResultSet rs = st.executeQuery();
				if(rs.next()){
					Integer idCat = rs.getInt(COLUMN_CATEGORIE_ID);
					String nome = rs.getString(COLUMN_CATEGORIE_NOME);
					pc = new ProdottoCategoria(idCat, nome);
					cacheCategorie.put(id, pc);
				}
				rs.close();
				st.close();
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
			DBConnectionPool.releaseConnection(con);
			return pc;
		}
	}
	public boolean addProdottoCategoria(ProdottoCategoria cat){
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
		String query = "INSERT INTO "+TABLE_NAME_CATEGORIE+" ("+COLUMN_CATEGORIE_NOME+") VALUES (\""+cat.getNomeCategoria()+"\")";
		try {
			PreparedStatement st = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			if(st.executeUpdate()>0){
				ResultSet rs = st.getGeneratedKeys();
				if(rs.next()){
					int id = rs.getInt(1);
					cat.setID(id);
					res = true;
					con.commit();
					cacheCategorie.put(id, cat);
				}
				rs.close();
				st.close();
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
		DBConnectionPool.releaseConnection(con);
		return res;
	}
	public boolean removeProdottoCategoria(int id){
		String query = "DELETE FROM "+TABLE_NAME_CATEGORIE+" WHERE "+COLUMN_CATEGORIE_ID+"="+id;
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
				res = true;
				cacheCategorie.remove(id);
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
	public boolean updateProdottoCategoria(ProdottoCategoria p){
		String query = "UPDATE "+TABLE_NAME_CATEGORIE+" SET "+COLUMN_CATEGORIE_NOME+"=\""+p.getNomeCategoria()+"\" WHERE "+COLUMN_CATEGORIE_ID+"="+p.getID();
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
				ProdottoCategoria cat_cache = getProdottoCategoria(p.getID());
				if(cat_cache!=null){
					cat_cache.setNomeCategoria(p.getNomeCategoria());
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
	public ArrayList<Prodotto> getCacheList(){
		ArrayList<Prodotto> list = new ArrayList<Prodotto>();
		for(Entry<Integer, Identifier> p : getCache().entrySet()){
			list.add((Prodotto)p.getValue());
		}
		return list;
	}
	
}
