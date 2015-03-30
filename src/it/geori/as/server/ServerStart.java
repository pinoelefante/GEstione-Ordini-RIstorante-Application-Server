package it.geori.as.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;


public class ServerStart {
	private static JettyServer jettyServer;
	
	public static void main(String[] args) {
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] { new AppContextBuilder().buildWebAppContext() });

		jettyServer = new JettyServer();
		jettyServer.setHandler(contexts);
		try {
			jettyServer.start();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			System.exit(-1);
		}
	
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				System.out.println("Shutdown hook - main");
				if(jettyServer.isStarted()) {
					try {
						jettyServer.stop();
					} 
					catch (Exception exception) {
						exception.printStackTrace();
					}
				}
			}
		},"Stop Jetty Hook")); 
	}
}
