package it.geori.as.communication;

import it.geori.as.controllers.DBIngredienti;
import it.geori.as.controllers.DBOrdini;
import it.geori.as.controllers.DBProdotti;
import it.geori.as.data.Dettaglio;
import it.geori.as.data.Ingrediente;
import it.geori.as.data.Ordine;
import it.geori.as.data.OrdineDettagli;
import it.geori.as.data.Prodotto;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom2.Document;
import org.jdom2.Element;
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
		COMMAND_ORDER_LIST_ALL = "order_list_all",
		COMMAND_ADD_TO_ORDER="order_add_to",
		COMMAND_REMOVE_FROM_ORDER="order_remove_from",
		COMMAND_MODIFY_FROM_ORDER="order_modify_from",
		COMMAND_SET_STATUS_FROM_ORDER="order_change_status_item",
		COMMAND_SET_STATUS="order_change_status",
		COMMAND_CALC_ORDER="order_calc",
		COMMAND_SCONTO_ORDER="order_add_sconto",
		COMMAND_GUEST_ORDER="order_guest";
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		Document docResponse = null;
		String xml = req.getParameter("xml");
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
						Ordine ordine = new Ordine(0, tavolo, coperti, idServedBy, null, null);
						boolean res = DBOrdini.getInstance().addNewOrder(ordine);
						if(res)
							docResponse = XMLDocumentCreator.listOrdine(ordine);
						else
							docResponse = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_UPDATE);
					}
					break;
				case COMMAND_DEL_ORDER:
					if(!AuthenticatedUsers.getInstance().isAuthenticated(req.getCookies())){
						docResponse = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_LOGGATO);
						break;
					}
					String del_order_id = req.getParameter("id");
					if(del_order_id!=null && del_order_id.length()>0){
						try {
							int id = Integer.parseInt(del_order_id);
							boolean res = DBOrdini.getInstance().removeOrdine(id);
							docResponse = XMLDocumentCreator.operationStatus(res, "");
						}
						catch(Exception e){
							docResponse = XMLDocumentCreator.errorParameters();
						}
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
							Ordine ordine = DBOrdini.getInstance().getOrdineByID(idOrd);
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
					String mod_order_id = req.getParameter("id");
					String mod_order_tavolo = req.getParameter("tavolo");
					String mod_order_coperti = req.getParameter("coperti");
					if(mod_order_id!=null && mod_order_tavolo!=null && mod_order_coperti!=null){
						try {
							int id = Integer.parseInt(mod_order_id);
							int tavolo = Integer.parseInt(mod_order_tavolo);
							int coperti = Integer.parseInt(mod_order_coperti);
							Ordine ordine = DBOrdini.getInstance().getOrdineByID(id);
							int oldCoperti = ordine.getCoperti();
							int oldTavolo = ordine.getTavolo();
							if(oldCoperti!=coperti || oldTavolo!=tavolo){
								ordine.setCoperti(coperti);
								ordine.setTavolo(tavolo);
								boolean res = DBOrdini.getInstance().updateOrdine(ordine);
								if(res){
									docResponse = XMLDocumentCreator.operationStatus(true, "");
								}
								else {
									ordine.setCoperti(oldCoperti);
									ordine.setTavolo(oldTavolo);
								}
							}
							else {
								docResponse = XMLDocumentCreator.operationStatus(true, "");
							}
						}
						catch(Exception e){}
					}
					break;
				case COMMAND_SET_STATUS:
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						docResponse = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_SEI_ADMIN);
						break;
					}
					String set_status_id = req.getParameter("id");
					String set_status_new_status = req.getParameter("stato");
					if(set_status_id!=null && set_status_new_status!=null){
						try {
							int id = Integer.parseInt(set_status_id);
							int status = Integer.parseInt(set_status_new_status);
							Ordine ordine = DBOrdini.getInstance().getOrdineByID(id);
							int lastStato = ordine.getStatoOrdine();
							if(lastStato==status){
								docResponse = XMLDocumentCreator.operationStatus(true, "");
							}
							else {
								ordine.setStatoOrdine(status);
								if(DBOrdini.getInstance().updateOrdine(ordine))
									docResponse = XMLDocumentCreator.listOrdine(ordine);
								else
									ordine.setStatoOrdine(lastStato);
							}
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					break;
				case COMMAND_ADD_TO_ORDER:
					if(!AuthenticatedUsers.getInstance().isAuthenticated(req.getCookies())){
						docResponse = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_LOGGATO);
						break;
					}
					String add_to_id = req.getParameter("id");
					if(add_to_id!=null && xml!=null){
						try {
							int id = Integer.parseInt(add_to_id);
							Document xmlDoc = readXML(xml, true);
							ArrayList<OrdineDettagli> dettagli = parseXML(xmlDoc);
							boolean res = DBOrdini.getInstance().addDettagliOrdineToOrdine(dettagli, id);
							if(res){
								Ordine ordine = DBOrdini.getInstance().getOrdineByID(id);
								docResponse = XMLDocumentCreator.listOrdine(ordine);
								//TODO notify
							}
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					break;
				case COMMAND_REMOVE_FROM_ORDER:
					if(!AuthenticatedUsers.getInstance().isAuthenticated(req.getCookies())){
						docResponse = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_LOGGATO);
						break;
					}
					String rem_from_id = req.getParameter("id");
					if(rem_from_id!=null && xml!=null){
						try {
							Integer id = Integer.parseInt(rem_from_id);
							Document xmlDoc = readXML(xml, true);
							ArrayList<OrdineDettagli> dettagli = parseXML(xmlDoc);
							int removed = DBOrdini.getInstance().removeItemsFromOrder(dettagli, id);
							boolean res = removed == dettagli.size();
							docResponse = XMLDocumentCreator.operationStatus(res, res?"":"Rimossi "+removed+"/"+dettagli.size());
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					//TODO notify
					break;
				case COMMAND_CALC_ORDER:
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						docResponse = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_LOGGATO);
						break;
					}
					String calc_order_id = req.getParameter("id");
					if(calc_order_id!=null){
						try {
							int id = Integer.parseInt(calc_order_id);
							boolean res = DBOrdini.getInstance().calcolaTotale(id);
							if(res){
								Ordine ordine = DBOrdini.getInstance().getOrdineByID(id);
								docResponse = XMLDocumentCreator.listOrdine(ordine);
							}
							else
								docResponse = XMLDocumentCreator.operationStatus(false, "");
						}
						catch(Exception e){
							docResponse = XMLDocumentCreator.errorParameters();
						}
					}
					break;
				case COMMAND_SCONTO_ORDER:
					if(!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())){
						docResponse = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_LOGGATO);
						break;
					}
					String sconto_order_id = req.getParameter("id");
					String sconto_order_sconto = req.getParameter("sconto");
					if(sconto_order_id!=null){
						try {
							int id = Integer.parseInt(sconto_order_id);
							int sconto = Integer.parseInt(sconto_order_sconto);
							Ordine ordine = DBOrdini.getInstance().getOrdineByID(id);
							int oldSconto = ordine.getSconto();
							if(oldSconto==sconto){
								docResponse = XMLDocumentCreator.listOrdine(ordine);
							}
							else {
								ordine.setSconto(sconto);
								boolean res = DBOrdini.getInstance().calcolaTotale(ordine);
								if(res){
									docResponse = XMLDocumentCreator.listOrdine(ordine);
								}
								else {
									ordine.setSconto(oldSconto);
									docResponse = XMLDocumentCreator.operationStatus(false, "");
								}
							}
						}
						catch(Exception e){}
					}
					break;
				case COMMAND_GUEST_ORDER:
					String guest_code = req.getParameter("guestCode");
					if(guest_code!=null){
						Ordine o = DBOrdini.getInstance().getOrdineByGuestCode(guest_code);
						if(o!=null){
							docResponse = XMLDocumentCreator.listOrdine(o);
						}
					}
					break;
				case COMMAND_ORDER_LIST_ALL:
					if(!AuthenticatedUsers.getInstance().isAuthenticated(req.getCookies())){
						docResponse = XMLDocumentCreator.operationStatus(false, Localization.MESSAGGIO_ERRORE_NON_LOGGATO);
						break;
					}
					ArrayList<Ordine> ordini24Ore = DBOrdini.getInstance().getOrdiniUltime24OreAperti();
					docResponse = XMLDocumentCreator.listOrdini(ordini24Ore);
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
	@SuppressWarnings("rawtypes")
	private ArrayList<OrdineDettagli> parseXML(Document xml){
		Element root = xml.getRootElement();
		List dettagli = root.getChildren("dettaglio");
		ArrayList<OrdineDettagli> listDettagli = new ArrayList<OrdineDettagli>();
		for(int i=0;i<dettagli.size();i++){
			Element dettaglio = (Element) dettagli.get(i);
			OrdineDettagli dett = parseOrdineDettagli(dettaglio);
			listDettagli.add(dett);
		}
		return listDettagli;
	}
	@SuppressWarnings("rawtypes")
	private OrdineDettagli parseOrdineDettagli(Element item){
		Integer id = Integer.parseInt(item.getChildText("id")==null?"0":item.getChildText("id"));
		Integer quantita = Integer.parseInt(item.getChildText("quantita"));
		String note = item.getChildText("note");
		Integer stato = Integer.parseInt(item.getChildText("stato"));
		List prodotti = item.getChildren("prodotto");
		ArrayList<Dettaglio> dettagli = new ArrayList<Dettaglio>();
		for(int i=0;i<prodotti.size();i++){
			Element e_prodotto = (Element) prodotti.get(i);
			Prodotto p = parseProdotto(e_prodotto);
			ArrayList<Ingrediente> conList = new ArrayList<Ingrediente>();
			List ingredientiCon=e_prodotto.getChild("con").getChildren("ingrediente");
			for(int j=0;i<ingredientiCon.size();i++){
				Ingrediente ingr = parseIngrediente((Element) ingredientiCon.get(j));
				conList.add(ingr);
			}
			ArrayList<Ingrediente> senzaList = new ArrayList<Ingrediente>();
			List ingredientiSenza=e_prodotto.getChild("senza").getChildren("ingrediente");
			for(int j=0;i<ingredientiSenza.size();i++){
				Ingrediente ingr = parseIngrediente((Element) ingredientiSenza.get(j));
				senzaList.add(ingr);
			}
			Dettaglio dett = new Dettaglio(p);
			dett.addToAdd(conList);
			dett.addToRem(senzaList);
			dettagli.add(dett);
		}
		OrdineDettagli ordineDett = new OrdineDettagli(id, quantita, stato, note);
		ordineDett.addDettagli(dettagli);
		return ordineDett;
	}
	private Prodotto parseProdotto(Element item){
		Integer id = Integer.parseInt(item.getChildText("id"));
		Prodotto prod = DBProdotti.getInstance().getProdottoByID(id);
		return prod;
	}
	private Ingrediente parseIngrediente(Element item){
		Integer id = Integer.parseInt(item.getChildText("id"));
		Ingrediente ingr = DBIngredienti.getInstance().getIngredienteByID(id);
		/*
		if(ingr == null) {
			String nome = item.getChildText("nome");
			double prezzo = Double.parseDouble(item.getChildText("prezzo"));
			ingr = new Ingrediente(id, nome, prezzo);	
		}
		*/
		return ingr;
	}
	/*
	public static void main(String[] args){
		ServletOrdini s = new ServletOrdini();
		Document xml = s.readXML("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48ZGV0dGFnbGk_PGRldHRhZ2xpbz48cXVhbnRpdGE_MTwvcXVhbnRpdGE_PHN0YXRvPjA8L3N0YXRvPjxub3RlPlBvc2F0ZSBjYWxkZTwvbm90ZT48bGlzdF9wcm9kb3R0aT48cHJvZG90dG8_PGlkPjE8L2lkPjxub21lPkNsYXNzaWNvPC9ub21lPjxwcmV6em8_Mi41PC9wcmV6em8_PGRlc2NyaXppb25lPlBhbmlubyB0cmFkaXppb25hbGUgY29uIHByb3NjaXV0dG8gZSBtYWlvbmVzZTwvZGVzY3JpemlvbmU_PGluZ3JlZGllbnRpPjxpbmdyZWRpZW50ZT48aWQ_MTwvaWQ_IDxub21lPlByb3NjaXV0dG88L25vbWU_PHByZXp6bz4wLjU8L3ByZXp6bz48L2luZ3JlZGllbnRlPjwvaW5ncmVkaWVudGk_PGNvbiAvPjxzZW56YSAvPjwvcHJvZG90dG8_PC9saXN0X3Byb2RvdHRpPjwvZGV0dGFnbGlvPjwvZGV0dGFnbGk_", true);
		s.parseXML(xml);
	}
	*/
	private String deBASE64(String base64){
		byte[] decoded = Base64.getDecoder().decode(base64);
		return new String(decoded);
	}
	private String unescapeBase64(String escaped){
		return escaped.replace("_", "+").replace("@", "/").replace(".", "=");
	}
}
