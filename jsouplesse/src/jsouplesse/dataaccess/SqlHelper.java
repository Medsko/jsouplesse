package jsouplesse.dataaccess;

import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import jsouplesse.util.DBUtils;

/**
 * Helper class that can be used by any DAO to delegate creating and 
 * executing SQL statements, retrieving the result set and moving its
 * values to the data object.
 */
public class SqlHelper {

	// TODO: at some point, this class will have to be divided into PreparedStatementBuilder and PreparedStatementExecutor, to make PrepSt reusable.
	// TODO: the current way of creating a PreparedStatement does not allow the PrepSt to be reused
	// for other instances of a sub class. An SqlHelper should compose the PrepSt and save it
	// as an instance variable so it can be reused by other instances of the DAO sub class.
	// This way, PreparedStatement could be filled, then call PreparedStatement.addBatch() until
	// all instances have been added to the batch, then call PreparedStatement.executeBatch() to
	// save all data in one go.

	/**
	 * The name of the table for which a statement is currently being
	 * prepared.
	 */
	protected String tableName;
	
	
	protected Connector connector;
	
	// Processing
	
	protected PreparedStatement prepSt;
	
	protected ResultSet resultSet;
	
	/**
	 * Holds the values of the primary key that are set in fillValues(), with
	 * the column name as key.
	 */
	protected Map<String, Integer> primaryKeyMap;
	
	/**
	 * Holds the values of columns that are not part of the primary key. 
	 * Values are set in fillValues(), with the column name as key.
	 */
	protected Map<String, Object> values;
		
	/**
	 * Holds the column names in the order that they have been added to the
	 * prepared statement.
	 * 
	 * When a statement has already been prepared for a data object, this
	 * can be used to set the values of another object of the same type
	 * on the prepared statement.
	 */
	protected String[] columnNames;

	public SqlHelper(Connector connector) {
		this.connector = connector;
	}
	

	/**
	 * Retrieves the row corresponding to the set primary key from the database.
	 * @throws SQLException 
	 */
	public void executeSelect() throws SQLException {
		
		// Check if the statement has been prepared.
		if (prepSt == null)
			throw new IllegalStateException("The statement has not been prepared! "
					+ "Call prepareSelect() before calling executeSelect().");
		
		// Set the primary key.
		for (int i=1; i<=columnNames.length; i++) {
			
			Integer pkValue = primaryKeyMap.get(columnNames[i]);
			
			if (pkValue == null)
				throw new UnsupportedOperationException("The primary key is not complete!");
			
			prepSt.setInt(i, pkValue);
		}
		
		// Execute the query to retrieve the result set.
		resultSet = prepSt.executeQuery();
	}
	
	
	/**
	 * Prepares the PreparedStatement that is used to perform the read action in {@link #executeSelect()}.
	 */
	public void prepareSelect() throws SQLException {
		
		StringBuilder selectSQL = new StringBuilder("select * from ");
		
		selectSQL.append(tableName);		
		selectSQL.append(" where ");
		
		columnNames = new String[primaryKeyMap.size()];
		
		int i = 0;

		for (String identifier : primaryKeyMap.keySet()) {
			selectSQL.append(identifier);
			selectSQL.append(" = ? and ");
			columnNames[i] = identifier;
			i++;
		}
		// Remove the last " and ".
		selectSQL.delete(selectSQL.length() - 5, selectSQL.length());
		
		prepSt = connector.prepareStatement(selectSQL.toString());
	}
	
	
	public void executeInsert() throws SQLException {
		
		// Check if the statement has been prepared.
		if (prepSt == null)
			throw new IllegalStateException("The statement has not been prepared! "
					+ "Call prepareInsert() before calling executeInsert().");
		
		// Move all primary key columns and values to the values map, so all
		// values for the statement can be retrieved from the same map.
		for (String pkColumn : primaryKeyMap.keySet()) {

			if (primaryKeyMap.get(pkColumn) == null)
				throw new UnsupportedOperationException("The primary key is not complete!");

			values.put(pkColumn, primaryKeyMap.get(pkColumn));
		}
		
		int i = 1;
		
		// Set the values in the map on the statement.
		for (String columnName : columnNames) {
			Object value = values.get(columnName);
			setValueOnPrepSt(i, value);
			i++;
		}
		
		// Execute the statement.
		if (prepSt.executeUpdate() == 1)
			System.out.println("Row inserted into " + tableName);
		else
			System.out.println("Failed to insert row into " + tableName);
	}
	
	/**
	 * Prepares insert statement.
	 */
	public void prepareInsert() throws SQLException {
		
		StringBuilder insertSQL = new StringBuilder("insert into ");
		insertSQL.append(tableName);
		insertSQL.append(" (");
		
		// Determine the required size for the array that will hold the column names.
		int totalColumns = primaryKeyMap.size() + values.size();
		columnNames = new String[totalColumns];
		
		int index = 0;

		// Add each regular column to the insert statement.
		for (String column : values.keySet()) {
			insertSQL.append(column);
			insertSQL.append(", ");
			columnNames[index] = column;
			index++;
		}
		
		// Add each primary key column to the insert statement.
		for (String pkColumn : primaryKeyMap.keySet()) {
			insertSQL.append(pkColumn);
			insertSQL.append(", ");
			columnNames[index] = pkColumn;
			index++;
		}

		// Remove the last comma and whitespace.
		insertSQL.delete(insertSQL.length() - 2, insertSQL.length());
		
		insertSQL.append(") values (");
		
		for (int j=0; j<columnNames.length; j++) {
			insertSQL.append("?, ");
		}
		// Remove the last comma and whitespace.
		insertSQL.delete(insertSQL.length() - 2, insertSQL.length());

		insertSQL.append(")");
		
		System.out.println(insertSQL);
		
		prepSt = connector.prepareStatement(insertSQL.toString());
	}
	
	
	public void prepareUpdate() throws SQLException {

		StringBuilder updateSQL = new StringBuilder("update ");
		updateSQL.append(tableName);
		updateSQL.append(" set ");
		
		// Determine the required size for the array that will hold the column names.
		int totalColumns = primaryKeyMap.size() + values.size();
		columnNames = new String[totalColumns];
		
		int index = 0;
		
		for (String columnName : values.keySet()) {
			updateSQL.append(columnName);
			updateSQL.append(" = ?, ");
			columnNames[index] = columnName;
			index++;
		}
		// Remove the last comma and whitespace.
		updateSQL.delete(updateSQL.length() - 2, updateSQL.length());

		updateSQL.append(" where ");
		
		for (String pkColumn : primaryKeyMap.keySet()) {
			updateSQL.append(pkColumn);
			updateSQL.append(" = ? and ");
			columnNames[index] = pkColumn;
			index++;
		}
		// Remove the last " and ".
		updateSQL.delete(updateSQL.length() - 5, updateSQL.length());
		// Prepare the statement.
		prepSt = connector.prepareStatement(updateSQL.toString());
	}
	
	
	public void executeUpdate() throws SQLException {
		
		// Check if the statement has been prepared.
		if (prepSt == null)
			throw new IllegalStateException("The statement has not been prepared! "
					+ "Call prepareUpdate() before calling executeUpdate().");
		
		// Move all primary key columns and values to the values map, so all
		// values for the statement can be retrieved from the same map.
		for (String pkColumn : primaryKeyMap.keySet()) {
			
			if (primaryKeyMap.get(pkColumn) == null)
				throw new UnsupportedOperationException("The primary key is not complete!");

			values.put(pkColumn, primaryKeyMap.get(pkColumn));
		}
		int index = 1;
		
		// Set the values in the map on the statement.
		for (String columnName : columnNames) {
			Object value = values.get(columnName);
			setValueOnPrepSt(index, value);
			index++;
		}
		
		// Execute the update.
		prepSt.executeUpdate();
		System.out.println("Record successfully updated for table " + tableName);
	}
	
	
	public void prepareDelete() throws SQLException {
		
		StringBuilder deleteSQL = new StringBuilder("delete from ");
		deleteSQL.append(tableName);
		deleteSQL.append(" where ");
		
		columnNames = new String[primaryKeyMap.size()];
		
		int i = 0;
		
		// Set the parameter names.
		for (String pkColumn : primaryKeyMap.keySet()) {
			deleteSQL.append(pkColumn);
			deleteSQL.append(" = ? and ");
			columnNames[i] = pkColumn;
			i++;
		}
		// Remove the last " and ".
		deleteSQL.delete(deleteSQL.length() - 5, deleteSQL.length());
		
		System.out.println(deleteSQL);
		
		prepSt = connector.prepareStatement(deleteSQL.toString());
	}
	
	
	public void executeDelete() throws SQLException {
		
		int i = 1;
		
		// Set the primary key values on the prepared statement.
		for (String pkColumn: columnNames) {
			if (primaryKeyMap.get(pkColumn) == null)
				throw new UnsupportedOperationException("The primary key is not complete!");
			
			prepSt.setInt(i, primaryKeyMap.get(pkColumn));
			i++;
		}
		
		// Execute the delete statement and print the number of affected records.
		int rowsDeleted = prepSt.executeUpdate();
		System.out.println("Rows deleted from " + tableName + ": " + rowsDeleted);
	}
	
	/**
	 * Determines the next available identifier for the primary key. 
	 */
	public Integer determineNewId() throws SQLException {
		
		String columnNameForNewId = null;
		StringBuilder sql = new StringBuilder("select max(");
		StringBuilder whereClause = null;
		
		for (String pkColumn : primaryKeyMap.keySet()) {
			
			if (primaryKeyMap.get(pkColumn) == null) {
				
				if (columnNameForNewId != null)
					throw new IllegalArgumentException("For a primary key spanning "
							+ "multiple fields, only one new max id can be determined.");
				
				sql.append(pkColumn).append(") as ").append(pkColumn);
				columnNameForNewId = pkColumn;
				
			} else {
				
				if (whereClause == null)
					whereClause = new StringBuilder(" where ");
				else
					whereClause.append(" and ");
				
				whereClause.append(pkColumn).append(" = ").append(primaryKeyMap.get(pkColumn));
			}
		}
		
		sql.append(" from ").append(tableName);
		
		if (whereClause != null)
			sql.append(whereClause);

		Integer newId;
		Statement statement = null;
		
		try {

			statement = connector.createStatement();
			
			resultSet = statement.executeQuery(sql.toString());
			
			if (resultSet.next())
				newId = resultSet.getInt(columnNameForNewId) + 1;
			else
				newId = 1;
			
		} finally {
			DBUtils.closeQuietly(resultSet, statement, null);
		}
		
		return newId;
	}
	
	public void closeResultSetAndPreparedStatement() {
		DBUtils.closeQuietly(resultSet, prepSt, null);
	}
	
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		connector.setAutoCommit(autoCommit);
	}
	
	public void commit() throws SQLException {
		connector.commit();
	}
	
	/**
	 * This method should be used in fillPrimaryKeyValues() to set the {@link Integer}s
	 * which comprise the primary key for this table on the {@link #primaryKeyMap}.
	 */
	public void setPrimaryKeyInt(String columnName, Integer value) {
		// Add the value to the primary key map.
		primaryKeyMap.put(columnName, value);
	}
	
	public Integer getInt(String columnName) throws SQLException {
		// Get the value for the given column name from the result set.
		return resultSet.getInt(columnName);
	}
	
	public void setInt(String columnName, Integer value) {
		// Check if the value is null.
		if (value == null)
			// Add a Null.Integer to the map.
			values.put(columnName, new Null.Integer());
		// Set the non-null value.
		else
			values.put(columnName, value);
	}
	
	public String getString(String columnName) throws SQLException {
		// Get the value for the given column name from the result set.
		return resultSet.getString(columnName);
	}
	
	public void setString(String columnName, String value) {
		// Check if the value is null.
		if (value == null)
			// Add a Null.Integer to the map.
			values.put(columnName, new Null.String());
		// Set the non-null value.
		else
			values.put(columnName, value);
	}
	
	public Double getDouble(String columnName) throws SQLException {
		return resultSet.getDouble(columnName);
	}
	
	public void setDouble(String columnName, Double value) {
		// Check if the value is null.
		if (value == null)
			// Add a Null.Double to the map.
			values.put(columnName, new Null.Double());
		// Set the non-null value.
		else
			values.put(columnName, value);
	}
	
	public Timestamp getTimestamp(String columnName) throws SQLException {
		return resultSet.getTimestamp(columnName);
	}
	
	public void setTimestamp(String columnName, Timestamp value) {
		// Check if the value is null.
		if (value == null)
			// Add a Null.Timestamp to the map.
			values.put(columnName, new Null.Timestamp());
		// Set the non-null value.
		else
			values.put(columnName, value);
	}

	public Clob getClob(String columnName) throws SQLException {
		return resultSet.getClob(columnName);
	}
	
	public void setClob(String columnName, Clob value) {
		// Check if the value is null.
		if (value == null)
			// Add a Null.Clob to the map.
			values.put(columnName, new Null.Clob());
		// Set the non-null value.
		else
			values.put(columnName, value);
	}
	
	/**
	 * Sets the given value at the given index of the prepared statement.
	 * Supports Integer, String, Double, Timestamp and Blob values.
	 * @param int index - the index at which to insert
	 * @param Object value - the value to insert
	 */
	private void setValueOnPrepSt(int index, Object value) throws SQLException {
		if (value instanceof Integer)
			prepSt.setInt(index, (Integer) value);
		else if (value instanceof String)
			prepSt.setString(index, (String) value);
		else if (value instanceof Double)
			prepSt.setDouble(index, (Double) value);
		else if (value instanceof Timestamp)
			prepSt.setTimestamp(index, (Timestamp) value);
		else if (value instanceof Clob)
			prepSt.setClob(index, (Clob) value);
		else if (value instanceof Null.Integer)
			prepSt.setNull(index, Types.INTEGER);
		else if (value instanceof Null.String)
			prepSt.setNull(index, Types.VARCHAR);
		else if (value instanceof Null.Double)
			prepSt.setNull(index, Types.DOUBLE);
		else if (value instanceof Null.Timestamp)
			prepSt.setNull(index, Types.TIMESTAMP);
		else if (value instanceof Null.Clob)
			prepSt.setNull(index, Types.CLOB);
		else {
			throw new UnsupportedOperationException("setValue() - unknown data type was encountered!");
		}
	}
	
	/**
	 * (Re)sets the {@link #values} Map {@link #primaryKeyMap}. 
	 */
	public void resetMaps() {
		values = new HashMap<>();
		primaryKeyMap = new HashMap<>();
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
