package it.geori.as.controllers;

import it.geori.as.data.Menu;
import it.geori.as.data.Prodotto;
import it.geori.as.data.ProdottoCategoria;
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
	
	private final static String TABLE_NAME_DETTAGLI="menu_dettagli", 
			COLUMN_PRODOTTO_DETTAGLI="id_prodotto", 
			COLUMN_MENU_DETTAGLI="id_menu";
	
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
	public boolean cloneMenuFrom(int oldMenu, String nome){
		Menu newMenu = new Menu(0, nome, "");
		if(addMenu(newMenu)){
			Connection con = null;
			Savepoint sp = null;
			try {
				con = DBConnectionPool.getConnection();
				sp = con.setSavepoint();
				ArrayList<Integer> prodottiOld = getListProdottiFromDB(oldMenu);
				for(int i=0;i<prodottiOld.size();i++){
					if(!addItemToMenu(con, newMenu.getID(), prodottiOld.get(i))){
						con.rollback(sp);
						removeMenu(newMenu.getID());
						return false;
					}
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
				removeMenu(newMenu.getID());
			}
			DBConnectionPool.releaseConnection(con);
		}
		return false;
	}
	protected boolean addItemToMenu(Connection con, int menu, int prod){
		Prodotto p = DBProdotti.getInstance().getProdottoByID(prod);
		Menu m = (Menu) getItem(menu);
		if(p!=null){
			String query = "INSERT INTO "+TABLE_NAME_DETTAGLI+" ("+COLUMN_MENU_DETTAGLI+","+COLUMN_PRODOTTO_DETTAGLI+") VALUES ("+menu+","+prod+")";
			try {
				PreparedStatement st = con.prepareStatement(query);
				boolean res = st.executeUpdate()>0;
				if(res)
					m.addItemToMenu(p);
				st.close();
				return res;
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	public boolean addItemToMenu(int menu, int prod){
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
		
		boolean res = addItemToMenu(menu, prod);
		if(res){
			try {
				con.commit();
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				con.rollback(sp);
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		DBConnectionPool.releaseConnection(con);
		return res;
	}
	public boolean removeItemFromMenu(int menu, int prod){
		String query = "DELETE FROM "+TABLE_NAME_DETTAGLI+" WHERE "+COLUMN_MENU_DETTAGLI+"="+menu+" AND "+COLUMN_PRODOTTO_DETTAGLI+"="+prod;
		Connection con;
		Savepoint sp;
		Menu m = getMenuByID(menu);
		if(m==null)
			return false;
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
				m.removeItemFromMenu(prod);
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
	private ArrayList<Integer> getListProdottiFromDB(int menu){
		ArrayList<Integer> idProdotti = new ArrayList<Integer>();
		String query = "SELECT * FROM "+TABLE_NAME_DETTAGLI+" WHERE "+COLUMN_MENU_DETTAGLI+"="+menu;
		Connection con = null;
		try {
			con = DBConnectionPool.getConnection();
			PreparedStatement st = con.prepareStatement(query);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				int idProd = rs.getInt(COLUMN_PRODOTTO_DETTAGLI);
				idProdotti.add(idProd);
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		DBConnectionPool.releaseConnection(con);
		return idProdotti;
	}
	public Map<ProdottoCategoria, ArrayList<Prodotto>> getListProdottiMenu(int menu, boolean forceQuery){
		Map<ProdottoCategoria, ArrayList<Prodotto>> list = new HashMap<ProdottoCategoria, ArrayList<Prodotto>>();
		Menu m = getMenuByID(menu);
		if(m.getListProdotti().size()==0 || forceQuery){
			//String query = "SELECT prod.id_prodotto, prod.nome_prodotto, prod.prezzo_prodotto, prod.descrizione, categorie.id_categoria, categorie.nome_categoria FROM prodotti AS prod JOIN prodotti_categorie AS categorie JOIN menu AS menu JOIN menu_dettagli AS m_det WHERE menu.versione_menu=1 AND m_det.id_prodotto=prod.id_prodotto AND categorie.id_categoria=prod.categoria ORDER BY categorie.nome_categoria ASC, prod.nome_prodotto ASC";
			String query = "SELECT "+COLUMN_PRODOTTO_DETTAGLI+" FROM "+TABLE_NAME_DETTAGLI+" WHERE "+COLUMN_MENU_DETTAGLI+"="+menu;
			Connection con = null;
			try {
				con = DBConnectionPool.getConnection();
				PreparedStatement st = con.prepareStatement(query);
				ResultSet rs = st.executeQuery();
				m.getListProdotti().clear();
				while(rs.next()){
					int idP = rs.getInt(COLUMN_PRODOTTO_DETTAGLI);
					Prodotto p = DBProdotti.getInstance().getProdottoByID(idP);
					if(p!=null){
						ProdottoCategoria categoria = DBProdotti.getInstance().getProdottoCategoria(p.getIdCategoria());
						if(!list.containsKey(categoria)){
							ArrayList<Prodotto> l_prod = new ArrayList<Prodotto>();
							list.put(categoria, l_prod);
						}
						ArrayList<Prodotto> l_prod = list.get(categoria);
						addProdottoToCacheList(l_prod, p);
					}
				}
				rs.close();
				st.close();
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
			finally {
				DBConnectionPool.releaseConnection(con);
			}
		}
		else {
			Map<Integer,Prodotto> listRaw = m.getListProdotti();
			for(Entry<Integer, Prodotto> e : listRaw.entrySet()){
				ProdottoCategoria cat = DBProdotti.getInstance().getProdottoCategoria(e.getKey());
				if(!list.containsKey(cat)){
					list.put(cat, new ArrayList<Prodotto>());
				}
				ArrayList<Prodotto> prodotti = list.get(cat);
				addProdottoToCacheList(prodotti, e.getValue());
			}
		}
		return list;
	}
	private void addProdottoToCacheList(ArrayList<Prodotto> list, Prodotto p){
		boolean insertOK = false;
		for(int i=0;i<list.size() && !insertOK;i++){
			int compareResult =p.getNomeProdotto().compareToIgnoreCase(list.get(i).getNomeProdotto()); 
			if(compareResult<0){
				list.add(i, p);
				insertOK=true;
			}
			else if(compareResult==0)
				insertOK=true;
		}
		if(!insertOK)
			list.add(p);
	}
}
