package NoSQL;

import oracle.kv.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
	public static JTextField tableNameText;
	private JButton startButton;
	private JButton exitButton;
	public JLabel lblTableName;

	public void onConnect(){
	}

	public Set<String> getMetaInfoForTableName(String tableNameText){
		String majorPart , minorPart;
		Key metaKey = Support.ParseKey.ParseKey(tableNameText + "/-/MetaData/",false);
		ValueVersion vv = ConnectionToNoSQL.myStore.get(metaKey);
		Value v = vv.getValue();
		String data = new String(v.getValue());
		Set<String> parts = new HashSet<>();
		majorPart = Support.ParseKey.ParseMetaDataForMajor(data,"major");
		minorPart = Support.ParseKey.ParseMetaDataForMajor(data,"minor");
		parts.add(majorPart);
		parts.add(minorPart);

		return parts;
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
			public void actionPerformed( ActionEvent e ) {
				ConnectionToNoSQL connectionToNoSQL = new ConnectionToNoSQL();
				connectionToNoSQL.main();
				onConnect();
			}
		});

		conenctToRDBMSButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {

			}
		});
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				frame.dispose();
			}
		});


		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				Set<String> partsOfKeyForTable = getMetaInfoForTableName(tableNameText.getText().toUpperCase());
				Key major = Support.ParseKey.ParseKey("EMPLOYEES/",false);
				Iterator<KeyValueVersion> keyValueVersionIterator = ConnectionToNoSQL.myStore.storeIterator(Direction.UNORDERED, 0, major, null, Depth.PARENT_AND_DESCENDANTS);
				while (keyValueVersionIterator.hasNext()){
					MyFormatter formatter = new MyFormatter();
					String res = formatter.toOracleLoaderFormat(keyValueVersionIterator.next(),ConnectionToNoSQL.myStore);
					System.out.println(res);

				}
			}
		});
	}
}
