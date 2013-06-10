package NoSQL;

import RDBMS.MainWindowForConverting;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
	private JTextField tableNameText;
	private JButton startButton;
	private JButton exitButton;
	public JLabel lblTableName;

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
				//connectionToNoSQL.setLocationRelativeTo(null);
				//connectionToNoSQL.setVisible(true);
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

			}
		});
	}
}
