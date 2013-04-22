package NoSQL;

import RDBMS.MainWindow;
import RDBMS.TableModel;
import oracle.kv.FaultException;
import oracle.kv.KVStore;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import static RDBMS.Util.MigPanel;

/**
 *
 * @author agavrilov
 */
public class NoSQLStorage extends JDialog
{
	//static JFrame frame = new JFrame();
  static JPanel noSqlPanel = new MigPanel();
  static final JPanel noSqlConnectionInfo = new MigPanel();
	static final JPanel noSqlInfo = new MigPanel();
	static final JLabel conectedNoSqlLbl = new JLabel("Status: ");
	static final JLabel hostLbl = new JLabel("Host: ");
	static final JLabel portLbl = new JLabel("Port: ");
	static final JLabel storeLbl = new JLabel("Store: ");
	static final JButton connectToNoSqlBut = new JButton("Connect to NoSQL Storage");
	static final JButton close = new JButton("Close");
	static final JButton startProcessOfConverting = new JButton("Start process");
	 JTextField portTxt = new JTextField();
	 JTextField hostTxt = new JTextField();
	 JTextField storeTxt = new JTextField();
	 JTextField connNoSqlStatusTxt = new JTextField("Not Connected");
	 ConnectionNoSQLStorage orastore;
	 KVStore myStore;
	static JTextPane progress = new JTextPane();
	static JScrollPane scroll = new JScrollPane();
	String selectedTableName = MainWindow.listOfTables.getSelectedValue().toString();

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
	  noSqlConnectionInfo.add(connectToNoSqlBut,"align right");

	  progress.setEditable(false);
	  scroll.getViewport().setView(progress);
	  noSqlInfo.add(scroll,"w 500, h 500, wrap 15");
	  noSqlInfo.add(close,"align right");
	  noSqlInfo.add(startProcessOfConverting,"align left");

	  noSqlPanel.add(noSqlConnectionInfo,"w 600,wrap 10");
	  noSqlPanel.add(noSqlInfo,"w 550");

	  //frame.setContentPane(noSqlPanel);
	  //frame.setVisible(true);
	 // frame.setSize(700,700);
  }
   //public static void main(String[] args)
   public NoSQLStorage()
  {
	  CreateForm();
	  setTitle("NoSQL Storage");
	  setContentPane(noSqlPanel);
	  setModal(true);
	  /*
	  Start of the storage; If need to start local storage, can be uncommented and added to form.
	   */
	  /*startStorage.addActionListener(new AbstractAction() {
		  @Override
		  public void actionPerformed(ActionEvent e) {
			  try{
				  if (proc != null){
					throw new Exception("The KVStore is already started. Connect to it.");
				  }
				  else
				  proc = Runtime.getRuntime().exec("java -jar NoSQL_Storage\\kv-ee-2.0.26\\kv-2.0.26\\lib\\kvstore.jar kvlite");
			  } catch ( Throwable ex ) {
				  JOptionPane.showMessageDialog(
								  noSqlPanel,
								  "An error accuses during start KVStore: " + ex.getMessage(),
								  "Error",
								  JOptionPane.ERROR_MESSAGE);
			  }
			  //System.out.println(proc.exitValue());
			  //if ( proc.isAlive() ) System.out.println("Process started!!!!");

				try{
					 Thread.sleep(1000);
						if (!proc.isAlive())
						{
							isStarted = false;
							throw new Exception("The KVStore doesn't started. Try again or sure that settings are correct.");
						}
					  else isStarted = true;
						}catch(Exception ex){
					JOptionPane.showMessageDialog(
									noSqlPanel,
									"An error accuses during start KVStore: " + ex.getMessage(),
									"Error",
									JOptionPane.ERROR_MESSAGE);
				}
		  }
	  });*/
	  connectToNoSqlBut.addActionListener(new AbstractAction() {
		  @Override
		  public void actionPerformed(ActionEvent e) {
			  try{
				  orastore = new ConnectionNoSQLStorage(store, host, port);
				  myStore = orastore.getStore();
			  }catch(FaultException ex){
				  JOptionPane.showMessageDialog(
								  noSqlPanel,
								  "An error accuses during connection to KVStore: " + ex.getMessage(),
								  "Error",
								  JOptionPane.ERROR_MESSAGE);
			  }catch (NullPointerException ne){
				  JOptionPane.showMessageDialog(
								  noSqlPanel,
								  "An error accuses during connection to KVStore: " + ne.getMessage(),
								  "Error",
								  JOptionPane.ERROR_MESSAGE);
			  }
			  if (ConnectionNoSQLStorage.isConenctedToStore())
			  {
				  connNoSqlStatusTxt.setText("Connected");
				  connNoSqlStatusTxt.setBackground(Color.GREEN);
				  progress.setText("Store opened");
			  }
		  }
	  });
	  close.addActionListener(new AbstractAction() {
		  @Override
		  public void actionPerformed( ActionEvent e ) {
			  try{
			  myStore.close();
				progress.setText(progress.getText() + "\n\nStore closed");
			  }catch (NullPointerException ex){
				  JOptionPane.showMessageDialog(
								  noSqlPanel,
								  "Nothing to close! At first connect.",
								  "Error",
								  JOptionPane.ERROR_MESSAGE);
			  }

			  //System.exit(0);
		  }
	  });
	  startProcessOfConverting.addActionListener(new AbstractAction() {
		  @Override
		  public void actionPerformed(ActionEvent e) {
			  try {
				  RDBMS.DatabaseWrapper.getDataForMajorAndMinorKey(TableModel.isAlreadySelectedMajor,
								                                           TableModel.isAlreadySelectedMinor,
								                                           selectedTableName);
			  } catch ( SQLException e1 ) {
				  e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			  }

		  }
	  });
  }
}
