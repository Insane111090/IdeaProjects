package NoSQL;

import oracle.kv.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: agavrilov
 * Date: 10.06.13
 * Time: 16:02
 * To change this template use File | Settings | File Templates.
 */
public class ExternalTable {
	private JButton connectToNoSQLButton;
	public JPanel ExternalPanel;
	private JButton conenctToRDBMSButton;
	private JLabel lblConnectToNoSQL;
	private JLabel lblConnectToRDBMS;
	public JLabel lblNoSQLConnected;
	private JLabel lblRDBMSConnected;
	public JTextField tableNameText;
	private JButton startButton;
	private JButton exitButton;
	public JLabel lblTableName;

	public List<String> getMetaInfoForTableName(String tableNameText, boolean flag)
					throws NullPointerException, IllegalArgumentException,StringIndexOutOfBoundsException {
		List<String> majorPart, minorPart;
		Key metaKey = Support.ParseKey.ParseKey(tableNameText + "/-/MetaData/", false);
		ValueVersion vv = ConnectionToNoSQL.myStore.get(metaKey);
		Value v = vv.getValue();

		String data = new String(v.getValue());

		List<String> parts = new LinkedList<>();
		if ( flag ) {
			majorPart = Support.ParseKey.ParseMetaData(data,
							"major");
			return majorPart;
		}
		else {
			minorPart = Support.ParseKey.ParseMetaData(data,
							"minor");
			return minorPart;
		}
	}


	public ExternalTable() {
		JFrame frame = new JFrame();
		frame.add(ExternalPanel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);


		connectToNoSQLButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ConnectionToNoSQL connectionToNoSQL = new ConnectionToNoSQL();

				connectionToNoSQL.buttonOK.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						lblNoSQLConnected.setText("Connected");
						lblNoSQLConnected.setForeground(Color.GREEN);
					}
				});
				connectionToNoSQL.main();
			}
		});

		conenctToRDBMSButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});


		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				StringBuilder nameofColumns = new StringBuilder();
				List<String> majorPartForTable;
				List<String> minorPartForTable;
				try {
					majorPartForTable = getMetaInfoForTableName(tableNameText.getText().toUpperCase(), true);
					minorPartForTable = getMetaInfoForTableName(tableNameText.getText().toUpperCase(), false);

					for ( String str : majorPartForTable ) {
						nameofColumns.append(str).append("|");
					}
					for ( String str : minorPartForTable ) {
						nameofColumns.append(str).append("|");
					}
					System.out.println(nameofColumns.replace(nameofColumns.length() - 1, nameofColumns.length(), ""));
					Key major = Support.ParseKey.ParseKey(tableNameText.getText().toUpperCase(),
									false);
					Iterator<KeyValueVersion> keyValueVersionIterator = ConnectionToNoSQL.myStore.storeIterator(Direction.UNORDERED, 0, major, null, Depth.PARENT_AND_DESCENDANTS);
					while ( keyValueVersionIterator.hasNext() ) {
						MyFormatter formatter = new MyFormatter();
						String res = formatter.toOracleLoaderFormat(keyValueVersionIterator.next(), ConnectionToNoSQL.myStore);
						System.out.println(res);
					}
				} catch ( NullPointerException ne ) {
					JOptionPane.showMessageDialog(frame, "Don't have such key in storage!", "Error", JOptionPane.ERROR_MESSAGE);
				} catch ( StringIndexOutOfBoundsException se ) {
					JOptionPane.showMessageDialog(frame, "Error: " + se.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				} catch ( IllegalArgumentException ie ){
					JOptionPane.showMessageDialog(frame, "Error: " + ie.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				//Support.ParseKey.SelectAll(ConnectionToNoSQL.myStore);

			}
		});
	}
}
