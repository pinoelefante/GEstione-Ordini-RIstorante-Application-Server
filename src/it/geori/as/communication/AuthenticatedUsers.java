package it.geori.as.communication;

import it.geori.as.controllers.DBOrdini;
import it.geori.as.controllers.DBUtenti;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;

public class AuthenticatedUsers {
	private final static int USER_ADMIN = 2, USER_NORMAL = 1, USER_GUEST = 0;
	private SecureRandom random = new SecureRandom();
	
	private static AuthenticatedUsers instance;
	private Map<String, User> authUsers;
	
	protected static AuthenticatedUsers getInstance(){
		if(instance==null)
			instance = new AuthenticatedUsers();
		return instance;
	}
	private AuthenticatedUsers(){
		authUsers = new HashMap<String, User>();
	}
	public boolean isAuthenticated(String user, String authcode){
		User u = authUsers.get(user);
		if(u==null || u.getAuthcode().compareTo(authcode)!=0)
			return false;
		else
			return true;
	}
	public Map<String,String> login(String username, String pass){
		Map<String, String> login = DBUtenti.getInstance().login(username, pass); 
		if(login!=null){
			String sessionid = new BigInteger(130, random).toString(32);
			String nome = login.get("nome");
			int idU = Integer.parseInt(login.get("id"));
			Integer livello = Integer.parseInt(login.get("livello"));
			login.put("sessionid", sessionid);
			
			User u = new User(username, sessionid, nome, livello, idU);
			authUsers.put(username, u);
			
			return login;
		}
		return null;
	}
	public boolean logout(String username, String authcode){
		User u = authUsers.get(username);
		if(u==null || u.getAuthcode().compareTo(authcode)!=0)
			return false;
		else {
			authUsers.remove(username);
			return true;
		}
	}
	protected boolean kickUser(String username){
		return authUsers.remove(username)!=null;
	}
	public boolean loginGuest(String orderCode){
		if(authUsers.get("guest_"+orderCode)!=null){
			return true;
		}
		else {
			if(DBOrdini.getInstance().isOrderCodeExists(orderCode)){
				User u = new User(orderCode);
				authUsers.put("guest_"+orderCode, u);
				return true;
			}
			else
				return false;
		}
	}
	public boolean logoutGuest(String orderCode){
		authUsers.remove("guest_"+orderCode);
		return true;
	}
	public boolean isAdmin(String username){
		User u = authUsers.get(username);
		if(u!=null && u.getLivelloAutorizzazione()==USER_ADMIN){
			return true;
		}
		return false;
	}
	public ArrayList<Entry<String,String>> getAuthenticatedUsersList(){
		ArrayList<Entry<String, String>> list = new ArrayList<>();
		for(Entry<String,User> u:authUsers.entrySet()){
			Entry<String,String> entry=new AbstractMap.SimpleEntry<String,String>(u.getValue().getUsername(),
					u.getValue().getNome().compareToIgnoreCase("guest")==0?"Guest":u.getValue().getAuthcode());
			list.add(entry);
		}
		return list;
	}
	public boolean changePassword(String username, String oldPass, String newPass){
		if(DBUtenti.getInstance().login(username, oldPass)!=null){
			return DBUtenti.getInstance().modificaPasswordUtente(username, newPass);
		}
		else {
			return false;
		}
	}
	public boolean isAdmin(Cookie[] listCookie){
		if(listCookie == null)
			return false;
		
		String user=CookieManager.getValueFromCookie(listCookie, CookieManager.COOKIE_USERNAME);
		String session=CookieManager.getValueFromCookie(listCookie, CookieManager.COOKIE_SESSION_ID);
		
		if(user!=null && session!=null){
			if(isAuthenticated(user, session) && isAdmin(user))
				return true;
		}
		return false;
	}
	public boolean isAuthenticated(Cookie[] listCookie){
		if(listCookie == null)
			return false;
		
		String user=CookieManager.getValueFromCookie(listCookie, CookieManager.COOKIE_USERNAME);
		String session=CookieManager.getValueFromCookie(listCookie, CookieManager.COOKIE_SESSION_ID);
		
		return isAuthenticated(user, session);
	}
}
class User {
	private String username, authcode, appearance, orderCode;
	private int livelloAutorizzazione, id;
	public User(String user, String code, String app, int lv, int i){
		username = user;
		authcode = code;
		appearance = app;
		livelloAutorizzazione = lv;
		id = i;
	}
	public User(String orderCode){
		this("guest_"+orderCode, "", "guest",0,0);
	}
	public String getUsername() {
		return username;
	}
	public String getAuthcode() {
		return authcode;
	}
	public String getNome(){
		return appearance;
	}
	public int getLivelloAutorizzazione(){
		return livelloAutorizzazione;
	}
	public String getOrderCode(){
		return orderCode;
	}
	public int getID(){
		return id;
	}
}
