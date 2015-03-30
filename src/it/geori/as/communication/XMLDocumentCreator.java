package it.geori.as.communication;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class XMLDocumentCreator {
	
	public static void sendResponse(HttpServletResponse response, Document doc){
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
		return operationStatus(false, "Parametri non presenti o valori non validi");
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
}
