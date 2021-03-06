package it.geori.as.communication;

import it.geori.as.controllers.DBIngredienti;
import it.geori.as.data.Ingrediente;

import java.io.IOException;
import java.util.ArrayList;

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
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						xml = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_SOLO_ADMIN);
						break;
					}
					String nomeIngr = req.getParameter("nome_ingrediente");
					String prezzoIngr = req.getParameter("prezzo");
					if(nomeIngr!=null && prezzoIngr!=null){
						double prezzo = 0;
						try {
							prezzo = Double.parseDouble(prezzoIngr);
							Ingrediente ingr = new Ingrediente(0, nomeIngr, prezzo);
							boolean res = DBIngredienti.getInstance().addIngrediente(ingr);
							xml = XMLDocumentCreator.operationStatus(res, res?"":"");
						}
						catch(Exception e){
							xml = XMLDocumentCreator.errorParameters();
						}
					}
					break;
				case COMMAND_LIST_INGREDIENTE:
					ArrayList<Ingrediente> list = DBIngredienti.getInstance().getList();
					xml = XMLDocumentCreator.listIngredienti(list);
					break;
				case COMMAND_MODIFICA_INGREDIENTE:
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						xml = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_SOLO_ADMIN);
						break;
					}
					String idMod = req.getParameter("id");
					String nomeIngrMod = req.getParameter("nome");
					String prezzoMod = req.getParameter("prezzo");
					if(nomeIngrMod!=null && nomeIngrMod.length()>0 && prezzoMod!=null){
						try {
							int id = Integer.parseInt(idMod);
							double prezzo = Double.parseDouble(prezzoMod);
							Ingrediente ingr = new Ingrediente(id, nomeIngrMod, prezzo);
							boolean res = DBIngredienti.getInstance().updateIngrediente(ingr);
							xml = XMLDocumentCreator.operationStatus(res,res?"":Localization.MESSAGGIO_ERRORE_UPDATE);
						}
						catch(NumberFormatException e){
							xml = XMLDocumentCreator.errorParameters();	
						}
					}
					break;
				case COMMAND_RIMUOVI_INGREDIENTE:
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						xml = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_SOLO_ADMIN);
						break;
					}
					String id_ingr_to_del = req.getParameter("id");
					if(id_ingr_to_del!=null){
						int id_ingr = Integer.parseInt(id_ingr_to_del);
						boolean res = DBIngredienti.getInstance().removeIngrediente(id_ingr);
						xml = XMLDocumentCreator.operationStatus(res,res?"":Localization.MESSAGGIO_ERRORE_DELETE);
						
					}
					break;
				default:
					xml = XMLDocumentCreator.errorParameters();
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
