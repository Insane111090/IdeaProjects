package NoSQL;

import oracle.kv.KVStore;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import static RDBMS.Util.MigPanel;
import static java.lang.System.*;

/**
 *
 * @author agavrilov
 */
public class NoSQLStorage
{
	static JFrame frame = new JFrame();
  static JPanel noSqlPanel = new MigPanel();
  static final JPanel noSqlConnectionInfo = new MigPanel();
	static final JPanel noSqlInfo = new MigPanel();
	static final JLabel conectedNoSqlLbl = new JLabel("Status: ");
	static final JLabel hostLbl = new JLabel("Host: ");
	static final JLabel portLbl = new JLabel("Port: ");
	static final JLabel storeLbl = new JLabel("Store: ");
	static final JButton startStorage = new JButton("Start NoSQL Storage");
	static final JButton connectToNoSqlBut = new JButton("Connect to NoSQL Storage");
	static JTextField portTxt = new JFormattedTextField();
	static JTextField hostTxt = new JFormattedTextField();
	static JTextField storeTxt = new JFormattedTextField();
	static JTextField connNoSqlStatusTxt = new JFormattedTextField("NotConnected");
	private static Process proc;

  static String port = "5000";
  static String host = "localhost";
  static String store = "kvstore";

   static void CreateForm()
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

	  frame.setContentPane(noSqlPanel);
	  frame.setVisible(true);
	  frame.setSize(700,700);
  }
   public static void main(String[] args)
  {
	  CreateForm();
	  //setTitle("NoSQL Storage");
	  //setContentPane(noSqlPanel);
	  //setModal(true);
	  /*
	  Start of the storage
	   */
	  startStorage.addActionListener(new AbstractAction() {
		  @Override
		  public void actionPerformed(ActionEvent e) {
			  //Runtime r = Runtime.getRuntime();
			  //proc = null;
			  try{
				  proc = Runtime.getRuntime().exec("java -jar NoSQL_Storage\\kv-ee-2.0.26\\kv-2.0.26\\lib\\kvstore.jar vlite");
				  out.println(proc.getErrorStream().toString());

			  } catch ( Throwable ex ) {
				  JOptionPane.showMessageDialog(
								  noSqlPanel,
								  "An error accuses during start KVStore: " + ex.getMessage(),
								  "Error",
								  JOptionPane.ERROR_MESSAGE);
			  }
						while(true){
							try{
							Thread.sleep(1000);
							}catch(Exception ex){}
							out.println(proc.isAlive());
						}

			  //System.out.println(proc.exitValue());
			  //if ( proc.isAlive() ) System.out.println("Process started!!!!");
		  }
	  });

	  connectToNoSqlBut.addActionListener(new AbstractAction() {
		  @Override
		  public void actionPerformed(ActionEvent e) {
			  ConnectionNoSQLStorage orastore = new ConnectionNoSQLStorage(store, host, port);
			  KVStore myStore = orastore.getStore();
		  }
	  });


    //myStore.close();
    out.println("Store closed");
  }
}
