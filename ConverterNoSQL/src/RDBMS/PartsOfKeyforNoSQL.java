package RDBMS;
/**
 * Created with IntelliJ IDEA.
 * User: agavrilov
 * Date: 17.04.13
 * Time: 15:42
 * To change this template use File | Settings | File Templates.
 */

import NoSQL.NoSQLStorage;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Андрей
 */
public class PartsOfKeyforNoSQL extends JDialog implements TableModelListener {

	JPanel tableFieldsPanel = new Util.MigPanel("fillx, flowy", "grow");
	final JTable fieldsTable = new JTable(new TableModel());

	final JRadioButton complexMinorKey = new JRadioButton("Choose Complex Minor Key");
	final JRadioButton simpleMinorKey = new JRadioButton("Choose simple Minor Key");

	JLabel showMajorKey = new JLabel();
	JLabel showMinorKey = new JLabel();
	JLabel showValue = new JLabel();
	JLabel forMinorKey = new JLabel("Enter your key: ");
	JTextField txtKeyMinorUser = new JTextField();
	JButton nextToMinor = new JButton("Next");
	JButton nextToValue = new JButton("Next");
	JButton nextToFinish = new JButton("Next");
	JButton connToNoSQLButton = new JButton("Finish");
	List<Object> colNameForMajor = new ArrayList<>();
	List<Object> colNameForMinor = new ArrayList<>();
	List<Object> colNameForValue = new ArrayList<>();
	JScrollPane scrollPaneForTable = new JScrollPane();
	String selectedTableName;
	public boolean nextToMinorButtonClicked, nextToValueButtonClicked;
	public static boolean isSimple;
	public static boolean isComplex;

	private void ClearSelection() {
		isSimple = false;
		isComplex = false;
		//Hide buttons
		nextToMinor.setVisible(true);
		nextToValue.setVisible(false);
		nextToFinish.setVisible(false);
		connToNoSQLButton.setVisible(false);

		complexMinorKey.setVisible(false);
		complexMinorKey.setSelected(false);
		simpleMinorKey.setVisible(false);
		simpleMinorKey.setSelected(false);
		//Hide labels
		showMinorKey.setVisible(false);
		showValue.setVisible(false);
		//Clear lists of selected values
		colNameForMajor.clear();
		colNameForMinor.clear();
		colNameForValue.clear();
		//Buttons flags are false at the beginning
		nextToMinorButtonClicked = false;
		nextToValueButtonClicked = false;
		//Clear Hash sets of values
		TableModel.isAlreadySelectedMajor.clear();
		TableModel.isAlreadySelectedMinor.clear();
		TableModel.isAlredySelectedValues.clear();
		//Set text to labels
		showMajorKey.setText("Your major part of key: ");
		showMinorKey.setText("Your minor part of key: ");
		showValue.setText("Your selected columns for value: ");
		txtKeyMinorUser.setVisible(false);
		forMinorKey.setVisible(false);
		selectedTableName = MainWindow.listOfTables.getSelectedValue().toString();
	}

	void CreateTable() {
		tableFieldsPanel.setBorder(new TitledBorder("Select columns for MAJOR component of key for table " + selectedTableName));
		//scrollPaneForTable.getViewport().setView(fieldsTable);
		tableFieldsPanel.add(fieldsTable.getTableHeader(),
		                     "dock north");
		tableFieldsPanel.add(fieldsTable,
		                     "north");
		tableFieldsPanel.add(simpleMinorKey,
		                     "align left");
		tableFieldsPanel.add(complexMinorKey,
		                     "align left");
		tableFieldsPanel.add(forMinorKey);
		tableFieldsPanel.add(txtKeyMinorUser,
		                     "w 100");
		tableFieldsPanel.add(showMajorKey,
		                     "align center");
		tableFieldsPanel.add(showMinorKey,
		                     "align center");
		tableFieldsPanel.add(showValue,
		                     "align center");
		tableFieldsPanel.add(nextToMinor,
		                     "align right");
		tableFieldsPanel.add(nextToValue,
		                     "align right");
		tableFieldsPanel.add(nextToFinish,
		                     "align right");
		tableFieldsPanel.add(connToNoSQLButton,
		                     "align right");
	}

	//main
	PartsOfKeyforNoSQL() {
		ButtonGroup group = new ButtonGroup();
		group.add(complexMinorKey);
		group.add(simpleMinorKey);
		ClearSelection();
		try {
			TableModel.data = DatabaseWrapper.descriptionTable(selectedTableName);
		} catch ( SQLException e ) {
			JOptionPane.showMessageDialog(
							tableFieldsPanel,
							"Ошибка: " + e.getErrorCode() + ". " + e.getMessage(),
							"Error",
							JOptionPane.ERROR_MESSAGE);
		}
		CreateTable();

		setTitle("Prepare for converting");
		setContentPane(new JScrollPane(tableFieldsPanel));
		setLocation(300,
		            100);
		setModal(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		fieldsTable.getModel().addTableModelListener(this);//Реакция на событие по изменению в таблице

		simpleMinorKey.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				if ( simpleMinorKey.isSelected() ) {
					isSimple = true;
					fieldsTable.setEnabled(true);
					fieldsTable.setVisible(true);
					nextToFinish.setVisible(true);
				}
			}
		});
		complexMinorKey.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				if ( complexMinorKey.isSelected() ) {
					isComplex = true;
					txtKeyMinorUser.setVisible(true);
					forMinorKey.setVisible(true);
					//fieldsTable.setEnabled(true);
					//fieldsTable.setVisible(true);
					nextToValue.setVisible(true);

				}
			}
		});
		//Next to Minor value button
		nextToMinor.addActionListener(new AbstractAction() {
			@Override
			@SuppressWarnings("unchecked")
			public void actionPerformed( ActionEvent ae ) {
				if ( colNameForMajor.isEmpty() ) {
					JOptionPane.showMessageDialog(
									tableFieldsPanel,
									"Ошибка: Major-часть ключа не может быть пустой!",
									"Error",
									JOptionPane.ERROR_MESSAGE);
				} else {

					tableFieldsPanel.setBorder(new TitledBorder(
									"Select columns for MINOR component of key for table " + selectedTableName));
					tableFieldsPanel.remove(nextToMinor);
					TableModel.isAlreadySelectedMajor.addAll(colNameForMajor);

					fieldsTable.setEnabled(false);
					fieldsTable.setVisible(false);
					complexMinorKey.setVisible(true);
					simpleMinorKey.setVisible(true);
					//nextToValue.setVisible(true);
					//showMinorKey.setVisible(true);
					nextToMinorButtonClicked = true;
					showMajorKey.setText(
									"Your major part of key: " + TableModel.isAlreadySelectedMajor.toString().replaceAll(
													",",
													"/"));
				}
			}
		});
		//Next to value button
		nextToValue.addActionListener(new AbstractAction() {
			@Override
			@SuppressWarnings("unchecked")
			public void actionPerformed( ActionEvent e ) {
				tableFieldsPanel.setBorder(new TitledBorder(
								"Select columns for VALUES of table " + selectedTableName));
				if ( complexMinorKey.isSelected() ) {
					try {
						if ( txtKeyMinorUser.getText().isEmpty() )
							throw new Exception();
						else
							tableFieldsPanel.remove(nextToValue);
						colNameForMinor.add(txtKeyMinorUser.getText());
						TableModel.isAlreadySelectedMinor.addAll(colNameForMinor);
						showMinorKey.setText(
										"Your minor part of key: " + TableModel.isAlreadySelectedMinor.toString().replaceAll(
														",",
														"/"));
						txtKeyMinorUser.setVisible(false);
						forMinorKey.setVisible(false);
						complexMinorKey.setVisible(false);
						simpleMinorKey.setVisible(false);
						fieldsTable.setVisible(true);
						fieldsTable.setEnabled(true);
						showMinorKey.setVisible(true);
						nextToMinorButtonClicked = false;
						nextToValueButtonClicked = true;
						nextToFinish.setVisible(true);
					} catch ( Exception ex ) {
						JOptionPane.showMessageDialog(
										tableFieldsPanel,
										"Ошибка: Введите minor ключ!",
										"Error",
										JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		//Next to Finish button
		nextToFinish.addActionListener(new AbstractAction() {
			@Override
			@SuppressWarnings("unchecked")
			public void actionPerformed( ActionEvent ae ) {
				tableFieldsPanel.remove(fieldsTable);
				tableFieldsPanel.remove(fieldsTable.getTableHeader());
				tableFieldsPanel.updateUI();

				TableModel.isAlredySelectedValues.addAll(colNameForValue);
				nextToValueButtonClicked = false;
				if ( simpleMinorKey.isSelected() ) {
					TableModel.isAlreadySelectedMinor.addAll(colNameForMinor);
					showMinorKey.setText(
									"Your minor part of key: " + TableModel.isAlreadySelectedMinor.toString().replaceAll(
													",",
													"/"));
					showMinorKey.setVisible(true);
					showValue.setVisible(false);
				} else {
					showValue.setVisible(true);
					showValue.setText(
									"Your selected columns for value: " + TableModel.isAlredySelectedValues.toString().replaceAll(
													",",
													"/"));
				}
				complexMinorKey.setVisible(false);
				simpleMinorKey.setVisible(false);
				tableFieldsPanel.remove(nextToFinish);
				tableFieldsPanel.setBorder(new TitledBorder("Check your choice before continue"));
				connToNoSQLButton.setVisible(true);
			}
		});
		//To NoSQL Storage Button
		connToNoSQLButton.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				dispose();
				NoSQLStorage storage = new NoSQLStorage();
				NoSQLStorage.progress.setText("");
				storage.setSize(599,
				                600);
				storage.setVisible(true);
				//Process proc;
				//try {
				//    proc = Runtime.getRuntime().exec("java -jar NoSQL_Storage\\kv-ee-2.0.26\\kv-2.0.26\\lib\\kvstore.jar kvlite");
				//} catch (Throwable ex) {
				//   System.out.println(ex.getMessage());

				// }
			}
		});

	}

	/**
	 * Реакция на изменение таблицы, смотрим флаг checkbox, и добавляем или
	 * удаляем элемент в список.
	 *
	 * @param e
	 */
	@Override
	public void tableChanged( TableModelEvent e ) {
		int row = e.getFirstRow();
		Boolean checkBoxState = (Boolean) fieldsTable.getValueAt(row,
		                                                         3);
		if ( ! nextToMinorButtonClicked && checkBoxState && ! nextToValueButtonClicked ) {
			colNameForMajor.add(fieldsTable.getValueAt(row,
			                                           0));
		} else if ( ! nextToMinorButtonClicked && ! checkBoxState && ! nextToValueButtonClicked ) {
			colNameForMajor.remove(fieldsTable.getValueAt(row,
			                                              0));
		} else if ( nextToMinorButtonClicked && checkBoxState && ! nextToValueButtonClicked ) {
			colNameForMinor.add(fieldsTable.getValueAt(row,
			                                           0));
		} else if ( nextToMinorButtonClicked && ! checkBoxState && ! nextToValueButtonClicked ) {
			colNameForMinor.remove(fieldsTable.getValueAt(row,
			                                              0));
		} else if ( ! nextToMinorButtonClicked && checkBoxState && nextToValueButtonClicked ) {
			colNameForValue.add(fieldsTable.getValueAt(row,
			                                           0));
		} else if ( ! nextToMinorButtonClicked && ! checkBoxState && nextToValueButtonClicked ) {
			colNameForValue.remove(fieldsTable.getValueAt(row,
			                                              0));
		}
	}
}
