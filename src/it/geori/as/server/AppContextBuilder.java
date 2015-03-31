package it.geori.as.server;

import it.geori.as.communication.ServletAuthentication;
import it.geori.as.communication.ServletIngredienti;

import org.eclipse.jetty.webapp.WebAppContext;

public class AppContextBuilder {
	private WebAppContext webAppContext;

	public WebAppContext buildWebAppContext() {
		webAppContext = new WebAppContext();
		webAppContext.setDescriptor(webAppContext + "/WEB-INF/web.xml");
		webAppContext.setResourceBase("./page");
		webAppContext.setContextPath("/");
		//webAppContext.setInitParameter("useFileMappedBuffer", "false"); //per non bloccare i file durante lo sviluppo
		webAppContext.setInitParameter("cacheControl","max-age=0,public");
		webAppContext.addServlet(ServletAuthentication.class, "/ServletAuthentication");
		webAppContext.addServlet(ServletIngredienti.class, "/ServletIngredienti");
		
		return webAppContext;
	}
}
