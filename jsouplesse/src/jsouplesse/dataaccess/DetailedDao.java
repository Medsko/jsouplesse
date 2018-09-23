package jsouplesse.dataaccess;

/**
 * Super for all DAO's that represent more complex entities, that have several
 * relations to other entities that of which certain fields will often have
 * to be retrieved to use the main entity in a meaningful way. To this end,
 * sub classes must also implement methods that 
 */
public abstract class DetailedDao extends SuperDao {

	
	public DetailedDao(SqlHelper sqlHelper) {
		super(sqlHelper);
	}

	/**
	 * As {@link #fillValues()}, fills the values Map used for preparing a
	 * statement. In this method, fields from other tables can be included.
	 */
	protected abstract void fillValuesWithDetails();
	
	/**
	 * As {@link #fillFields()}, moves the values from the result set to the
	 * instance variables. In this method, fields from other tables can be included.
	 */
	protected abstract void fillFieldsWithDetails();
	
	/**
	 * Adds the necessary joins to query the fields mentioned in 
	 * {@link #fillValuesWithDetails()} and {@link #fillFieldsWithDetails()}.
	 */
	protected abstract void addJoinsForRetrieveWithDetails(StringBuilder sqlBuilder);
	
	public void retrieveWithDetails() {
		// TODO: write concrete method to retrieve entity with details.
	}

}
