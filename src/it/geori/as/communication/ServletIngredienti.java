package it.geori.as.communication;

import it.geori.as.controllers.DBIngredienti;
import it.geori.as.data.Ingrediente;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;

public class ServletIngredienti extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final static String 
		COMMAND_INSERISCI_INGREDIENTE="ingrediente_insert",
		COMMAND_RIMUOVI_INGREDIENTE="ingrediente_remove",
		COMMAND_MODIFICA_INGREDIENTE="ingrediente_modify",
		COMMAND_LIST_INGREDIENTE="ingrediente_list";
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)	throws ServletException, IOException {
		String action = req.getParameter("action");
		Document xml = null;
		if(action != null){
			switch(action){
				case COMMAND_INSERISCI_INGREDIENTE: 
					if(!CookieManager.isAdmin(req.getCookies())){
						xml = XMLDocumentCreator.operationStatus(false, "Operazione consentita solo agli amministratori");
						break;
					}
					String nomeIngr = req.getParameter("nome_ingrediente");
					String prezzoIngr = req.getParameter("prezzo");
					if(nomeIngr!=null && prezzoIngr!=null){
						double prezzo = 0;
						try {
							prezzo = Double.parseDouble(prezzoIngr);
						}
						catch(Exception e){
							prezzo = 0;
						}
						Ingrediente ingr = new Ingrediente(0, nomeIngr, prezzo);
						boolean ins = DBIngredienti.getInstance().addIngrediente(ingr);
						if(ins){
							xml = XMLDocumentCreator.operationStatus(true, "");
						}
						else
							xml = XMLDocumentCreator.operationStatus(false, "");
					}
					else
						xml = XMLDocumentCreator.errorParameters();
					break;
				case COMMAND_LIST_INGREDIENTE:
					if(!CookieManager.isAdmin(req.getCookies())){
						xml = XMLDocumentCreator.operationStatus(false, "Operazione consentita solo agli amministratori");
						break;
					}
					break;
				case COMMAND_MODIFICA_INGREDIENTE:
					if(!CookieManager.isAdmin(req.getCookies())){
						xml = XMLDocumentCreator.operationStatus(false, "Operazione consentita solo agli amministratori");
						break;
					}
					
					break;
				case COMMAND_RIMUOVI_INGREDIENTE:
					if(!CookieManager.isAdmin(req.getCookies())){
						xml = XMLDocumentCreator.operationStatus(false, "Operazione consentita solo agli amministratori");
						break;
					}
					String id_ingr_to_del = req.getParameter("id");
					if(id_ingr_to_del!=null){
						int id_ingr = Integer.parseInt(id_ingr_to_del);
						boolean res = DBIngredienti.getInstance().removeIngrediente(id_ingr);
						if(res){
							xml = XMLDocumentCreator.operationStatus(true, "");
						}
						else {
							xml = XMLDocumentCreator.operationStatus(false, "Si è verificato un errore durante l'eliminazione");
						}
					}
					else {
						xml = XMLDocumentCreator.errorParameters();
					}
					break;
			}
		}
		else
			xml = XMLDocumentCreator.errorParameters();
		
		XMLDocumentCreator.sendResponse(resp, xml);
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
}
