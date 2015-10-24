package it.geori.as.communication;

import it.geori.as.controllers.DBMenu;
import it.geori.as.data.Menu;
import it.geori.as.data.Prodotto;
import it.geori.as.data.ProdottoCategoria;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom2.Document;

public class ServletMenu extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String 
		COMMAND_ADD_MENU="menu_add",
		COMMAND_LIST_MENU="menu_list",
		COMMAND_DELETE_MENU="menu_del",
		COMMAND_UPDATE_MENU="menu_update",
		COMMAND_CREATE_MENU_FROM="menu_copy",
		COMMAND_ADD_ITEM_TO_MENU="menu_add_item",
		COMMAND_SET_ITEMS_TO_MENU="menu_set_items",
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
						xml = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_SOLO_ADMIN);
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
						xml = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_SOLO_ADMIN);
						break;
					}
					String create_from_id = req.getParameter("id");
					String create_from_nome = req.getParameter("nome");
					if(create_from_id!=null && create_from_nome!=null && create_from_nome.length()>0){
						try {
							int id = Integer.parseInt(create_from_id);
							boolean res = DBMenu.getInstance().cloneMenuFrom(id, create_from_nome); 
							xml = XMLDocumentCreator.operationStatus(res, res?"":"");
						}
						catch(Exception e){
							xml = XMLDocumentCreator.errorParameters();
						}
					}
					break;
				case COMMAND_DELETE_MENU:
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						xml = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_SOLO_ADMIN);
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
						xml = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_SOLO_ADMIN);
						break;
					}
					String update_id = req.getParameter("id");
					String update_nome = req.getParameter("nome");
					String update_data = req.getParameter("data");
					if(update_id!=null && update_nome!=null && update_nome.length()>0 && update_data!=null){
						try {
							int id = Integer.parseInt(update_id);
							Menu m = new Menu(id, update_nome, update_data);
							boolean res = DBMenu.getInstance().updateMenu(m);
							xml = XMLDocumentCreator.operationStatus(res, res?"":"");
						}
						catch(Exception e){
							xml = XMLDocumentCreator.errorParameters();
						}
					}
					break;
				case COMMAND_ADD_ITEM_TO_MENU:
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						xml = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_SOLO_ADMIN);
						break;
					}
					String add_item_menu = req.getParameter("menu");
					String add_item_item = req.getParameter("prodotto");
					if(add_item_item!=null && add_item_menu!=null){
						try {
							int menu = Integer.parseInt(add_item_menu);
							int prod = Integer.parseInt(add_item_item);
							boolean res = DBMenu.getInstance().addItemToMenu(menu, prod);
							xml = XMLDocumentCreator.operationStatus(res, res?"":"");
						}
						catch(Exception e){}
					}
					break;
				case COMMAND_SET_ITEMS_TO_MENU:
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						xml = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_SOLO_ADMIN);
						break;
					}
					String set_id_menu = req.getParameter("menu");
					String set_prodotti = req.getParameter("prodotti");
					if(set_id_menu!=null && set_prodotti!=null)
					{
						int menu = Integer.parseInt(set_id_menu);
						String[] ids_prodotti = set_prodotti.split(";");
						ArrayList<Integer> id_prodotti = new ArrayList<Integer>(ids_prodotti.length);
						for(int i=0;i<ids_prodotti.length;i++){
							int id = Integer.parseInt(ids_prodotti[i]);
							id_prodotti.add(id);
						}
						boolean res = DBMenu.getInstance().setProdottiMenu(menu, id_prodotti);
						xml = XMLDocumentCreator.operationStatus(res, "");
					}
					break;
				case COMMAND_REMOVE_ITEM_FROM_MENU:
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						xml = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_SOLO_ADMIN);
						break;
					}
					String remove_item_menu = req.getParameter("menu");
					String remove_item_item = req.getParameter("prodotto");
					if(remove_item_item!=null && remove_item_menu!=null){
						try {
							int menu = Integer.parseInt(remove_item_menu);
							int prod = Integer.parseInt(remove_item_item);
							boolean res = DBMenu.getInstance().removeItemFromMenu(menu, prod);
							xml = XMLDocumentCreator.operationStatus(res, res?"":"");
						}
						catch(Exception e){}
					}
					break;
				case COMMAND_GET_LIST_PRODOTTI_MENU:
					String list_menu_dett = req.getParameter("id");
					if(list_menu_dett!=null){
						int id = Integer.parseInt(list_menu_dett);
						Map<ProdottoCategoria, ArrayList<Prodotto>> dettagli = DBMenu.getInstance().getListProdottiMenu(id,false);
						xml = XMLDocumentCreator.listMenuDetails(dettagli);
					}
					break;
				default:
					xml = XMLDocumentCreator.errorParameters();
			}
		}
		XMLDocumentCreator.sendResponse(resp, xml);
	}
}
