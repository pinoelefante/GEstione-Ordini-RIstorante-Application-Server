package it.geori.as.data;

import it.geori.as.data.interfaces.Identifier;

import java.util.ArrayList;

public class OrdineDettagli implements Identifier{
	public final static int 
		STATO_IN_CODA=0,
		STATO_IN_PREPARAZIONE=1,
		STATO_PREPARATO=2,
		STATO_INGREDIENTI_NON_PRESENTI=3,
		STATO_PAGATO = 4;
	private int id;
	private ArrayList<Dettaglio> dettagli;
	private int quantita;
	private String note;
	private int stato;
	
	public OrdineDettagli(int id, int quantita, int stato, String note){
		this.id = id;
		this.quantita = quantita;
		this.stato = stato;
		this.note = note;
		this.dettagli = new ArrayList<Dettaglio>();
	}
	public ArrayList<Dettaglio> getProdotti() {
		return dettagli;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void addDettaglio(Dettaglio d){
		if(getDettaglioByProdotto(d.getProdotto())==null)
			dettagli.add(d);
	}
	public void addModificheToProdotto(Prodotto p, ArrayList<Ingrediente> mod, String tipo){
		Dettaglio d = getDettaglioByProdotto(p);
		if(d==null)
			return;
		switch(tipo){
    		case "+":
    			d.addToAdd(mod);
    			break;
    		case "-":
    			d.addToRem(mod);
    			break;
		}
	}
	public void setQuantita(int newQuantita){
		if(newQuantita==0){
			dettagli.clear();
			quantita = 0;
		}
		else
			quantita = newQuantita;
	}
	public int getQuantita(){
		return quantita;
	}
	@Override
	public int getID() {
		return id;
	}
	public void setNote(String note){
		this.note = note;
	}
	public String getNote(){
		return note;
	}
	public void ingredientiToRemove(Prodotto p, Ingrediente ingr){
		if(isToRemove(p, ingr)){
			Dettaglio d = getDettaglioByProdotto(p);
			if(d==null)
				return;
			ArrayList<Ingrediente> l=d.getToRem();
			for(int i=0;i<l.size();i++){
				if(l.get(i).getID()==ingr.getID()){
					l.remove(i);
					break;
				}
			}	
		}
	}
	public Dettaglio getDettaglioByProdotto(Prodotto p){
		for(int i=0;i<dettagli.size();i++){
			if(dettagli.get(i).getId()==p.getID())
				return dettagli.get(i);
		}
		return null;
	}
	private boolean isToRemove(Prodotto p, Ingrediente ingr){
		Dettaglio d = getDettaglioByProdotto(p);
		if(d==null)
			return false;
		ArrayList<Ingrediente> l=d.getToRem();
		for(int i=0;i<l.size();i++){
			if(l.get(i).getID()==ingr.getID())
				return true;
		}
		return false;
	}
	public void ingredientiToAdd(Prodotto p, Ingrediente ingr){
		if(isToAdd(p, ingr)){
			Dettaglio d = getDettaglioByProdotto(p);
			if(d==null)
				return;
			ArrayList<Ingrediente> l=d.getToAdd();
			l.add(ingr);	
		}
	}
	private boolean isToAdd(Prodotto p, Ingrediente ingr){
		Dettaglio d = getDettaglioByProdotto(p);
		if(d==null)
			return false;
		ArrayList<Ingrediente> l=d.getToAdd();
		for(int i=0;i<l.size();i++){
			if(l.get(i).getID()==ingr.getID())
				return false;
		}
		return true;
	}
	public int getStato() {
		return stato;
	}
	public void setStato(int stato) {
		this.stato = stato;
	}
	public boolean isRemovable(){
		switch(getStato()){
			case STATO_IN_CODA:
			case STATO_INGREDIENTI_NON_PRESENTI:
				return true;
		}
		return false;
	}
	public boolean isModificable(){
		switch(getStato()){
			case STATO_IN_CODA:
			case STATO_INGREDIENTI_NON_PRESENTI:
				return true;
		}
		return false;
	}
}
