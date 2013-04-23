package RDBMS;
/**
 * Created with IntelliJ IDEA.
 * User: agavrilov
 * Date: 17.04.13
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */

import java.sql.*;
import java.util.*;

public class DatabaseWrapper {
	static String DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";
	static Connection MyConnection;
	static final List<String> tables = new ArrayList<>();
	static List<String> key = new ArrayList<>();
	static private boolean _isConnected;
	static public StringBuilder descriptionResult;

	/*
	 * Function that provides a connection to DB
	 */
	public static Connection createConnection(String username,
	                                          String password,
	                                          String url) throws ClassNotFoundException, SQLException {
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


		while ( DatabaseResultSet.next() ) {
			tables.add(DatabaseResultSet.getString(1));
		}
		statementForTables.close();
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

	public static Object[][] descriptionTable(String selectedTableName) throws SQLException {
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

		int numCol = descResSet.getMetaData().getColumnCount();
		while ( descResSet.next() ) {
			Object[] current = new Object[numCol + 1];
			current[0] = descResSet.getString(1);
			current[1] = descResSet.getString(2);
			current[2] = descResSet.getString(3);
			current[3] = new Boolean(false);
			data.add(current);
		}
		ascDescript.close();
		descResSet.close();
		return data.toArray(new Object[data.size()][numCol]);
	}

	/*
	 * Procedure that takes a description of selected table in list
	 */
	public static String getDescription(String selectedTableName) throws SQLException {
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
		ascDesc.close();
		descriptionResultSet.close();
		return descriptionResult.toString();
	}

	public static void getDataForMajorAndMinorKey(Set<String> majorSet,
	                                              Set<String> minorSet,
	                                              String selectedTableName) throws SQLException {
		StringBuilder resMajor = new StringBuilder();
		String resMinor;
		StringBuilder var2 = new StringBuilder();
		StringBuilder result = new StringBuilder();
		for ( String token : majorSet ) {
			resMajor.append(token).append("||'/'||");
		}
		/*for ( String token2 : minorSet ) {
			resMinor = resMajor.toString();
			var2.append("|| - /").append(resMinor).append(token2);
			//resMinor.append(token2).append("||'/'||");
		}*/

		result.append("'/'||").append(resMajor).append("'-/'||");
		//result.replace(result.lastIndexOf("||"), result.length(), "");
		//System.out.println(result);
		for ( String token2 : minorSet ) {
			PreparedStatement getKey = MyConnection.prepareStatement(" SELECT " + result + "'" + token2.toString() + "/'" + " AS KEY FROM " + selectedTableName.toString());
			ResultSet getkeyResultSet = getKey.executeQuery();
			while ( getkeyResultSet.next() ) {
				key.add(getkeyResultSet.getString(1));
			}
			getKey.close();
			getkeyResultSet.close();
		}
		Collections.sort(key, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				String s1 = o1;
				String s2 = o2;
				return s1.compareTo(s2);
			}
		});
		for ( String c : key ) {
			System.out.println(c.toString());
		}
	}
}
