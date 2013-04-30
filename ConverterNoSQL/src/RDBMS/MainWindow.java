package RDBMS;
/**
 * Created with IntelliJ IDEA.
 * User: agavrilov
 * Date: 17.04.13
 * Time: 14:24
 * To change this template use File | Settings | File Templates.
 */

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class MainWindow {
	static final JFrame mainForm = new JFrame();
	static final JPanel mainPanel = new Util.MigPanel();//Main Panel
	static final JPanel connectionSettings = new Util.MigPanel();//COnnection settings Panel
	static final JPanel resultTables = new Util.MigPanel();//Panel for list of tables of your Scheme
	static final JPanel descSelectedTable = new Util.MigPanel();//Panel for selected table DESC
	/**
	 *
	 */
	static public JTextField statusTxt = new JTextField("Not connected");
	/**
	 *
	 */
	static public JTextField connectedUrlTxt = new JTextField();
	static final ConnectionRDBMSConfigDialog connectionSetupDialog = new ConnectionRDBMSConfigDialog();
	public static JList listOfTables = new JList();//List of result tables from Database
	static JScrollPane scrollPaneonTableList = new JScrollPane();
	static JScrollPane scrollPaneonDescTab = new JScrollPane();
	static JLabel countTablesLbl = new JLabel("Count of tables: ");
	static final JTextField countTablesTxt = new JTextField();
	static final JTextPane descriptionTables = new JTextPane();

	/**
	 * @param connStatus
	 * @param urlconn
	 * @param openConnectionSetup
	 * @param exitApplic
	 * @param getDdlOfSelectedTable_btn
	 * @param handMadeConvert
	 * @param autoConvert
	 * @param startConvert
	 */
	public static void createGUI( JLabel connStatus,
	                              JLabel urlconn,
	                              JButton openConnectionSetup,
	                              JButton exitApplic,
	                              JButton getDdlOfSelectedTable_btn,
	                              JRadioButton handMadeConvert,
	                              JRadioButton autoConvert,
	                              JButton startConvert ) {
	  /*
	   * Adding elements on MainForm window
     */
		connectionSettings.add(connStatus,
		                       "split");
		connectionSettings.add(statusTxt,
		                       "wrap 10, w :100:300");//wrap to the next row
		connectionSettings.add(urlconn,
		                       "split");
		connectionSettings.add(connectedUrlTxt,
		                       "wrap 10, w :500:800, gapleft 20");
		connectionSettings.add(openConnectionSetup,
		                       "wrap");

		scrollPaneonTableList.getViewport().setView(listOfTables);
		resultTables.add(scrollPaneonTableList,
		                 "w 200:250, h 300,wrap 30");
		resultTables.add(countTablesLbl,
		                 "split");
		resultTables.add(countTablesTxt,
		                 "w 20");
		resultTables.add(getDdlOfSelectedTable_btn);

		scrollPaneonDescTab.getViewport().setView(descriptionTables);
		descSelectedTable.add(scrollPaneonDescTab,
		                      "w 600, h 300, wrap");
		descSelectedTable.add(handMadeConvert,
		                      "wrap");
		descSelectedTable.add(autoConvert,
		                      "split, gapright 230");
		descSelectedTable.add(startConvert);
		descSelectedTable.setEnabled(false);

		mainPanel.add(connectionSettings,
		              "wrap, dock north");
		mainPanel.add(resultTables,
		              "split");
		mainPanel.add(descSelectedTable,
		              " wrap");
		mainPanel.add(exitApplic,
		              "align right,gapright 20");

		mainForm.setTitle("MainForm");
		mainForm.setVisible(true);
		mainForm.setResizable(false);
		mainForm.setSize(650,
		                 600);
		mainForm.setContentPane(mainPanel);
		mainForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainForm.setLocationRelativeTo(null);//Appears on the screen center

		connectionSetupDialog.setModal(true);//Makes window modal
	}

	/**
	 * @param args
	 */
	public static void main( String[] args ) {
		connectionSettings.setBorder(new TitledBorder(
						"Configure connection to RDBMS"));
		resultTables.setBorder(new TitledBorder("List of tables"));
		descSelectedTable.setBorder(new TitledBorder(
						"Description of selected table"));
	  /*
     * Creation of Buttons
     */
		//Connection button
		final JButton openConnectionSetup = new JButton("Configure connection to RDBMS");//Button pressed for configure connection
		openConnectionSetup.setToolTipText("Press for enter connection setup");//ToolTip for button

		//Exit button
		final JButton exitApplic = new JButton("Exit");
		exitApplic.setToolTipText("Exit application");

		//GetDdl button
		final JButton getDdlOfSelectedTable_btn = new JButton("Get DDL");
		getDdlOfSelectedTable_btn.setToolTipText(
						"Press for view DDL of the selected table");
		getDdlOfSelectedTable_btn.setEnabled(false);

		//Start convertion button
		final JButton startConvert = new JButton("Start");
		startConvert.setToolTipText("Press to start the Converting data");
		startConvert.setEnabled(false);

    /*
     * Creation ob RadioButtons
     */
		final JRadioButton handMadeConvert = new JRadioButton(
						"Hand convert");
		final JRadioButton autoConvert = new JRadioButton(
						"Auto convert");
		handMadeConvert.setEnabled(false);
		autoConvert.setEnabled(false);
		ButtonGroup group = new ButtonGroup();
		group.add(handMadeConvert);
		group.add(autoConvert);

    /*
     * Labels on mainForm
     */
		final JLabel connStatus = new JLabel("Status: ");//Connection status Label in MainForm
		final JLabel urlconn = new JLabel("URL:");

    /*
     * Text fields on MainForm
     */
		countTablesTxt.setVisible(false);
		countTablesTxt.setBorder(null);
		countTablesTxt.setEditable(false);

		connectedUrlTxt.setEditable(false);

		descriptionTables.setEditable(false);

		statusTxt.setEditable(false);
		statusTxt.setBackground(Color.red);

		//Creation a UI procedure
		createGUI(connStatus,
		          urlconn,
		          openConnectionSetup,
		          exitApplic,
		          getDdlOfSelectedTable_btn,
		          handMadeConvert,
		          autoConvert,
		          startConvert);

		//Watching for tables list: if it is empty, the Button "GetDDL" is unactive
		listOfTables.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged( ListSelectionEvent lse ) {
				if ( ! listOfTables.isSelectionEmpty() ) {
					getDdlOfSelectedTable_btn.setEnabled(true);
				}
			}
		});
    /*
     * Radio buttons on click events
     */
		handMadeConvert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent ae ) {
				if ( handMadeConvert.isSelected() ) {
					startConvert.setEnabled(true);
				}
			}
		});
		autoConvert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent ae ) {
				if ( autoConvert.isSelected() ) {
					startConvert.setEnabled(true);
				}
			}
		});
    /*
     * On buttons click event
     */
		//Connect button event
		openConnectionSetup.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed( ActionEvent ae ) {
				connectionSetupDialog.setTitle("Connection settings");
				connectionSetupDialog.setSize(550,
				                              350);
				connectionSetupDialog.setVisible(true);
			}
		});
		//Exit button event
		exitApplic.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed( ActionEvent ae ) {
				System.exit(0);
			}
		});

		startConvert.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				if ( handMadeConvert.isSelected() ) {
					PartsOfKeyforNoSQL tableFields = new PartsOfKeyforNoSQL();
					tableFields.setSize(700,
					                    500);
					tableFields.setVisible(true);
				}
			}
		});

		//GetDDL button event
		getDdlOfSelectedTable_btn.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed( ActionEvent ae ) {
				String str,
								constText = "<table style='border: black solid 1px; width:100%'><tr><th>Столбец</th> <th>Тип</th> <th>Null</th></tr>",
								selectedTableName = listOfTables.getSelectedValue().toString();

				descSelectedTable.setBorder(new TitledBorder(
								"Table " + selectedTableName));
				descSelectedTable.setEnabled(true);
				descriptionTables.setContentType("text/html");
				try {
					descriptionTables.setText("");
					str = DatabaseWrapper.getDescription(selectedTableName).toString();
					descriptionTables.setText(
									"<html>" + constText + str + "</table></html>");
				} catch ( SQLException ex ) {
					JOptionPane.showMessageDialog(
									mainForm,
									"Ошибка: " + ex.getErrorCode() + ". " + ex.getMessage(),
									"Error",
									JOptionPane.ERROR_MESSAGE);
				}
				handMadeConvert.setEnabled(true);
				autoConvert.setEnabled(true);
			}
		});


	}

	public static final class ConnectionRDBMSConfigDialog extends JDialog {

		final JTextField serverTxt = new JTextField();//Field for server input
		final JTextField portTxt = new JTextField();//Field for port input
		final JTextField sidTxt = new JTextField();//Field for sid input
		final JTextField usernameTxt = new JTextField();//Field for username input
		private transient JPasswordField passwordTxt = new JPasswordField();//Field for password input
		final static JTextField connectionStatusLabel = new JTextField();
		final static JTextField connectionUrlLabel = new JTextField();//Field for connection url
		final static JTextArea connectionErrorLabel = new JTextArea();//Connection error

    /*
     * Procedure for cleaning textFields on connection to DB form
     */

		/**
		 *
		 */
		public void clearFields() {
			serverTxt.setText("");
			portTxt.setText("");
			sidTxt.setText("");
			usernameTxt.setText("");
			passwordTxt.setText("");
			connectionStatusLabel.setText("");
			connectionUrlLabel.setText("");
			connectionErrorLabel.setText("");
			connectionStatusLabel.setBackground(Color.WHITE);
		}

    /*
     * Function, that creates a connection to DB Form GUI
     */

		/**
		 * @param ConnectButton
		 * @param OkButton
		 * @param CancelButton
		 */
		public void createFormGUI( JButton ConnectButton,
		                           JButton OkButton,
		                           JButton CancelButton ) {
			final JPanel ConnectionPanel = new Util.MigPanel();
			final JPanel InputServerPanel = new Util.MigPanel();
			final JPanel InputUserPanel = new Util.MigPanel();
			final JLabel serverLbl = new JLabel("Server: ");
			final JLabel portLbl = new JLabel("Port: ");
			final JLabel sidLbl = new JLabel("SID: ");
			final JLabel usernameLbl = new JLabel("Username: ");
			final JLabel passwordLbl = new JLabel("Password: ");
			final JLabel Error = new JLabel("Error: ");
			final JLabel stat = new JLabel("Status: ");


			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      /*
       * Adding atributes on connectionToDB form
       */
			InputUserPanel.setBorder(new TitledBorder(
							"Username and password settings"));
			InputUserPanel.add(usernameLbl,
			                   "split");
			InputUserPanel.add(usernameTxt,
			                   "wrap 10, w 100:150:200");
			InputUserPanel.add(passwordLbl,
			                   "split");
			InputUserPanel.add(passwordTxt,
			                   "wrap 28, w 100:150:200");

			InputServerPanel.setBorder(new TitledBorder(
							"Server connection settings"));
			InputServerPanel.add(serverLbl,
			                     "split");
			InputServerPanel.add(serverTxt,
			                     "w 150:150:200, wrap");
			InputServerPanel.add(portLbl,
			                     "split");
			InputServerPanel.add(portTxt,
			                     "w 150:150:200, gapleft 18, wrap");
			InputServerPanel.add(sidLbl,
			                     "split");
			InputServerPanel.add(sidTxt,
			                     "w 150:150:200, gapleft 22, wrap 10");

			ConnectionPanel.add(InputServerPanel,
			                    "split");
			ConnectionPanel.add(InputUserPanel,
			                    "wrap 10, gapleft 20");
			ConnectionPanel.add(ConnectButton,
			                    "split");
			ConnectionPanel.add(connectionUrlLabel,
			                    "wrap 10,grow");
			ConnectionPanel.add(stat,
			                    "split");
			ConnectionPanel.add(connectionStatusLabel,
			                    "wrap,w 50:70:");
			ConnectionPanel.add(Error,
			                    "split");
			ConnectionPanel.add(connectionErrorLabel,
			                    "wrap 24, w :1200:,gapleft 12");
			ConnectionPanel.add(OkButton,
			                    "split,align right");
			ConnectionPanel.add(CancelButton);

      /*
       * Editable fields on Connection form
       */
			connectionStatusLabel.setEditable(false);
			connectionUrlLabel.setEditable(false);
			connectionErrorLabel.setEditable(false);

      /*
       * ToolTips for buttons and fields on connection form
       */
			OkButton.setToolTipText("OK");
			CancelButton.setToolTipText("Cancel");
			ConnectButton.setToolTipText("Press for set connection to DB");
			usernameTxt.setToolTipText("Enter your username or login");
			passwordTxt.setToolTipText("Enter your password");
			serverTxt.setToolTipText("Address(link) of DB server");
			portTxt.setToolTipText("Port for DB connection");
			sidTxt.setToolTipText("SID of your DB");

			setContentPane(ConnectionPanel);
			setLocationRelativeTo(null);
		}

		//main
		ConnectionRDBMSConfigDialog() {
			super(mainForm);//calls mainForm constructor
			final JButton ConnectButton = new JButton("Connect");//Button for connection
			final JButton OkButton = new JButton("Ok");
			final JButton CancelButton = new JButton("Cancel");//Exit button

			createFormGUI(ConnectButton,
			              OkButton,
			              CancelButton);

      /*
       * Button click events
       */
			//Cancel button
			CancelButton.addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed( ActionEvent ae ) {
					clearFields();
					dispose();
				}
			});
			//Connect button(establish connection)
			ConnectButton.addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed( ActionEvent ae ) {
					String server = "oracle11.avalon.ru"; //serverTxt.getText().toString();//"oracle11.avalon.ru"
					String sid = "orcl";//sidTxt.getText().toUpperCase().toString();//"ORCL";
					int port = Integer.decode("1521");//portTxt.getText().toString();//"1521";
					String url = "jdbc:oracle:thin:@" + server + ":" + port + ":" + sid;
					try {
						DatabaseWrapper.createConnection("andgavr",
//usernameTxt.getText(),
                                             "andgavr",
//new String(passwordTxt.getPassword()),
                                             url);
					} catch ( SQLException e ) {
						JOptionPane.showMessageDialog(
										mainForm,
										"Ошибка: " + e.getMessage(),
										"Error",
										JOptionPane.ERROR_MESSAGE);
						ConnectionRDBMSConfigDialog.connectionErrorLabel.setText(
										"SQL Error: " + e.getMessage());
						ConnectionRDBMSConfigDialog.connectionStatusLabel.setBackground(
										Color.RED);
						ConnectionRDBMSConfigDialog.connectionStatusLabel.setText(
										"Failed");
						ConnectionRDBMSConfigDialog.connectionUrlLabel.setText("");
					} catch ( ClassNotFoundException e ) {
						JOptionPane.showMessageDialog(
										mainForm,
										"Ошибка: " + e.getMessage(),
										"Error",
										JOptionPane.ERROR_MESSAGE);
						connectionErrorLabel.setText(
										"ClassNotFoundException: " + e.getMessage());
						connectionStatusLabel.setBackground(Color.RED);
						connectionStatusLabel.setText("Failed");
						connectionUrlLabel.setText("");
					}
					if ( DatabaseWrapper.isConnected() ) {
						ConnectionRDBMSConfigDialog.connectionErrorLabel.setText("");
						ConnectionRDBMSConfigDialog.connectionStatusLabel.setBackground(
										Color.GREEN);
						ConnectionRDBMSConfigDialog.connectionStatusLabel.setText(
										"Succeed");
						ConnectionRDBMSConfigDialog.connectionUrlLabel.setText(
										"Connected to: " + url);
					} else {
						ConnectionRDBMSConfigDialog.connectionStatusLabel.setBackground(
										Color.RED);
						ConnectionRDBMSConfigDialog.connectionStatusLabel.setText(
										"Failed");
						ConnectionRDBMSConfigDialog.connectionUrlLabel.setText("");
					}
				}
			});
			//Ok Button (Confirm)
			OkButton.addActionListener(new AbstractAction() {
				@Override
				@SuppressWarnings("unchecked")
				public void actionPerformed( ActionEvent ae ) {
					if ( DatabaseWrapper.isConnected() ) {
						MainWindow.statusTxt.setText("Connected");
						MainWindow.statusTxt.setBackground(Color.green);
						MainWindow.connectedUrlTxt.setText(
										connectionUrlLabel.getText());
					} else {
						MainWindow.statusTxt.setText("Not connected");
						MainWindow.statusTxt.setBackground(Color.red);
						MainWindow.connectedUrlTxt.setText(connectionUrlLabel.getText());
					}
					try {
						listOfTables.setListData(DatabaseWrapper.getTableList().toArray());
					} catch ( SQLException | NullPointerException e ) {
						JOptionPane.showMessageDialog(
										MainWindow.mainForm,
										"Ошибка: " + e.getMessage(),
										"Error",
										JOptionPane.ERROR_MESSAGE);
					}
					countTablesTxt.setVisible(true);
					countTablesTxt.setText(String.valueOf(DatabaseWrapper.tables.size()));

					DatabaseWrapper.clearArrayList();
					dispose();
				}
			});
		}
	}
}
