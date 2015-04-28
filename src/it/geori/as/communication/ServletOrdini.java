package it.geori.as.communication;

import it.geori.as.controllers.DBOrdini;
import it.geori.as.data.Ordine;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class ServletOrdini extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private final static Document XMLEmpty = new Document();
	private final static String
		COMMAND_ADD_ORDER="order_add",
		COMMAND_DEL_ORDER="order_del",
		COMMAND_MOD_ORDER="order_modify",
		COMMAND_LIST_ORDER="order_list",
		COMMAND_ADD_TO_ORDER="order_add_to",
		COMMAND_REMOVE_FROM_ORDER="order_remove_from",
		COMMAND_CALC_ORDER="order_calc",
		COMMAND_SCONTO_ORDER="order_add_sconto",
		COMMAND_GUEST_ORDER="order_guest";
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		Document docResponse = null;
		//String xml = req.getParameter("xml");
		if(action!=null){
			switch(action){
				case COMMAND_ADD_ORDER:
					if(!AuthenticatedUsers.getInstance().isAuthenticated(req.getCookies())){
						docResponse = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_LOGGATO);
						break;
					}
					String add_order_tavolo=req.getParameter("tavolo");
					String add_order_num_coperti=req.getParameter("coperti");
					if(add_order_num_coperti!=null && add_order_tavolo!=null){
						int tavolo = Integer.parseInt(add_order_tavolo);
						int coperti = Integer.parseInt(add_order_num_coperti);
						Integer idServedBy = AuthenticatedUsers.getInstance().getIDUser(req.getCookies());
						Ordine ordine = new Ordine(0, tavolo, coperti, idServedBy, "", "");
						boolean res = DBOrdini.getInstance().addNewOrder(ordine);
						//TODO
					}
					break;
				case COMMAND_DEL_ORDER:
					if(!AuthenticatedUsers.getInstance().isAuthenticated(req.getCookies())){
						docResponse = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_LOGGATO);
						break;
					}
					break;
				case COMMAND_LIST_ORDER:
					if(!AuthenticatedUsers.getInstance().isAuthenticated(req.getCookies())){
						docResponse = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_LOGGATO);
						break;
					}
					String idOrdine = req.getParameter("id");
					if(idOrdine!=null){
						try {
							Integer idOrd = Integer.parseInt(idOrdine);
							Ordine ordine = DBOrdini.getInstance().getOrdine(idOrd);
							if(ordine!=null) {
								docResponse = XMLDocumentCreator.listOrdine(ordine);
							}
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					break;
				case COMMAND_MOD_ORDER:
					if(!AuthenticatedUsers.getInstance().isAuthenticated(req.getCookies())){
						docResponse = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_LOGGATO);
						break;
					}
					//TODO notify
					break;
				case COMMAND_ADD_TO_ORDER:
					if(!AuthenticatedUsers.getInstance().isAuthenticated(req.getCookies())){
						docResponse = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_LOGGATO);
						break;
					}
					//TODO notify
					break;
				case COMMAND_REMOVE_FROM_ORDER:
					if(!AuthenticatedUsers.getInstance().isAuthenticated(req.getCookies())){
						docResponse = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_LOGGATO);
						break;
					}
					//TODO notify
					break;
				case COMMAND_CALC_ORDER:
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						docResponse = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_LOGGATO);
						break;
					}
					break;
				case COMMAND_SCONTO_ORDER:
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						docResponse = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_LOGGATO);
						break;
					}
					break;
				case COMMAND_GUEST_ORDER:
					
					break;
			}
		}
		XMLDocumentCreator.sendResponse(resp, docResponse);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	private Document readXML(String base64XML, boolean customBase) {
		String xml = deBASE64(customBase?unescapeBase64(base64XML):base64XML);
		SAXBuilder saxBuilder=new SAXBuilder();
		Reader stringReader=new StringReader(xml);
		Document doc;
		try {
			doc = saxBuilder.build(stringReader);
			return doc;
		}
		catch (JDOMException | IOException e) {
			e.printStackTrace();
			return XMLEmpty;
		}
	}
	private String deBASE64(String base64){
		byte[] decoded = Base64.getDecoder().decode(base64);
		return new String(decoded);
	}
	private String unescapeBase64(String escaped){
		return escaped.replace("_", "+").replace("@", "/").replace(".", "=");
	}
}
