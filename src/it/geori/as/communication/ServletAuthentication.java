package it.geori.as.communication;

import it.geori.as.controllers.DBUtenti;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;

public class ServletAuthentication extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final static String 
		COMMAND_LOGIN = "login", 
		COMMAND_LOGIN_GUEST="login_guest", 
		COMMAND_LOGOUT="logout",
		COMMAND_LOGOUT_GUEST="logout_guest", 
		COMMAND_VERIFICA_LOGIN="isLogged",
		COMMAND_REGISTRA="signup",
		COMMAND_CHIUDI_SESSIONE="kick_user",
		COMMAND_LIST_SESSIONI="list_sessions",
		COMMAND_CHANGE_PASSWORD="change_password";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)	throws ServletException, IOException {
		Document xml = null;
		
		String action = req.getParameter("action");
		if(action == null){
			xml = XMLDocumentCreator.errorParameters();
			XMLDocumentCreator.sendResponse(resp, xml);
			return;
		}
		
		switch(action){
			case COMMAND_LOGIN:
				if(AuthenticatedUsers.getInstance().isAuthenticated(req.getCookies())){
					xml = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_GIA_LOGGATO);
					break;
				}
				String username = req.getParameter("username");
				String pass = req.getParameter("password");
				if(username!=null && pass!=null){
					Map<String,String> login = AuthenticatedUsers.getInstance().login(username, pass);
					if(login!=null){
						Cookie cookie_user = new Cookie(CookieManager.COOKIE_USERNAME, username);
						Cookie cookie_sess = new Cookie(CookieManager.COOKIE_SESSION_ID, login.get("sessionid"));
						Cookie cookie_nome = new Cookie(CookieManager.COOKIE_NOME, login.get("nome"));
						resp.addCookie(cookie_user);
						resp.addCookie(cookie_sess);
						resp.addCookie(cookie_nome);
					}
					xml = XMLDocumentCreator.operationStatus(login!=null, "");
				}
				break;
			case COMMAND_LOGOUT:
				boolean logout = doLogout(req.getCookies());
				xml = XMLDocumentCreator.operationStatus(logout, "");
				break;
			case COMMAND_VERIFICA_LOGIN:
				boolean r = AuthenticatedUsers.getInstance().isAuthenticated(req.getCookies());
				xml = XMLDocumentCreator.operationStatus(r, "");
				break;
			case COMMAND_LOGIN_GUEST:
				if(AuthenticatedUsers.getInstance().isAuthenticated(req.getCookies())){
					xml = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_GIA_LOGGATO);
					break;
				}
				String orderCode = req.getParameter("orderCode");
				if(orderCode != null){
					boolean login = AuthenticatedUsers.getInstance().loginGuest(orderCode);
					if(login){
						Cookie c = new Cookie(CookieManager.COOKIE_ORDERCODE, orderCode);
						resp.addCookie(c);
					}
					xml = XMLDocumentCreator.operationStatus(login, "");
				}
				break;
			case COMMAND_LOGOUT_GUEST:
				xml = XMLDocumentCreator.operationStatus(doLogoutGuest(req.getCookies()), "");
				break;
			case COMMAND_REGISTRA:
				if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
					xml = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_SEI_ADMIN);
					break;
				}
				String user = req.getParameter("username");
				String pass1 = req.getParameter("password");
				String nome = req.getParameter("nome");
				String cognome = req.getParameter("cognome");
				String l = req.getParameter("livello");
				if(user!=null && pass1!=null && nome!=null && cognome!=null && l!=null){
					int lev = 0;
					try {
						lev = Integer.parseInt(l);
						boolean res = DBUtenti.getInstance().registraUtente(nome, cognome, user, pass1, lev);
						xml = XMLDocumentCreator.operationStatus(res, "");
					}
					catch(NumberFormatException e){
						xml = XMLDocumentCreator.errorParameters();
						break;
					}
				}
				break;
			case COMMAND_CHIUDI_SESSIONE:
				if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
					xml = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_SEI_ADMIN);
					break;
				}
				String toKick = req.getParameter("sessione");
				if(toKick!=null){
					boolean k = AuthenticatedUsers.getInstance().kickUser(toKick);
					xml = XMLDocumentCreator.operationStatus(k, k?"":Localization.MESSAGGIO_ERRORE_SESSIONE_NON_TROVATA);
					
				}
				break;
			case COMMAND_CHANGE_PASSWORD:
				if(AuthenticatedUsers.getInstance().isAuthenticated(req.getCookies())){
					String newPass = req.getParameter("newPass");
					String oldPass = req.getParameter("oldPass");
					String u1 = CookieManager.getValueFromCookie(req.getCookies(), CookieManager.COOKIE_USERNAME);
					if(u1!=null){
						boolean r1 = AuthenticatedUsers.getInstance().changePassword(u1, oldPass, newPass);
						xml = XMLDocumentCreator.operationStatus(r1,r1?Localization.MESSAGGIO_PASSWORD_CAMBIATA:Localization.MESSAGGIO_ERRORE_UPDATE);
					}
				}
				else {
					xml = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_LOGGATO);
				}
				break;
			case COMMAND_LIST_SESSIONI:
				if(AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
					ArrayList<Entry<String, String>> list = AuthenticatedUsers.getInstance().getAuthenticatedUsersList();
					xml = XMLDocumentCreator.listSessions(list);
				}
				else
					xml = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_SOLO_ADMIN);
				break;
			default:
				xml = XMLDocumentCreator.errorParameters();
		}
		XMLDocumentCreator.sendResponse(resp, xml);
	}
	private boolean doLogout(Cookie[] listCookie){
		if(listCookie == null)
			return false;
		
		String user=CookieManager.getValueFromCookie(listCookie, CookieManager.COOKIE_USERNAME);
		String session=CookieManager.getValueFromCookie(listCookie, CookieManager.COOKIE_SESSION_ID);
		
		if(user!=null && session!=null){
			return AuthenticatedUsers.getInstance().logout(user, session);
		}
		return false;
	}
	private boolean doLogoutGuest(Cookie[] listCookie){
		if(listCookie == null)
			return false;
		
		String orderCode = CookieManager.getValueFromCookie(listCookie, CookieManager.COOKIE_ORDERCODE);
		if(orderCode!=null){
			AuthenticatedUsers.getInstance().logoutGuest(orderCode);
		}
		return true;
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
}
