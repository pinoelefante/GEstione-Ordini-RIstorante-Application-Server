package it.geori.as.communication;

import java.io.IOException;
import java.util.Map;

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
		COMMAND_VERIFICA_LOGIN="isLogged";
	
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
				String username = req.getParameter("username");
				String pass = req.getParameter("password");
				if(username!=null && pass!=null){
					Map<String,String> login = AuthenticatedUsers.getInstance().login(username, pass);
					if(login!=null){
						Cookie cookie_user = new Cookie("user", username);
						Cookie cookie_sess = new Cookie("sessionid", login.get("sessionid"));
						Cookie cookie_nome = new Cookie("nome", login.get("nome"));
						resp.addCookie(cookie_user);
						resp.addCookie(cookie_sess);
						resp.addCookie(cookie_nome);
						xml = XMLDocumentCreator.operationStatus(true);
					}
					else {
						xml = XMLDocumentCreator.operationStatus(false);
					}
				}
				else {
					xml = XMLDocumentCreator.errorParameters();
				}
				break;
			case COMMAND_LOGOUT:
				boolean logout = doLogout(req.getCookies());
				xml = XMLDocumentCreator.operationStatus(logout);
				break;
			case COMMAND_VERIFICA_LOGIN:
				boolean r = verificaCookieLogin(req.getCookies());
				xml = XMLDocumentCreator.operationStatus(r);
				break;
			case COMMAND_LOGIN_GUEST:
				String orderCode = req.getParameter("orderCode");
				if(orderCode != null){
					boolean login = AuthenticatedUsers.getInstance().loginGuest(orderCode);
					if(login){
						Cookie c = new Cookie("orderCode", orderCode);
						resp.addCookie(c);	
					}
					xml = XMLDocumentCreator.operationStatus(login);
				}
				else {
					xml = XMLDocumentCreator.errorParameters();
				}
				break;
			case COMMAND_LOGOUT_GUEST:
				String orderCode1 = req.getParameter("orderCode");
				break;
		}
		
		XMLDocumentCreator.sendResponse(resp, xml);
	}
	private boolean verificaCookieLogin(Cookie[] listCookie){
		String user=null, session = null;
		for(int i=0;i<listCookie.length;i++){
			Cookie cookie = listCookie[i];
			switch(cookie.getName()){
				case "user":
					user = cookie.getValue();
					break;
				case "sessionid":
					session = cookie.getValue();
					break;
			}
		}
		if(user!=null && session!=null){
			return AuthenticatedUsers.getInstance().isAuthenticated(user, session);
		}
		return false;
	}
	private boolean doLogout(Cookie[] listCookie){
		String user=null, session = null;
		for(int i=0;i<listCookie.length;i++){
			Cookie cookie = listCookie[i];
			switch(cookie.getName()){
				case "user":
					user = cookie.getValue();
					break;
				case "sessionid":
					session = cookie.getValue();
					break;
			}
		}
		if(user!=null && session!=null){
			return AuthenticatedUsers.getInstance().logout(user, session);
		}
		return false;
	}
}
