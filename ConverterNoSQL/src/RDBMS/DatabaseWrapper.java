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

public class DatabaseWrapper
{
    static String DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";
    static Connection MyConnection;
    static final List<String> tables = new ArrayList<>();
    static private boolean _isConnected;
    static public StringBuilder descriptionResult;

    /*
     * Function that provides a connection to DB
     */
    public static Connection createConnection(String username,
                                              String password,
                                              String url) throws ClassNotFoundException, SQLException
    {
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
    public static Boolean isConnected()
    {
        return _isConnected;
    }

    /*
     * Function for getting list of tables of current scheme in Database
     */
    public static List<String> getTableList() throws SQLException
    {
        PreparedStatement statementForTables =
                MyConnection.prepareStatement("Select table_name from all_tables "
                        + "where not regexp_like(tablespace_name,'SYS.+') "
                        + "and owner=upper('olgbel')");
        ResultSet DatabaseResultSet = statementForTables.executeQuery();

        while (DatabaseResultSet.next()) {
            tables.add(DatabaseResultSet.getString(1));
        }
        return tables;
    }

    /*
     * Delete values from ArrayList of tables
     */
    public static void clearArrayList()
    {
        tables.clear();
    }

  /*
   * Procedure that takes a description of selected table in JTable
   */

    public static Object[][] descriptionTable(String selectedTableName) throws SQLException
    {
        List<Object[]> data = new LinkedList<>();
        PreparedStatement ascDescript = MyConnection.prepareStatement(
                "select column_name, case when data_precision is not null then "
                        + "data_type||'('||data_precision||','||data_scale||')' else "
                        + "case when data_precision is null and char_used is null then "
                        + "data_type else  case when char_used = 'B' then "
                        + "data_type||'('||char_length||' BYTE)'  else "
                        + "data_type||'('||char_length||' CHAR)' end end end data_type,"
                        + "case when nullable = 'N' then 'NOT NULL' else '--' end nul "
                        + "from user_tab_cols where table_name = upper(?)");

        ascDescript.setString(1, selectedTableName);
        ResultSet descResSet = ascDescript.executeQuery();
        int numCol = descResSet.getMetaData().getColumnCount();
        while (descResSet.next()) {
            Object[] current = new Object[numCol + 1];
            current[0] = descResSet.getString(1);
            current[1] = descResSet.getString(2);
            current[2] = descResSet.getString(3);
            current[3] = new Boolean(false);
            data.add(current);
        }
        return data.toArray(new Object[data.size()][numCol]);
    }

    /*
     * Procedure that takes a description of selected table in list
     */
    public static String getDescription(String selectedTableName) throws SQLException
    {
        PreparedStatement ascDesc = MyConnection.prepareStatement(
                "select column_name, case when data_precision is not null then "
                        + "data_type||'('||data_precision||','||data_scale||')' else "
                        + "case when data_precision is null and char_used is null then "
                        + "data_type else  case when char_used = 'B' then "
                        + "data_type||'('||char_length||' BYTE)'  else "
                        + "data_type||'('||char_length||' CHAR)' end end end data_type,"
                        + "case when nullable = 'N' then 'NOT NULL' else '--' end nul "
                        + "from user_tab_cols where table_name = upper(?)");
        ascDesc.setString(1, selectedTableName);

        ResultSet descriptionResultSet = ascDesc.executeQuery();
        descriptionResult = new StringBuilder("");
        while (descriptionResultSet.next()) {
            descriptionResult.append("<tr><td>")
                    .append(descriptionResultSet.getString(1))
                    .append("</td> <td>")
                    .append(descriptionResultSet.getString(2))
                    .append("</td> <td>")
                    .append(descriptionResultSet.getString(3))
                    .append("</td>");
        }
        return descriptionResult.toString();
    }
}
