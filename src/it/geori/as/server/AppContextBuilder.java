package it.geori.as.server;

import it.geori.as.communication.ServletAuthentication;
import it.geori.as.communication.ServletIngredienti;
import it.geori.as.communication.ServletTavoli;

import org.eclipse.jetty.webapp.WebAppContext;

public class AppContextBuilder {
	private WebAppContext webAppContext;

	public WebAppContext buildWebAppContext() {
		webAppContext = new WebAppContext();
		webAppContext.setDescriptor(webAppContext + "/WEB-INF/web.xml");
		webAppContext.setResourceBase("./page");
		webAppContext.setContextPath("/");
		webAppContext.setInitParameter("cacheControl","max-age=0,public");
		webAppContext.addServlet(ServletAuthentication.class, "/ServletAuthentication");
		webAppContext.addServlet(ServletIngredienti.class, "/ServletIngredienti");
		webAppContext.addServlet(ServletTavoli.class, "/ServletTavoli");
		
		return webAppContext;
	}
}
