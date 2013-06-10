import NoSQL.ExternalTable;
import RDBMS.MainWindowForConverting;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: agavrilov
 * Date: 10.06.13
 * Time: 14:14
 * To change this template use File | Settings | File Templates.
 */
public class MainForm {
	private JPanel NoSQLConverter;
	private JButton convertDataToNoSQLButton;
	private JButton getDataFromNoSQLButton;

	public MainForm() {
		convertDataToNoSQLButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				MainWindowForConverting m = new	MainWindowForConverting();
				m.main();
			}
		});
		getDataFromNoSQLButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				new ExternalTable();
			}
		});
	}

	public static void main( String[] args ) {
		JFrame frame = new JFrame("NoSQL Data converter");
		frame.setContentPane(new MainForm().NoSQLConverter);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
	}
}