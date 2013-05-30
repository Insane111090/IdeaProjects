package RDBMS;
/**
 * Created with IntelliJ IDEA.
 * User: agavrilov
 * Date: 17.04.13
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */

import NoSQL.NoSQLStorage;
import NoSQL.Support;
import oracle.kv.Durability;
import oracle.kv.KVStore;
import oracle.kv.Key;
import oracle.kv.Value;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DatabaseWrapper implements Runnable {

	static String DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";
	static Connection MyConnection;
	static final List<String> tables = new ArrayList<>();
	static private boolean _isConnected;
	static public StringBuilder descriptionResult;
	static private Thread[] pool;
	static private LinkedBlockingQueue<Util.KV> dataToSend;
	static private boolean lobFlag;

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


		_isConnected = MyConnection != null;
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
	public static List<String> getTableList( String username ) throws SQLException {
		PreparedStatement statementForTables =
						MyConnection.prepareStatement("SELECT table_name FROM all_tables "
										                              + "WHERE NOT regexp_like(tablespace_name,'SYS.+') "
										                              + "AND owner=upper(?) ORDER BY table_name");
		statementForTables.setString(1,
		                             username);
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

		ascDescript.setString(1,
		                      selectedTableName);
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
		ascDesc.setString(1,
		                  selectedTableName);
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
/*	public static Boolean isLob(Set<String> minorSet,
	                            Set<String> valueSet,
	                            String selectedTableName) throws SQLException
	{ boolean ret = false;
		PreparedStatement defineIsLob = MyConnection.prepareStatement("select column_name from user_tab_columns " +
						"where table_name = '" + selectedTableName + "'"+
						" and (data_type like 'CLOB' or data_type like 'BLOB')");
		ResultSet getLobObj = defineIsLob.executeQuery();
		while(getLobObj.next())
		{
			for (String minor:minorSet)
			{ String str = getLobObj.getString(1);
				if (minor.equals(str))
					ret = true;
				continue;
			}
			for (String value: valueSet)
			{ String str = getLobObj.getString(1);
				if (value.equals(str))
					ret = true;
				continue;
			}
		}
		return ret;
	}*/

	public static Set<String> filterLobs( Set<String> minors,
	                                      Set<String> values,
	                                      String tableName ) throws SQLException {
		PreparedStatement lobColumnsQuery = MyConnection.prepareStatement("SELECT column_name FROM user_tab_columns " +
						                                                                  "WHERE table_name = ? AND (data_type LIKE 'CLOB' OR data_type LIKE 'BLOB')");
		lobColumnsQuery.setString(1,
		                          tableName);
		ResultSet lobColumns = lobColumnsQuery.executeQuery();
		Set<String> result = new HashSet<>();
		while ( lobColumns.next() ) {
			String columnName = lobColumns.getString(1);
			if ( minors.contains(columnName) || values.contains(columnName) ) {
				result.add(columnName);
			}
		}
		return result;
	}

	//Write data in storage
	public static void getDataForMajorAndMinorKey( Set<String> majorSet,
	                                               Set<String> minorSet,
	                                               Set<String> valueSet,
	                                               String selectedTableName ) throws SQLException, NullPointerException {
		StringBuilder resMajor = new StringBuilder();
		StringBuilder resMinor = new StringBuilder();
		StringBuilder resValues = new StringBuilder();
		StringBuilder result = new StringBuilder();
		Durability myDurability = new Durability(Durability.SyncPolicy.NO_SYNC,
		                                         Durability.SyncPolicy.NO_SYNC,
		                                         Durability.ReplicaAckPolicy.SIMPLE_MAJORITY);
		int cores = 5; //Runtime.getRuntime().availableProcessors();    //Count of threads
		dataToSend = new LinkedBlockingQueue<>(50000);//Size of queue

		for ( String major : majorSet ) {
			resMajor.append(major).append("||'/'||");
		}
		result.append(resMajor).append("'-/'||");

		for ( String minor : minorSet ) {
			resMinor.delete(0,
			                resMinor.length());
			resMinor.append(minor);
			if ( PartsOfKeyforNoSQL.isSimple ) {
				Set<String> lobs = filterLobs(minorSet,
				                              valueSet,
				                              selectedTableName);
				if ( lobs.contains(minor) )
					lobFlag = true;
				PreparedStatement getKey;
				if ( lobFlag ) {
					getKey = MyConnection.prepareStatement("SELECT " + result + "'" + minor + "/:'," + resMinor +
									                                       " FROM " + selectedTableName + " where " + resMinor + " is not null");
				} else {
					getKey = MyConnection.prepareStatement("SELECT " + result + "'" + minor + "/:' ||" + resMinor +
									                                       " AS KEY FROM " + selectedTableName);
				}
				;

				getKey.setFetchSize(1000);
				ResultSet getkeyResultSet = getKey.executeQuery();
				int counterSimple = 0;
				pool = new Thread[cores];
				for ( int i = 0; i < cores; i++ ) {
					pool[i] = new Thread(new Pusher(NoSQLStorage.store,
					                                NoSQLStorage.host,
					                                NoSQLStorage.port));
				}
				for ( Thread pusher : pool ) {
					pusher.start();
				}
				while ( getkeyResultSet.next() ) {
					Key myKeySimple = Support.ParseKey.ParseKey(selectedTableName + "/" + getkeyResultSet.getString(1),
					                                            lobFlag);
					Value mySimpleValue;
					if ( lobFlag ) {
						InputStream simpleValueStream = new ByteArrayInputStream(getkeyResultSet.getBytes(2));

						dataToSend.add(new Util.KV(myKeySimple,
						                           simpleValueStream,
						                           myDurability,
						                           20,
						                           TimeUnit.MILLISECONDS));
					} else {
						mySimpleValue = Support.ParseKey.ParseValue(getkeyResultSet.getString(1),
						                                            lobFlag);
						dataToSend.add(new Util.KV(myKeySimple,
						                           mySimpleValue));
					}
					counterSimple += 1;
					System.out.println("Rows converted " + counterSimple);
				}
				NoSQLStorage.progress.append("\nCount of converted data is " + counterSimple + " rows\n");
				getKey.close();
				getkeyResultSet.close();
				for ( Thread pusher : pool ) {
					pusher.interrupt();
				}
			} else if ( PartsOfKeyforNoSQL.isComplex ) {
				for ( String value : valueSet ) {
					resValues.append("\"").append(value).append("\":").append(" \"'||").append(value).append("||'\",\n");
				}
				PreparedStatement getComplexMinorValue = MyConnection.prepareStatement("SELECT regexp_replace(" + result + "'" + minor + "/:' ||" +
								                                                                       "'{" + resValues + "',',$','}') FROM " + selectedTableName);
				getComplexMinorValue.setFetchSize(1000);
				ResultSet getComplexKeyResultSet = getComplexMinorValue.executeQuery();
				int counterComplex = 0;
				pool = new Thread[cores];
				for ( int i = 0; i < cores; i++ ) {
					pool[i] = new Thread(new Pusher(NoSQLStorage.store,
					                                NoSQLStorage.host,
					                                NoSQLStorage.port));
				}
				for ( Thread pusher : pool ) {
					pusher.start();
				}
				while ( getComplexKeyResultSet.next() ) {
					lobFlag = false;
					Key myKeyComplex = Support.ParseKey.ParseKey(selectedTableName + "/" + getComplexKeyResultSet.getString(1),
					                                             lobFlag);
					Value myValueComplex = Support.ParseKey.ParseValue(getComplexKeyResultSet.getString(1),
					                                                   lobFlag);
					InputStream lobStream = new ByteArrayInputStream(myValueComplex.getValue());
					dataToSend.add(new Util.KV(myKeyComplex,
					                           myValueComplex));
					/*NoSQLStorage.myStore.put(myKeyComplex,
							                         myValueComplex);*/
					//, null, myDurability,30,TimeUnit.MILLISECONDS);

					// TODO Need .lob key
					/*try {
						NoSQLStorage.myStore.putLOB(myKeyComplex,lobStream
										,myDurability.COMMIT_WRITE_NO_SYNC,30,TimeUnit.MILLISECONDS);
					} catch ( IOException e ) {
						System.out.println(e.getMessage());
					}*/
					counterComplex += 1;
					System.out.println("Rows converted " + counterComplex);
				}
				NoSQLStorage.progress.append("\nCount of converted data is " + counterComplex + " rows\n");
				for ( Thread pusher : pool ) {
					pusher.interrupt();
				}
			}
		}
	}

	private static class Pusher implements Runnable {

		List<Util.KV> localBuffer = new ArrayList<>();
		final KVStore connection;

		public Pusher( String name, String host, int port ) {
			connection = Support.makeNoSQLConnection(name,
			                                         host,
			                                         port);
		}

		@Override
		public void run() {
			while ( true ) {
				localBuffer.clear();
				dataToSend.drainTo(localBuffer,
				                   2000);
				if ( ! lobFlag ) {
					for ( Util.KV kv : localBuffer ) {
						connection.put(kv.k,
						               kv.v);
					}
				} else {
					for ( Util.KV kv : localBuffer ) {
						try {
							connection.putLOB(kv.k,
							                  kv.s,
							                  kv.d,
							                  kv.l,
							                  kv.tu);
						} catch ( IOException e ) {
							System.out.println("Error: " + e.getMessage());
						}
					}
				}
			}

		}
	}

	//Write meta information for converting table in storage (for External Tables)
	public static void writeMetaDataToStorage( Set<String> majorSet,
	                                           Set<String> minorSet,
	                                           String selectedTableName ) {
		StringBuilder metaInfo = new StringBuilder();
		StringBuilder valuesForMeta = new StringBuilder();
		boolean flag = false;
		metaInfo.append(selectedTableName).append("/-/").append("MetaData/:");

		valuesForMeta.append("{\"").append("Major key\":\"");
		for ( String valuesMaj : majorSet ) {
			valuesForMeta.append(valuesMaj).append(";");
		}
		valuesForMeta.replace(valuesForMeta.length() - 1,
		                      valuesForMeta.length(),
		                      "\",\"");
		valuesForMeta.append("Minor key\":\"");
		for ( String valuesMin : minorSet ) {
			valuesForMeta.append(valuesMin).append(";");
		}
		valuesForMeta.replace(valuesForMeta.length() - 1,
		                      valuesForMeta.length(),
		                      "\"}");
		Key metaKey = Support.ParseKey.ParseKey(metaInfo.toString(),
		                                        flag);
		Value metaValue = Support.ParseKey.ParseValue("Meta:" + valuesForMeta.toString(),
		                                              flag);

		NoSQLStorage.myStore.put(metaKey,
		                         metaValue);
		NoSQLStorage.progress.append("Meta information is stored on:\nKey: " + metaKey.getMajorPath() + " " + metaKey.getMinorPath() + "\nand values is \n" + new String(metaValue.getValue()) + "\n");
		System.out.println("Meta information is stored on:" + metaKey.getMajorPath() + " " + metaKey.getMinorPath()
						                   + " and values is " + new String(metaValue.getValue()));
	}


	@Override
	public void run() {
		double before = System.currentTimeMillis();
		try {
			RDBMS.DatabaseWrapper.writeMetaDataToStorage(TableModel.isAlreadySelectedMajor,
			                                             TableModel.isAlreadySelectedMinor,
			                                             MainWindow.listOfTables.getSelectedValue().toString());

			RDBMS.DatabaseWrapper.getDataForMajorAndMinorKey(TableModel.isAlreadySelectedMajor,
			                                                 TableModel.isAlreadySelectedMinor,
			                                                 TableModel.isAlredySelectedValues,
			                                                 MainWindow.listOfTables.getSelectedValue().toString());
		} catch ( SQLException e1 ) {

		} catch ( NullPointerException ne ) {
			NoSQLStorage.progress.setText("\n\nAn error occurred during the convertation." + ne.getMessage() + " \nYour value is NULL");
		} /*catch ( Throwable ee ) {
			NoSQLStorage.progress.setText("\n\nAn error occurred during the convertation." + ee.getMessage());
		}*/
		double after = System.currentTimeMillis();

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					NoSQLStorage.startProcessOfConverting.setEnabled(true);
				}
			});
		} catch ( InterruptedException e ) {

		} catch ( InvocationTargetException e ) {

		}
		double diff = after - before;
		NoSQLStorage.progress.append("\n\nProgram executed for " + diff / 1000 + " sec(" + diff / 1000 / 60 + " min)\n");
		System.out.println("Program executed for " + diff / 1000 + " sec (" + diff / 1000 / 60 + " min)");
	}
}
