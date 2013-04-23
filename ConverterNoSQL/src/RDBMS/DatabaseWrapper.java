package RDBMS;
/**
 * Created with IntelliJ IDEA.
 * User: agavrilov
 * Date: 17.04.13
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DatabaseWrapper {
	static String DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";
	static Connection MyConnection;
	static final List<String> tables = new ArrayList<>();
	static private boolean _isConnected;
	static public StringBuilder descriptionResult;

	/*
	 * Function that provides a connection to DB
	 */
	public static Connection createConnection( String username,
	                                           String password,
	                                           String url ) throws ClassNotFoundException, SQLException {
		Class.forName(DRIVER_NAME);
		MyConnection = DriverManager.getConnection(url,
						username,
						password);

		_isConnected = MyConnection != null ? true : false;
		return MyConnection;
	}

	/*
	 * Returns true or false, if connected of not
	 */
	public static Boolean isConnected() {
		return _isConnected;
	}

	/*
	 * Function for getting list of tables of current scheme in Database
	 */
	public static List<String> getTableList() throws SQLException {
		PreparedStatement statementForTables =
						MyConnection.prepareStatement("SELECT table_name FROM all_tables "
										+ "WHERE NOT regexp_like(tablespace_name,'SYS.+') "
										+ "AND owner=upper('andgavr')");
		ResultSet DatabaseResultSet = statementForTables.executeQuery();
		statementForTables.close();

		while ( DatabaseResultSet.next() ) {
			tables.add(DatabaseResultSet.getString(1));
		}
		DatabaseResultSet.close();
		return tables;
	}

	/*
	 * Delete values from ArrayList of tables
	 */
	public static void clearArrayList() {
		tables.clear();
	}

  /*
   * Procedure that takes a description of selected table in JTable
   */

	public static Object[][] descriptionTable( String selectedTableName ) throws SQLException {
		List<Object[]> data = new LinkedList<>();
		PreparedStatement ascDescript = MyConnection.prepareStatement(
						"SELECT column_name, CASE WHEN data_precision IS NOT null THEN "
										+ "data_type||'('||data_precision||','||data_scale||')' ELSE "
										+ "CASE WHEN data_precision IS null AND char_used IS null THEN "
										+ "data_type ELSE  CASE WHEN char_used = 'B' THEN "
										+ "data_type||'('||char_length||' BYTE)'  ELSE "
										+ "data_type||'('||char_length||' CHAR)' END END END data_type,"
										+ "CASE WHEN nullable = 'N' THEN 'NOT NULL' ELSE '--' END nul "
										+ "FROM user_tab_cols WHERE table_name = upper(?)");

		ascDescript.setString(1, selectedTableName);
		ResultSet descResSet = ascDescript.executeQuery();
		ascDescript.close();
		int numCol = descResSet.getMetaData().getColumnCount();
		while ( descResSet.next() ) {
			Object[] current = new Object[numCol + 1];
			current[0] = descResSet.getString(1);
			current[1] = descResSet.getString(2);
			current[2] = descResSet.getString(3);
			current[3] = new Boolean(false);
			data.add(current);
		}
		descResSet.close();
		return data.toArray(new Object[data.size()][numCol]);
	}

	/*
	 * Procedure that takes a description of selected table in list
	 */
	public static String getDescription( String selectedTableName ) throws SQLException {
		PreparedStatement ascDesc = MyConnection.prepareStatement(
						"SELECT column_name, CASE WHEN data_precision IS NOT null THEN "
										+ "data_type||'('||data_precision||','||data_scale||')' ELSE "
										+ "CASE WHEN data_precision IS null AND char_used IS null THEN "
										+ "data_type ELSE  CASE WHEN char_used = 'B' THEN "
										+ "data_type||'('||char_length||' BYTE)'  ELSE "
										+ "data_type||'('||char_length||' CHAR)' END END END data_type,"
										+ "CASE WHEN nullable = 'N' THEN 'NOT NULL' ELSE '--' END nul "
										+ "FROM user_tab_cols WHERE table_name = upper(?)");
		ascDesc.setString(1, selectedTableName);
		ResultSet descriptionResultSet = ascDesc.executeQuery();
		ascDesc.close();
		descriptionResult = new StringBuilder("");
		while ( descriptionResultSet.next() ) {
			descriptionResult.append("<tr><td>")
							.append(descriptionResultSet.getString(1))
							.append("</td> <td>")
							.append(descriptionResultSet.getString(2))
							.append("</td> <td>")
							.append(descriptionResultSet.getString(3))
							.append("</td>");
		}
		descriptionResultSet.close();
		return descriptionResult.toString();
	}

	public static void getDataForMajorAndMinorKey( Set<String> majorSet,
	                                               Set<String> minorSet,
	                                               String selectedTableName ) throws SQLException {
		StringBuilder resMajor = new StringBuilder();
		StringBuilder resMinor = new StringBuilder();
		StringBuilder result = new StringBuilder();
		for ( String token : majorSet )
		{
			resMajor.append(token).append("||'/'||");
		}
		for ( String token2 : minorSet )
		{
			resMinor.append(token2).append("||'/'||");
		}
		result.append(resMajor).append("'-/'||").append(resMinor);
		result.replace(result.lastIndexOf("||"), result.length(), "");
		//System.out.println(result);
		PreparedStatement getKey = MyConnection.prepareStatement(" SELECT "+result+" AS key FROM " + selectedTableName.toString());
		ResultSet getkeyResultSet = getKey.executeQuery();
		getKey.close();
		while ( getkeyResultSet.next() )
		{
			System.out.println(getkeyResultSet.getString(1));
		}
		getkeyResultSet.close();
	}
}
