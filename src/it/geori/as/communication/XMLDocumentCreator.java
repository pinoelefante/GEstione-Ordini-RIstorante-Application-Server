package it.geori.as.communication;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
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
	public static Document operationStatus(boolean s){
		return null;
	}
	public static Document errorParameters(){
		return null;
	}
}
