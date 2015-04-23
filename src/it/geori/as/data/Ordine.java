package it.geori.as.data;

import it.geori.as.data.interfaces.Identifier;

import java.util.ArrayList;

public class Ordine implements Identifier{
	public final static int STATO_CREATO=0,
			STATO_IN_CORSO=1, STATO_IN_PREPARAZIONE=2,STATO_PREPARATO=3,STATO_PAGATO=10;
	private Integer id, tavolo, coperti, sconto, servitoDa, statoOrdine;
	private double costoTotale;
	private String dataCreazione, dataChiusura, guestCode;
	private ArrayList<OrdineDettagli> dettagli_ordine;
	
	public Ordine(int id, int tavolo, int coperti, int servitoDa, String dataCreazione, String guestCode) {
		this(id, tavolo, coperti, 0, servitoDa, STATO_CREATO, 0.0f, dataCreazione, "", guestCode);
	}
	public Ordine(int id, int tavolo, int coperti, int sconto, int servitoda, int statoOrdine, double costo, String creazione, String chiusura, String guest){
		setId(id);
		setTavolo(tavolo);
		setCoperti(coperti);
		setSconto(sconto);
		setServitoDa(servitoda);
		setCostoTotale(costo);
		setDataCreazione(creazione);
		setDataChiusura(chiusura);
		setGuestCode(guest);
		dettagli_ordine = new ArrayList<OrdineDettagli>();
	}

	public Integer getStatoOrdine() {
		return statoOrdine;
	}
	public void setStatoOrdine(Integer statoOrdine) {
		this.statoOrdine = statoOrdine;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getTavolo() {
		return tavolo;
	}

	public void setTavolo(Integer tavolo) {
		this.tavolo = tavolo;
	}

	public Integer getCoperti() {
		return coperti;
	}

	public void setCoperti(Integer coperti) {
		this.coperti = coperti;
	}

	public Integer getSconto() {
		return sconto;
	}

	public void setSconto(Integer sconto) {
		this.sconto = sconto;
	}

	public Integer getServitoDa() {
		return servitoDa;
	}

	public void setServitoDa(Integer servitoDa) {
		this.servitoDa = servitoDa;
	}

	public double getCostoTotale() {
		return costoTotale;
	}

	public void setCostoTotale(double costoTotale) {
		this.costoTotale = costoTotale;
	}

	public String getDataCreazione() {
		return dataCreazione;
	}

	public void setDataCreazione(String dataCreazione) {
		this.dataCreazione = dataCreazione;
	}

	public String getDataChiusura() {
		return dataChiusura;
	}

	public void setDataChiusura(String dataChiusura) {
		this.dataChiusura = dataChiusura;
	}

	public String getGuestCode() {
		return guestCode;
	}

	public void setGuestCode(String guestCode) {
		this.guestCode = guestCode;
	}

	@Override
	public int getID() {
		return id;
	}
	public ArrayList<OrdineDettagli> getDettagliOrdine(){
		return dettagli_ordine;
	}
	public void addDettaglioOrdine(OrdineDettagli det){
		dettagli_ordine.add(det);
	}
}
