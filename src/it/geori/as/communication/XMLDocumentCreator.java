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
			Element ingr = elementIngrediente(i);
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
			Element tavolo = elementTavolo(t);
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
			Element menu = elementMenu(m);
			main_menu.addContent(menu);
		}
		root.addContent(main_menu);
		Document doc = new Document(root);
		return doc;
	}
	public static Document listMenuDetails(Map<ProdottoCategoria, ArrayList<Prodotto>> dett){
		Element root = getBooleanElement(true, "");
		
		for(Entry<ProdottoCategoria, ArrayList<Prodotto>> cat : dett.entrySet()){
			ProdottoCategoria categoria = cat.getKey();
			ArrayList<Prodotto> prodotti = cat.getValue();
			Element categoriaNode = elementProdottoCategoria(categoria);
			root.addContent(categoriaNode);
			Element categoriaProdotti = new Element("prodotti");
			for(Prodotto p : prodotti){
				Element prodottoNode = elementProdotto(p);
				categoriaProdotti.addContent(prodottoNode);
			}
			categoriaNode.addContent(categoriaProdotti);
		}
		
		Document doc = new Document(root);
		return doc;
	}
	private static Element elementProdotto(Prodotto p){
		Element prod = new Element("prodotto");
		Element id = new Element("id");
		id.addContent(p.getID()+"");
		Element nome = new Element("nome");
		nome.addContent(p.getNomeProdotto());
		Element descr = new Element("descrizione");
		descr.addContent(p.getDescrizione());
		Element prezzo = new Element("prezzo");
		prezzo.addContent(p.getPrezzo()+"");
		Element ingredienti = new Element("ingredienti");
		for(Ingrediente i : p.getIngredienti()){
			Element ingr = elementIngrediente(i);
			ingredienti.addContent(ingr);
		}
		prod.addContent(id);
		prod.addContent(nome);
		prod.addContent(prezzo);
		prod.addContent(descr);
		prod.addContent(ingredienti);
		return prod;
	}
	private static Element elementIngrediente(Ingrediente i){
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
		return ingr;
	}
	private static Element elementTavolo(Tavolo t){
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
		return tavolo;
	}
	private static Element elementMenu(Menu m){
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
		return menu;
	}
	private static Element elementProdottoCategoria(ProdottoCategoria categoria){
		Element categoriaNode = new Element("categoria");
		categoriaNode.setAttribute("nome",categoria.getNomeCategoria());
		categoriaNode.setAttribute("id", categoria.getID()+"");
		return categoriaNode;
	}
}
