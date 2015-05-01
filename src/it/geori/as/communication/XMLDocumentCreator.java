package it.geori.as.communication;

import it.geori.as.data.Dettaglio;
import it.geori.as.data.Ingrediente;
import it.geori.as.data.Menu;
import it.geori.as.data.Ordine;
import it.geori.as.data.OrdineDettagli;
import it.geori.as.data.Prodotto;
import it.geori.as.data.ProdottoCategoria;
import it.geori.as.data.Tavolo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

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
		if(categoria == null)
			System.out.println("categoria = null");
		Element categoriaNode = new Element("categoria");
		categoriaNode.setAttribute("nome",categoria.getNomeCategoria());
		categoriaNode.setAttribute("id", categoria.getID()+"");
		return categoriaNode;
	}
	public static Document listOrdine(Ordine o){
		Element root = getBooleanElement(true, "");
		Element ordine = elementOrdine(o);
		root.addContent(ordine);
		Document doc = new Document(root);
		return doc;
	}
	private static Element elementOrdine(Ordine o){
		Element ordine = new Element("ordine");
		Element id = new Element("id");
		id.addContent(""+o.getID());
		Element tavolo = new Element("tavolo");
		tavolo.addContent(""+o.getTavolo());
		Element coperti = new Element("coperti");
		coperti.addContent(""+o.getCoperti());
		Element data_creazione = new Element("data_creazione");
		data_creazione.addContent(o.getDataCreazione());
		Element data_chiusura = new Element("data_chiusura");
		data_chiusura.addContent(o.getDataChiusura());
		Element sconto = new Element("sconto");
		sconto.addContent(""+o.getSconto());
		Element totale = new Element("totale");
		totale.addContent(""+o.getCostoTotale());
		Element stato_ordine = new Element("stato_ordine");
		stato_ordine.addContent(""+o.getStatoOrdine());
		Element servito_da = new Element("servito_da");
		servito_da.addContent(""+o.getServitoDa());
		Element access_code = new Element("access_code");
		access_code.addContent(""+o.getGuestCode());
		
		ordine.addContent(id);
		ordine.addContent(tavolo);
		ordine.addContent(coperti);
		ordine.addContent(data_creazione);
		ordine.addContent(data_chiusura);
		ordine.addContent(sconto);
		ordine.addContent(totale);
		ordine.addContent(stato_ordine);
		ordine.addContent(servito_da);
		ordine.addContent(access_code);
		
		Element dettagli = new Element("dettagli");
		ordine.addContent(dettagli);
		
		ArrayList<OrdineDettagli> dett = o.getDettagliOrdine();
		for(OrdineDettagli d : dett){
			Element e = elementOrdineDettagli(d);
			dettagli.addContent(e);
		}
		
		return ordine;
	}
	private static Element elementOrdineDettagli(OrdineDettagli dett){
		Element dettaglio = new Element("dettaglio");
		
		Element quant = new Element("quantita");
		Element stato = new Element("stato");
		Element note = new Element("note");
		Element prodotto = new Element("list_prodotti");
		
		quant.addContent(dett.getQuantita()+"");
		stato.addContent(dett.getStato()+"");
		note.addContent(dett.getNote());
		
		dettaglio.addContent(quant);
		dettaglio.addContent(stato);
		dettaglio.addContent(note);
		
		ArrayList<Dettaglio> prodotti = dett.getProdotti();
		for(Dettaglio d : prodotti){
			Prodotto prod = d.getProdotto();
			ArrayList<Ingrediente> conList = d.getToAdd();
			ArrayList<Ingrediente> senzaList = d.getToRem();
			
			Element p = elementProdotto(prod);
			Element con = new Element("con");
			Element senza = new Element("senza");
			
			for(int i = 0;i<conList.size();i++){
				Ingrediente ing = conList.get(i);
				Element ingr = elementIngrediente(ing);
				con.addContent(ingr);
			}
			
			for(int i = 0;i<senzaList.size();i++){
				Ingrediente ing = senzaList.get(i);
				Element ingr = elementIngrediente(ing);
				senza.addContent(ingr);
			}
			p.addContent(con);
			p.addContent(senza);
			prodotto.addContent(p);
		}
		dettaglio.addContent(prodotto);
		return dettaglio;
	}
}
