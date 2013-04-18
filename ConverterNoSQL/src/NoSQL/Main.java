package NoSQL;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import NoSQL.Utils.MigPanel;
import oracle.kv.*;

/**
 *
 * @author agavrilov
 */
public class Main extends JDialog
{
  static JPanel noSqlPanel = new MigPanel("debug");
	static final JPanel noSqlConnectionInfo = new MigPanel();
	static final JLabel conectedNoSql = new JLabel();
	static final JButton connectToNoSqlBut = new JButton();
	static final JTextField connNoSqlStatus = new JTextField();
	static private Process proc;

  static String port = "5000";
  static String host = "localhost";
  static String store = "kvstore";

  static void CreateForm()
  {
	  conectedNoSql.setText("Status: ");
	  connNoSqlStatus.setText("NotConnected");
	  connNoSqlStatus.setEditable(false);
	  connNoSqlStatus.setBackground(Color.RED);
	  noSqlPanel.setBorder(new TitledBorder("Connection to NoSQL Storage"));

	  noSqlConnectionInfo.add(conectedNoSql);
	  noSqlConnectionInfo.add(connNoSqlStatus,"wrap 30");
	  noSqlConnectionInfo.add(connectToNoSqlBut,"align right");

	  noSqlPanel.add(noSqlConnectionInfo);
  }
   public Main()
  {

	  CreateForm();
	  setTitle("NoSQL Storage");
	  setContentPane(noSqlPanel);
	  setLocation(200,300);
	  setModal(true);
    //ConnectionNoSQLStorage orastore = new ConnectionNoSQLStorage(store, host, port);
    //KVStore myStore = orastore.getStore();

    //myStore.close();
    System.out.println("Store closed");
  }
}
