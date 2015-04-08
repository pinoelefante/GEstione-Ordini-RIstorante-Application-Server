package it.geori.as.communication;

import it.geori.as.controllers.DBMenu;
import it.geori.as.data.Menu;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;

public class ServletMenu extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String 
		COMMAND_ADD_MENU="menu_add",
		COMMAND_LIST_MENU="menu_list",
		COMMAND_DELETE_MENU="menu_del",
		COMMAND_UPDATE_MENU="menu_update",
		COMMAND_CREATE_MENU_FROM="menu_copy",
		COMMAND_ADD_ITEM_TO_MENU="menu_add_item",
		COMMAND_REMOVE_ITEM_FROM_MENU="menu_remove_item",
		COMMAND_GET_LIST_PRODOTTI_MENU="menu_list_prodotti";
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		Document xml = null;
		if(action!=null){
			switch(action){
				case COMMAND_ADD_MENU:
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						xml = XMLDocumentCreator.operationStatus(false, "Operazione consentita solo agli amministratori");
						break;
					}
					String add_nome = req.getParameter("nome");
					if(add_nome!=null && !add_nome.isEmpty()){
						Menu m = new Menu(0,add_nome,"");
						boolean res = DBMenu.getInstance().addMenu(m);
						xml = XMLDocumentCreator.operationStatus(res, res?"":"");
					}
					break;
				case COMMAND_CREATE_MENU_FROM:
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						xml = XMLDocumentCreator.operationStatus(false, "Operazione consentita solo agli amministratori");
						break;
					}
					String create_from_id = req.getParameter("id");
					if(create_from_id!=null){
						try {
							int id = Integer.parseInt(create_from_id);
							Menu menu = DBMenu.getInstance().getMenuByID(id);
							if(menu!=null){
								boolean res = DBMenu.getInstance().cloneMenuFrom(menu); 
								xml = XMLDocumentCreator.operationStatus(res, res?"":"");
							}
						}
						catch(Exception e){
							xml = XMLDocumentCreator.errorParameters();
						}
					}
					break;
				case COMMAND_DELETE_MENU:
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						xml = XMLDocumentCreator.operationStatus(false, "Operazione consentita solo agli amministratori");
						break;
					}
					String del_id=req.getParameter("id");
					if(del_id!=null){
						try {
							int id = Integer.parseInt(del_id);
							boolean res = DBMenu.getInstance().removeMenu(id);
							xml = XMLDocumentCreator.operationStatus(res, res?"":"");
						}
						catch(Exception e){
							xml = XMLDocumentCreator.errorParameters();
						}
					}
					break;
				case COMMAND_LIST_MENU:
					ArrayList<Menu> list_menu = DBMenu.getInstance().getList();
					xml = XMLDocumentCreator.listMenu(list_menu);
					break;
				case COMMAND_UPDATE_MENU:
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						xml = XMLDocumentCreator.operationStatus(false, "Operazione consentita solo agli amministratori");
						break;
					}
					break;
				case COMMAND_ADD_ITEM_TO_MENU:
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						xml = XMLDocumentCreator.operationStatus(false, "Operazione consentita solo agli amministratori");
						break;
					}
					break;
				case COMMAND_REMOVE_ITEM_FROM_MENU:
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						xml = XMLDocumentCreator.operationStatus(false, "Operazione consentita solo agli amministratori");
						break;
					}
					break;
				case COMMAND_GET_LIST_PRODOTTI_MENU:
					
					break;
			}
		}
		XMLDocumentCreator.sendResponse(resp, xml);
	}
}
