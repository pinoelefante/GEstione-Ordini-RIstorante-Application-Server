package it.geori.as.communication;

import it.geori.as.controllers.DBIngredienti;
import it.geori.as.controllers.DBProdotti;
import it.geori.as.data.Ingrediente;
import it.geori.as.data.Prodotto;
import it.geori.as.data.ProdottoCategoria;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom2.Document;

public class ServletProdotti extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final static String 
		COMMAND_ADD_PRODUCT = "prodotto_add",
		COMMAND_REMOVE_PRODUCT = "prodotto_del",
		COMMAND_UPDATE_PRODUCT = "prodotto_update",
		COMMAND_LIST_PRODUCT = "prodotti_list",
		COMMAND_ADD_CATEGORY = "categoria_add",
		COMMAND_LIST_CATEGORY = "categoria_list";

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		Document xml = null;
		switch (action) {
			case COMMAND_ADD_CATEGORY: {
				if (!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())) {
					xml = XMLDocumentCreator.operationStatus(false, "Operazione consentita solo agli amministratori");
					break;
				}
				String nomeCat = req.getParameter("nome");
				if(nomeCat!=null && !nomeCat.isEmpty()){
					ProdottoCategoria cat = new ProdottoCategoria(0, nomeCat);
					if(DBProdotti.getInstance().addProdottoCategoria(cat))
						xml = XMLDocumentCreator.operationStatus(true, "");
				}
			}
			case COMMAND_ADD_PRODUCT:
				if (!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())) {
					xml = XMLDocumentCreator.operationStatus(false, "Operazione consentita solo agli amministratori");
					break;
				}
				String categoriaAdd = req.getParameter("categoria");
				String nomeAdd = req.getParameter("nome");
				String descr = req.getParameter("descrizione");
				String prezzoAdd = req.getParameter("prezzo");
				String ingredienti = req.getParameter("ingredienti");
				if (nomeAdd != null && nomeAdd.length() > 0 && prezzoAdd != null) {
					try {
						double prezzo = Double.parseDouble(prezzoAdd);
						int idCategoria = Integer.parseInt(categoriaAdd);
						Prodotto p = new Prodotto(idCategoria, 0, nomeAdd, descr, prezzo);
						String[] ingr = ingredienti.split(";");
						boolean ingredientiOK = true;
						for(int i=0;i<ingr.length;i++){
							if(ingr[i].trim().length()>0){
								int id = Integer.parseInt(ingr[i]);
								Ingrediente ingred = DBIngredienti.getInstance().getIngredienteByID(id);
								if(ingred == null){
									xml = XMLDocumentCreator.operationStatus(false, "Ingrediente "+ ingr[i]+" non trovato");
									ingredientiOK = false;
									break;
								}
								else 
									p.getIngredienti().add(ingred);
							}
						}
						if (ingredientiOK && DBProdotti.getInstance().addProdotto(p)) {
							xml = XMLDocumentCreator.operationStatus(true, "");
						}
						else if(!ingredientiOK){
							p.getIngredienti().clear();
							p = null;
							break;
						}
						else
							xml = XMLDocumentCreator.operationStatus(false, "Il prodotto " + nomeAdd + " potrebbe essere già presente o i parametri potrebbero non essere validi");
					}
					catch (Exception e) {}
				}
				break;
			case COMMAND_REMOVE_PRODUCT:
				if (!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())) {
					xml = XMLDocumentCreator.operationStatus(false, "Operazione consentita solo agli amministratori");
					break;
				}
				String idRemove = req.getParameter("id");
				if (idRemove != null) {
					try {
						int id = Integer.parseInt(idRemove);
						boolean r = DBProdotti.getInstance().removeProdotto(id);
						if (r)
							xml = XMLDocumentCreator.operationStatus(true, "");
						else
							xml = XMLDocumentCreator.operationStatus(false, "");
					}
					catch (Exception e) {}
				}
				break;
			case COMMAND_UPDATE_PRODUCT:
				if (!AuthenticatedUsers.getInstance().isAdmin(req.getCookies())) {
					xml = XMLDocumentCreator.operationStatus(false, "Operazione consentita solo agli amministratori");
					break;
				}
				String nomeUpd = req.getParameter("nome");
				String idUpd = req.getParameter("id");
				String prezzoUpd = req.getParameter("prezzo");
				String newCat = req.getParameter("categoria");
				String descrUpd = req.getParameter("descrizione");
				String ingrs = req.getParameter("ingredienti");
				if (nomeUpd != null && idUpd != null && prezzoUpd != null && ingrs != null) {
					try {
						int id = Integer.parseInt(idUpd);
						double prezzo = Double.parseDouble(prezzoUpd);
						int categoria = Integer.parseInt(newCat);
						
						Prodotto p = new Prodotto(categoria, id, nomeUpd, descrUpd, prezzo);
						String[] ingrs_new = ingrs.split(";");
						for(int i=0;i<ingrs_new.length;i++){
							Ingrediente ingr = DBIngredienti.getInstance().getIngredienteByID(Integer.parseInt(ingrs_new[i]));
							if(ingr!=null)
								p.getIngredienti().add(ingr);
						}
						if (DBProdotti.getInstance().updateProdotto(p)) {
							xml = XMLDocumentCreator.operationStatus(true, "");
						}
						else
							xml = XMLDocumentCreator.operationStatus(false, "Prodotto inesistente o parametri non validi");
					}
					catch (Exception e) {}
				}
				break;
			case COMMAND_LIST_PRODUCT:
				ArrayList<Prodotto> l = DBProdotti.getInstance().getCacheList();
				xml = XMLDocumentCreator.listProdotti(l);
				break;
			case COMMAND_LIST_CATEGORY:
    			ArrayList<ProdottoCategoria> lc = DBProdotti.getInstance().getListProdottoCategoria();
    			xml = XMLDocumentCreator.listProdottoCategoria(lc);
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
