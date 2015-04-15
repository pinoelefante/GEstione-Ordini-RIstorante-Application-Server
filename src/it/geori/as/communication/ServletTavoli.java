package it.geori.as.communication;

import it.geori.as.controllers.DBTavoli;
import it.geori.as.data.Tavolo;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;

public class ServletTavoli extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final static String
		COMMAND_ADD_TABLE="tavoli_add",
		COMMAND_REMOVE_TABLE="tavoli_del",
		COMMAND_UPDATE_TABLE="tavoli_update",
		COMMAND_LIST_TABLE="tavoli_list";
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException ,IOException {
		String action = req.getParameter("action");
		Document xml = null;
		switch(action){
			case COMMAND_ADD_TABLE:
				if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
					xml = XMLDocumentCreator.operationStatus(false, "Operazione consentita solo agli amministratori");
					break;
				}
				String nomeAdd = req.getParameter("nome");
				String prezzoAdd = req.getParameter("prezzo");
				if(nomeAdd!=null && nomeAdd.length()>0 && prezzoAdd!=null){
					try {
						double prezzo = Double.parseDouble(prezzoAdd);
						Tavolo t = new Tavolo(0, prezzo, nomeAdd);
						if(DBTavoli.getInstance().addTable(t)){
							xml = XMLDocumentCreator.operationStatus(true, "");
						}
						else
							xml = XMLDocumentCreator.operationStatus(false, "Il tavolo "+nomeAdd+" potrebbe essere già presente");
					}
					catch(Exception e){}
				}
				break;
			case COMMAND_REMOVE_TABLE:
				if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
					xml = XMLDocumentCreator.operationStatus(false, "Operazione consentita solo agli amministratori");
					break;
				}
				String idRemove = req.getParameter("id");
				if(idRemove!=null){
					try {
						int id = Integer.parseInt(idRemove);
						boolean r = DBTavoli.getInstance().removeTable(id);
						if(r)
							xml = XMLDocumentCreator.operationStatus(true, "");
						else
							xml = XMLDocumentCreator.operationStatus(false, "");
					}
					catch(Exception e){}
				}
				break;
			case COMMAND_UPDATE_TABLE:
				if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
					xml = XMLDocumentCreator.operationStatus(false, "Operazione consentita solo agli amministratori");
					break;
				}
				String nomeUpd = req.getParameter("nome");
				String idUpd = req.getParameter("id");
				String prezzoUpd = req.getParameter("prezzo");
				if(nomeUpd!=null && idUpd!=null && prezzoUpd!=null){
					try {
						int id = Integer.parseInt(idUpd);
						double prezzo = Double.parseDouble(prezzoUpd);
						Tavolo t = new Tavolo(id, prezzo, nomeUpd);
						if(DBTavoli.getInstance().updateTable(t)){
							xml = XMLDocumentCreator.operationStatus(true, "");
						}
						else
							xml = XMLDocumentCreator.operationStatus(false, "Tavolo inesistente o nome già in uso");
					}
					catch(Exception e){}
				}
				break;
			case COMMAND_LIST_TABLE:
				ArrayList<Tavolo> l = DBTavoli.getInstance().getList();
				xml = XMLDocumentCreator.listTavoli(l);
				break;
			default:
				xml = XMLDocumentCreator.errorParameters();
				break;
		}
		XMLDocumentCreator.sendResponse(resp, xml);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
}
