package it.geori.as.controllers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnectionPool {
	
	static {
		freeDbConnections = new ArrayList<Connection>();
		try {
			DBConnectionPool.loadDbProperties();
			DBConnectionPool.loadDbDriver();
		}
		catch (ClassNotFoundException e) {
			System.err.println("DB DRIVER NOT FOUND!");
			System.exit(1);
		}
		catch (IOException e) {
			System.err.println("DB CONNECTION POOL ERROR!");
			System.exit(2);
		}
	}
	
	private static Properties dbProperties;
	private static List<Connection> freeDbConnections;

	@SuppressWarnings("resource")
	public static synchronized Connection getConnection() throws SQLException {
		Connection connection;

		if (!freeDbConnections.isEmpty()) {
			connection = (Connection) freeDbConnections.get(0);
			DBConnectionPool.freeDbConnections.remove(0);

			try {
				if (connection.isClosed()) {
					connection = DBConnectionPool.getConnection();
				}
			}
			catch (SQLException e) {
				connection = DBConnectionPool.getConnection();
			}
		}
		else {
			connection = DBConnectionPool.createDBConnection();
		}

		return connection;
	}

	public static synchronized void releaseConnection(
			Connection pReleasedConnection) {
		try {
			DBConnectionPool.freeDbConnections.add(pReleasedConnection);
			pReleasedConnection.close();
		}
		catch (SQLException ex) {
			Logger.getLogger(DBConnectionPool.class.getName()).log(Level.SEVERE,null,ex);
		}
	}

	private static Connection createDBConnection() throws SQLException {
		Connection newConnection = null;

		// Create a new db connection using the db properties
		newConnection = DriverManager.getConnection(DBConnectionPool.dbProperties.getProperty("url"),
				DBConnectionPool.dbProperties.getProperty("username"),
				DBConnectionPool.dbProperties.getProperty("password"));

		newConnection.setAutoCommit(false);

		return newConnection;
	}

	private static void loadDbDriver() throws ClassNotFoundException {
		Class.forName(dbProperties.getProperty("driver"));
	}

	private static void loadDbProperties() throws IOException {
		InputStream fileProperties = new FileInputStream("database.properties");
		dbProperties = new Properties();
		dbProperties.load(fileProperties);
	}
}
