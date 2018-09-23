package jsouplesse.dataaccess;

import java.sql.SQLException;

/**
 * Super class for all DAO's.
 * Fields that are (part of) the primary key must be of {@link Integer} type.
 */
public abstract class SuperDao {

	/**
	 * The name of the table of which the subclass is a representation. 
	 * Sub classes should define this value in an initialization block.
	 */
	protected String tableName;
	
	/**
	 * The helper that composes and (for now) executes SQL statements. 
	 */
	protected SqlHelper sqlHelper;	
	
	
	public SuperDao(SqlHelper sqlHelper) {
		this.sqlHelper = sqlHelper;
	}
	
	/**
	 * Puts the instance variables to the values Map, which will then be used
	 * to prepare an SQL statement and set values on it.
	 */
	protected abstract void fillValues();
		
	/**
	 * Puts the fields of which the primary key for this table is comprised to
	 * the {@link #primaryKeyMap}.
	 */
	protected abstract void fillPrimaryKey();

	/**
	 * Moves the values from the result set to the instance variables. 
	 */
	protected abstract void fillFields() throws SQLException;
	
	// TODO: the current way of creating a PreparedStatement does not allow the PrepSt to be reused
	// for other instances of a sub class. An SqlHelper should compose the PrepSt and save it
	// as an instance variable (or pass it to an SqlExecutionHelper) so it can be reused by other 
	// instances of the DAO sub class.
	// This way, PreparedStatement could be filled, then call PreparedStatement.addBatch() until
	// all instances have been added to the batch, then call PreparedStatement.executeBatch() to
	// save all data in one go.
	
	/**
	 * Retrieves the row corresponding to the set primary key from the database.
	 * @throws SQLException 
	 */
	public void select() throws SQLException {
		// Put lock on SqlHelper.
		
		sqlHelper.setTableName(tableName);
		sqlHelper.resetMaps();
		
		fillPrimaryKey();

		try {
			sqlHelper.prepareSelect();
			
			sqlHelper.executeSelect();
			
			// Fill the instance variables with the results from the query.
			fillFields();
			
		} finally {
			sqlHelper.closeResultSetAndPreparedStatement();
		}
	}
	
	
	public void insert() throws SQLException {
		// Put lock on SqlHelper.

		// TODO: pass the table name to prepareXXX() - to determine whether statement can be reused.
		// The operations in these next two lines could then be handled by the SqlHelper. 
		sqlHelper.setTableName(tableName);
		sqlHelper.resetMaps();

		fillValues();
		
		fillPrimaryKey();
		
		try {
			sqlHelper.prepareInsert();
			
			sqlHelper.executeInsert();
		} finally {
			sqlHelper.closeResultSetAndPreparedStatement();
		}
	}
	
	
	public void update() throws SQLException {
		// Put lock on SqlHelper.
		
		sqlHelper.setTableName(tableName);
		sqlHelper.resetMaps();
		
		fillValues();
		
		fillPrimaryKey();

		try{ 
			sqlHelper.prepareUpdate();
		
			sqlHelper.executeUpdate();
		} finally {
			sqlHelper.closeResultSetAndPreparedStatement();
		}
	}
	
	
	public void delete() throws SQLException {
		// Put lock on SqlHelper.
		
		sqlHelper.setTableName(tableName);
		sqlHelper.resetMaps();
		
		fillPrimaryKey();

		try {
			sqlHelper.prepareDelete();
			
			sqlHelper.executeDelete();
		} finally {
			sqlHelper.closeResultSetAndPreparedStatement();
		}
	}
	
	public Integer determineNewId() throws SQLException {
		
		sqlHelper.setTableName(tableName);
		sqlHelper.resetMaps();
		fillPrimaryKey();
		return sqlHelper.determineNewId();
	}
}
