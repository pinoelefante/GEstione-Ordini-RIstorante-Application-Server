package server;

import it.geori.as.communication.ServletAuthentication;
import it.geori.as.communication.ServletIngredienti;
import it.geori.as.communication.ServletMenu;
import it.geori.as.communication.ServletOrdini;
import it.geori.as.communication.ServletProdotti;
import it.geori.as.communication.ServletTavoli;

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
		webAppContext.addServlet(ServletTavoli.class, "/ServletTavoli");
		webAppContext.addServlet(ServletMenu.class, "/ServletMenu");
		webAppContext.addServlet(ServletOrdini.class, "/ServletOrdini");
		webAppContext.addServlet(ServletProdotti.class, "/ServletProdotti");
		
		return webAppContext;
	}
}
