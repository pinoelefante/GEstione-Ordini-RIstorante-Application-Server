package it.geori.as.communication;

import javax.servlet.http.Cookie;

public class CookieManager {
	final static String 
		COOKIE_USERNAME="user",
		COOKIE_NOME="nome",
		COOKIE_ORDERCODE="orderCode",
		COOKIE_SESSION_ID="sessionid";
	
	static String getValueFromCookie(Cookie[] listCookie, String key){
		if(listCookie==null)
			return null;
		for(int i=0;i<listCookie.length;i++){
			Cookie cookie = listCookie[i];
			if(cookie.getName().compareTo(key)==0){
				return cookie.getValue();
			}
		}
		return null;
	}
}
