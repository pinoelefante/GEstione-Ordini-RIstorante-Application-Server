package it.geori.as.controllers;

public class DBOrdini extends CacheManager {
	private static DBOrdini instance;
	public static DBOrdini getInstance(){
		if(instance == null){
			instance = new DBOrdini();
		}
		return instance;
	}
	private DBOrdini(){
		super();
	}
	
	public boolean isOrderCodeExists(String orderCode){
		//TODO
		return false;
	}
	
	private final static String setNormalized = "abc0def1ghi2jk3lm4no5pqr6st7u8vwx9yz";
	private static String getRandomString(String set) {
		String indici = (System.currentTimeMillis()+"").substring(4); //8 indici
		StringBuilder sb = new StringBuilder();

		for (int loop = 0; loop < indici.length(); loop++) {
			int index = Integer.parseInt(indici.charAt(loop)+"");
			sb.append(set.charAt(index));
		}

		String nonce = sb.toString();
		return nonce;
	}
	
}
