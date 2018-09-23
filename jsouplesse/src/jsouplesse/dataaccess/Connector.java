package jsouplesse.dataaccess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jsouplesseutil.DBUtils;

/**
 * Creates and manages the connection to the database. If the application is run for the
 * first time, Connector also knows how to get and execute the DDL needed for creating
 * the database.
 * NB: in multi-threaded implementation, the synchronized managing capabilities of this
 * class will have to... be brought into existence.
 */
public class Connector {
	
	private static final String databasePath = "jdbc:sqlite:C:/scrapeOnDemand/sqlite/jsouplesse.db";
	
	private Statement statement;
		
	private Connection connection;
	
	/** Indicates whether the initial DDL statement has been executed. */
	private boolean isDatabaseCreated = false;
	
	public boolean initialize() {
		try {
			// Get a connection to the database from the driver manager (and possibly create the 
			// database - if this is first run).
			connection = DriverManager.getConnection(databasePath);
			// Check if this Connector has already created the database.
			if (!isDatabaseCreated) {
				// Try out a simple query to determine whether database was created in earlier run.
		        String tableCountQuery = "select count(1) as count from sqlite_master where type = 'table'";
		        Statement stat = connection.createStatement();
		        ResultSet rs = stat.executeQuery(tableCountQuery);
		        if (rs.getInt("count") == 0)
		        	// The database has only just now been created. Time to add some tables.
		        	createTables();
			}
		} catch (SQLException sqlex) {
			System.out.println("Something went horribly wrong.");
			sqlex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed())
			connection = DriverManager.getConnection(databasePath);
		return connection;
	}
	
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return connection.prepareStatement(sql);
	}
	
	public Statement createStatement() throws SQLException {
		return connection.createStatement();
	}
	
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		connection.setAutoCommit(autoCommit);
	}
	
	public void commit() throws SQLException {
		connection.commit();
	}
	
	public void close() {
		DBUtils.closeConnectionQuietly(connection);
	}
	
	private void createTables() throws SQLException {
				
		InputStream is = getClass().getResourceAsStream("createJsouplesseDB.sql");
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			
			String sql = "";
			String line;
			// Wait until all tables are created before committing.
			connection.setAutoCommit(false);
			
			while ((line = reader.readLine()) != null) {
				
				if (line.startsWith("--"))
					// Skip lines with comments.
					continue;
				// Add the current line to the statement.
				sql += line;
				
				if (line.contains(";")) {
					// End of statement reached. Execute the statement.
					statement = connection.createStatement();
					statement.execute(sql);
					
					// Reset the sql variable.
					sql = "";
				}
			}
			// Successfully created the database. Commit and log success message.
			connection.commit();
			System.out.println("Database was created successfully.");
			
		} catch (IOException ioex) {
			System.out.println("Failed to read the DDL file.");
		} finally {
			DBUtils.closeStatementQuietly(statement);
		}
	}
}



