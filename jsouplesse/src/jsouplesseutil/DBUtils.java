package jsouplesseutil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtils {

	public static void closeQuietly(ResultSet rs, Statement st, Connection con) {
		closeResultSetQuietly(rs);
		closeStatementQuietly(st);
		closeConnectionQuietly(con);
	}
	
	public static void closeResultSetQuietly(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException sqlex) {
			// Quiet, so do nothing
		}
	}
	
	public static void closeStatementQuietly(Statement st) {
		try {
			if (st != null) {
				st.close();
			}
		} catch (SQLException sqlex) {
			// Quiet, so do nothing
		}
	}
	
	public static void closeConnectionQuietly(Connection con) {
		try {
			if (con != null) {
				con.close();
			}
		} catch (SQLException sqlex) {
			// Quiet, so do nothing
		}
	}
	
}
