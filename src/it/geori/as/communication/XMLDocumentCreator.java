package it.geori.as.communication;

import it.geori.as.data.Ingrediente;
import it.geori.as.data.Menu;
import it.geori.as.data.Prodotto;
import it.geori.as.data.ProdottoCategoria;
import it.geori.as.data.Tavolo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class XMLDocumentCreator {
	
	public static void sendResponse(HttpServletResponse response, Document doc){
		if(doc == null)
			doc = errorParameters();
		XMLOutputter xml_out = new XMLOutputter();
		xml_out.setFormat(Format.getPrettyFormat());
		response.setContentType("text/xml");
		response.setHeader("Cache-Control",	"no-store, no-cache, must-revalidate");
		PrintWriter out;
		try {
			out = response.getWriter();
			xml_out.output(doc, out);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static Element getBooleanElement(boolean b, String message){
		Element root = new Element("response");
		Element status = new Element("status");
		Element m = new Element("message");
		status.addContent(b+"");
		m.addContent(message==null?"":message);
		root.addContent(status);
		root.addContent(m);
		return root;
	}
	public static Document operationStatus(boolean s, String message){
		Element root = getBooleanElement(s, message);
		Document doc = new Document(root);
		return doc;
	}
	public static Document errorParameters(){
		return operationStatus(false, Localization.MESSAGGIO_ERRORE_PARAMETRI);
	}
	public static Document listSessions(ArrayList<Entry<String, String>> l){
		Element root = getBooleanElement(true, "");
		
		Element listUser = new Element("sessioni");
		for(Entry<String,String> e : l){
			Element sessione = new Element("sessione");
			Element user_sessione = new Element("user");
			user_sessione.addContent(e.getKey());
			Element id_sessione = new Element("sessione");
			id_sessione.addContent(e.getValue());
			sessione.addContent(user_sessione);
			sessione.addContent(id_sessione);
			listUser.addContent(sessione);
		}
		root.addContent(listUser);
		Document doc = new Document(root);
		return doc;
	}
	public static Document listIngredienti(ArrayList<Ingrediente> l){
		Element root = getBooleanElement(true, "");
		Element main_ingredienti = new Element("ingredienti");
		for(Ingrediente i : l){
			Element ingr = new Element("ingrediente");
			Element nomeIngr = new Element("nome");
			Element prezzoIngr = new Element("prezzo");
			Element idIngr = new Element("id");
			nomeIngr.addContent(i.getNome());
			prezzoIngr.addContent(i.getPrezzo()+"");
			idIngr.addContent(i.getId()+"");
			ingr.addContent(idIngr);
			ingr.addContent(nomeIngr);
			ingr.addContent(prezzoIngr);
			main_ingredienti.addContent(ingr);
		}
		root.addContent(main_ingredienti);
		Document doc = new Document(root);
		return doc;
	}
	public static Document listTavoli(ArrayList<Tavolo> l) {
		Element root = getBooleanElement(true, "");
		Element main_tavoli = new Element("tavoli");
		for(Tavolo t : l){
			Element tavolo = new Element("tavolo");
			Element idTavolo = new Element("id");
			Element nomeTavolo = new Element("nome");
			Element prezzoCoperto = new Element("coperto");
			idTavolo.addContent(t.getID()+"");
			nomeTavolo.addContent(t.getNomeTavolo());
			prezzoCoperto.addContent(t.getCostoCoperto()+"");
			tavolo.addContent(idTavolo);
			tavolo.addContent(nomeTavolo);
			tavolo.addContent(prezzoCoperto);
			main_tavoli.addContent(tavolo);
		}
		root.addContent(main_tavoli);
		Document doc = new Document(root);
		return doc;
	}
	public static Document listMenu(ArrayList<Menu> l) {
		Element root = getBooleanElement(true, "");
		Element main_menu = new Element("menus");
		for(Menu m : l){
			Element menu = new Element("menu");
			Element idMenu = new Element("id");
			Element nomeMenu = new Element("nome");
			Element dataMenu = new Element("data");
			idMenu.addContent(m.getID()+"");
			nomeMenu.addContent(m.getNomeMenu());
			dataMenu.addContent(m.getDataCreazione());
			menu.addContent(idMenu);
			menu.addContent(nomeMenu);
			menu.addContent(dataMenu);
			main_menu.addContent(menu);
		}
		root.addContent(main_menu);
		Document doc = new Document(root);
		return doc;
	}
	public static Document listMenuDetails(Map<ProdottoCategoria, ArrayList<Prodotto>> dett){
		Element root = getBooleanElement(true, "");
		
		//TODO
		
		Document doc = new Document(root);
		return doc;
	}
}
