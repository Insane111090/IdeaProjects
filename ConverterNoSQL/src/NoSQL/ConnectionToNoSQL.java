package NoSQL;

import oracle.kv.FaultException;
import oracle.kv.KVStore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public  class ConnectionToNoSQL extends JDialog {
	private JPanel contentPane;
	JButton buttonOK;
	private JButton buttonCancel;
	private JPanel NoSQLConnPanel;
	private JPanel ButtonPanel;
	private JTextField txtStorageName;
	private JTextField txtHost;
	private JTextField txtPort;
  private JButton connectButton;
	private JLabel connectedlbl;
	public static KVStore myStore;


	public ConnectionToNoSQL() {
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);


		buttonOK.addActionListener(new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				onOK();
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				onCancel();
			}
		});

// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing( WindowEvent e ) {
				onCancel();
			}
		});

// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				onCancel();
			}
		},
		                                   KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,
		                                                          0),
		                                   JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {

				try {
					myStore = Support.makeNoSQLConnection("MyStore",//txtStorageName.getText(),
									"localhost",//txtHost.getText(),
									5000);//Integer.decode(txtPort.getText().toString()));
				} catch (Throwable ee){
					JOptionPane.showMessageDialog(
									contentPane,
									"An error accuses during connection to KVStore: " + ee.getMessage(),
									"Error",
									JOptionPane.ERROR_MESSAGE);
				}
				if (myStore != null){
					   onConnected();
				}
				else
				{
					  onFailedConnect();
				}

			}
		});
	}

	protected void onOK() {
	 dispose();
	}

	protected void onConnected(){
		connectedlbl.setText("Connected");
		connectedlbl.setForeground(Color.GREEN);
	}
	protected void onFailedConnect(){
		connectedlbl.setForeground(Color.RED);
		connectedlbl.setText("Not connected");
	}
	private void onCancel() {
// add your code here if necessary
		dispose();
	}

}
