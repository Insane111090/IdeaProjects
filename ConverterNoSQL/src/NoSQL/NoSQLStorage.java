package NoSQL;

import oracle.kv.KVStore;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import static RDBMS.Util.MigPanel;

/**
 *
 * @author agavrilov
 */
public class NoSQLStorage extends JDialog
{
  JPanel noSqlPanel = new MigPanel();
  final JPanel noSqlConnectionInfo = new MigPanel();
	final JPanel noSqlInfo = new MigPanel();
	final JLabel conectedNoSqlLbl = new JLabel("Status: ");
	final JLabel hostLbl = new JLabel("Host: ");
	final JLabel portLbl = new JLabel("Port: ");
	final JLabel storeLbl = new JLabel("Store: ");
	final JButton startStorage = new JButton("Start NoSQL Storage");
	final JButton connectToNoSqlBut = new JButton("Connect to NoSQL Storage");
	JTextField portTxt = new JFormattedTextField();
	JTextField hostTxt = new JFormattedTextField();
	JTextField storeTxt = new JFormattedTextField();
	JTextField connNoSqlStatusTxt = new JFormattedTextField("NotConnected");
	private Process proc;

  static String port = "5000";
  static String host = "localhost";
  static String store = "kvstore";

   void CreateForm()
  {
	  noSqlPanel.setBorder(new TitledBorder("Connection to NoSQL Storage"));
	  noSqlInfo.setBorder(new TitledBorder("Data status"));
	  connNoSqlStatusTxt.setEditable(false);
	  connNoSqlStatusTxt.setBackground(Color.RED);

	  noSqlConnectionInfo.add(portLbl);
	  noSqlConnectionInfo.add(portTxt,"w 150, wrap");
	  noSqlConnectionInfo.add(hostLbl);
	  noSqlConnectionInfo.add(hostTxt,"w 150, wrap");
	  noSqlConnectionInfo.add(storeLbl);
	  noSqlConnectionInfo.add(storeTxt,"w 150, wrap 15");
	  noSqlConnectionInfo.add(conectedNoSqlLbl);
	  noSqlConnectionInfo.add(connNoSqlStatusTxt);
	  noSqlConnectionInfo.add(startStorage,"align right");
	  noSqlConnectionInfo.add(connectToNoSqlBut,"align right");

	  noSqlPanel.add(noSqlConnectionInfo,"w 600,wrap 10");
	  noSqlPanel.add(noSqlInfo,"w 550");
  }
   public NoSQLStorage()
  {
	  CreateForm();
	  setTitle("NoSQL Storage");
	  setContentPane(noSqlPanel);
	  setModal(true);
	  /*
	  Start of the storage
	   */
	  startStorage.addActionListener(new AbstractAction() {
		  @Override
		  public void actionPerformed(ActionEvent e) {
			  Runtime r = Runtime.getRuntime();
			  //proc = null;
			  try{
				  proc = Runtime.getRuntime().exec("java -jar NoSQL_Storage\\kv-ee-2.0.26\\kv-2.0.26\\lib\\kvstore.jar kvl");
				  System.out.println(proc.getErrorStream().toString());


			  } catch ( Throwable ex ) {
				  JOptionPane.showMessageDialog(
								  noSqlPanel,
								  "An error accuses during start KVStore: " + ex.getMessage(),
								  "Error",
								  JOptionPane.ERROR_MESSAGE);
			  }
			  //System.out.println(proc.exitValue());
			  //if ( proc.isAlive() ) System.out.println("Process started!!!!");

		  }
	  });

	  connectToNoSqlBut.addActionListener(new AbstractAction() {
		  @Override
		  public void actionPerformed(ActionEvent e) {
			  //ConnectionNoSQLStorage orastore = new ConnectionNoSQLStorage(store, host, port);
			  //KVStore myStore = orastore.getStore();
		  }
	  });


    //myStore.close();
    System.out.println("Store closed");
  }
}
