package it.geori.as.communication;

import it.geori.as.controllers.DBOrdini;
import it.geori.as.controllers.DBUtenti;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class AuthenticatedUsers {
	private final static int USER_ADMIN = 2, USER_NORMAL = 1, USER_ERROR = 0;
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
			Integer livello = Integer.parseInt(login.get("livello"));
			login.put("sessionid", sessionid);
			
			User u = new User(username, sessionid, nome, livello);
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
}
class User {
	private String username, authcode, appearance, orderCode;
	private int livelloAutorizzazione;
	
	public User(String user, String code, String app, int lv){
		username = user;
		authcode = code;
		appearance = app;
		livelloAutorizzazione = lv;
	}
	public User(String orderCode){
		username="guest_"+orderCode;
		authcode="";
		appearance="guest";
		livelloAutorizzazione=0;
		this.orderCode = orderCode;
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
}
