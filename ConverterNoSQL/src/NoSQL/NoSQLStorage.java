package NoSQL;

import RDBMS.DatabaseWrapper;
import oracle.kv.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.concurrent.TimeUnit;

import oracle.kv.lob.InputStreamVersion;

import static RDBMS.Util.MigPanel;

/**
 * @author agavrilov
 */
public class NoSQLStorage extends JDialog {
	public JPanel noSqlPanel = new MigPanel();
	final JPanel noSqlConnectionInfo = new MigPanel();
	final JPanel noSqlInfo = new MigPanel();
	final JLabel conectedNoSqlLbl = new JLabel("Status: ");
	final JLabel hostLbl = new JLabel("Host: ");
	final JLabel portLbl = new JLabel("Port: ");
	final JLabel storeLbl = new JLabel("Store: ");
	final JButton connectToNoSqlBut = new JButton("Connect to NoSQL Storage");
	final JButton disconnectAndClose = new JButton("Disconnect and close");
	final JButton disconnectNoSQL = new JButton("Disconnect from storage");
	public static final JButton startProcessOfConverting = new JButton("Start process");
	JTextField portTxt = new JTextField();
	JTextField hostTxt = new JTextField();
	JTextField storeTxt = new JTextField();
	JTextField connNoSqlStatusTxt = new JTextField("Not Connected");
	public static KVStore myStore;
	public static JTextArea progress = new JTextArea();
	JScrollPane scroll = new JScrollPane();


	public static int port = 5000;
	public static String host = "localhost";
	public static String store = "MyStore";

	void CreateForm() {
		noSqlConnectionInfo.setBorder(new TitledBorder("Connection to NoSQL Storage"));
		connNoSqlStatusTxt.setEditable(false);
		connNoSqlStatusTxt.setBackground(Color.RED);

		noSqlConnectionInfo.add(portLbl,
		                        "split, gapleft 20");
		noSqlConnectionInfo.add(portTxt,
		                        "w 150, wrap, split");
		noSqlConnectionInfo.add(hostLbl,
		                        "split,gapleft 18");
		noSqlConnectionInfo.add(hostTxt,
		                        "w 150, wrap, split");
		noSqlConnectionInfo.add(storeLbl,
		                        "split, gapleft 15");
		noSqlConnectionInfo.add(storeTxt,
		                        "w 150, wrap 15, split");
		noSqlConnectionInfo.add(conectedNoSqlLbl,
		                        "split");
		noSqlConnectionInfo.add(connNoSqlStatusTxt,
		                        "wrap 10");
		noSqlConnectionInfo.add(connectToNoSqlBut,
		                        "gapleft 180");
		noSqlConnectionInfo.add(disconnectNoSQL,
		                        "align right");

		noSqlInfo.setBorder(new TitledBorder("Data status"));
		progress.setEditable(false);
		scroll.getViewport().setView(progress);
		noSqlInfo.add(scroll,
		              "w 600,h 300, wrap 15");
		noSqlInfo.add(startProcessOfConverting,
		              "align right");
		noSqlInfo.add(disconnectAndClose,
		              "align right");
		noSqlPanel.add(noSqlConnectionInfo,
		               "wrap");
		noSqlPanel.add(noSqlInfo);
	}

	public NoSQLStorage() {
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
			public void actionPerformed( ActionEvent e ) {
				try {
					myStore = Support.makeNoSQLConnection(store,
					                                      host,
					                                      port);
				} catch ( FaultException ex ) {
					JOptionPane.showMessageDialog(
									noSqlPanel,
									"An error accuses during connection to KVStore: " + ex.getMessage(),
									"Error",
									JOptionPane.ERROR_MESSAGE);
				} catch ( NullPointerException ne ) {
					JOptionPane.showMessageDialog(
									noSqlPanel,
									"An error accuses during connection to KVStore: " + ne.getMessage(),
									"Error",
									JOptionPane.ERROR_MESSAGE);
				}
				if ( myStore != null ) {
					connNoSqlStatusTxt.setText("Connected");
					connNoSqlStatusTxt.setBackground(Color.GREEN);
					progress.setText("Store opened");
				}
			}
		});
		disconnectNoSQL.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				try {
					myStore.close();
					progress.invalidate();
					progress.append("\n\nStore closed");//);append();
				} catch ( NullPointerException ex ) {
					JOptionPane.showMessageDialog(
									noSqlPanel,
									"You are not connected. Nothing to disconnect.",
									"Error",
									JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		disconnectAndClose.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				try {
					myStore.close();
					progress.append("\n\nStore closed");
				} catch ( NullPointerException ex ) {
//					JOptionPane.showMessageDialog(
//									noSqlPanel,
//									"Nothing to close! At first connect.",
//									"Error",
//									JOptionPane.ERROR_MESSAGE);
				}
				dispose();
			}
		});
		startProcessOfConverting.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				progress.setText("");
				startProcessOfConverting.setEnabled(false);
				Thread importer = new Thread(new DatabaseWrapper());
				importer.start();

			/*Key test = Support.ParseKey.ParseKey("MESSAGES/2364/-/MSG_TEXT.lob/",false);
				InputStreamVersion stream = myStore.getLOB(test,Consistency.NONE_REQUIRED,5,
				                                           TimeUnit.SECONDS);
				BufferedReader br;
				String line;
				InputStreamReader isr;
				isr = new InputStreamReader(stream.getInputStream());
				System.out.println(isr.getEncoding());
				br = new BufferedReader(isr);
				try {
					while ((line = br.readLine()) != null)
					{
						byte[] b  = line.getBytes();
						String line2 =  new String(b);
						System.out.println(line2);
					}
				} catch ( IOException e2 ) {
					System.out.println(e2.getMessage());  //To change body of catch statement use File | Settings | File Templates.
				}*/

				/*Key test = Support.ParseKey.ParseKey("CHILD/1194/Грудкин Антон Борисович/-/PersonalData/",false);
				ValueVersion vv = myStore.get(test);
				Value v = vv.getValue();
				String data;
				data = new String(v.getValue());
				System.out.println(data.toString() );//+ " " + myStore.get(test));*/
			}


		});
	}
}
