/**
 * Created with IntelliJ IDEA.
 * User: agavrilov
 * Date: 17.04.13
 * Time: 15:42
 * To change this template use File | Settings | File Templates.
 */
import com.intellij.ui.table.JBTable;

import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * @author Андрей
 */
public class PartsOfKeyforNoSQL extends JDialog implements TableModelListener
{

    static JPanel tableFieldsPanel = new Util.MigPanel("fillx, flowy");
    static final JTable fieldsTable = new JBTable(new TableModel());
    static JLabel showMajorKey = new JLabel();
    static JLabel showMinorKey = new JLabel();
    static JLabel showValue = new JLabel();
    static JLabel warning = new JLabel();
    static JButton nextToMinor = new JButton("Next");
    static JButton nextToValue = new JButton("Next");
    static JButton nextToFinish = new JButton("Finish");
    static JButton connToNoSQLButton = new JButton("Finish");
    static List<Object> colNameForMajor = new ArrayList<>();
    static List<Object> colNameForMinor = new ArrayList<>();
    static List<Object> colNameForValue = new ArrayList<>();
    static String selectedTableName;
    public static boolean nextToMinorButtonClicked, nextToValueButtonClicked;

    private static void ClearSelection()
    {
        //Hide buttons
        nextToMinor.setVisible(true);
        nextToValue.setVisible(false);
        nextToFinish.setVisible(false);
        connToNoSQLButton.setVisible(false);
        //Hide labels
        showMinorKey.setVisible(false);
        showValue.setVisible(false);
        //Clear lists of selected values
        colNameForMajor.clear();
        colNameForMinor.clear();
        colNameForValue.clear();
        //Buttons flags are false at the begining
        nextToMinorButtonClicked = false;
        nextToValueButtonClicked = false;
        //Crear Hash sets of values
        TableModel.isAlreadySelectedMajor.clear();
        TableModel.isAlreadySelectedMinor.clear();
        TableModel.isAlredySelectedValues.clear();
        //Set text to labels
        showMajorKey.setText("Your major part of key: ");
        showMinorKey.setText("Your minor part of key: ");
        showValue.setText("Your selected columns for value: ");
        selectedTableName = MainWindow.listOfTables.getSelectedValue().toString();
    }

    static void CreateTable()
    {
        tableFieldsPanel.setBorder(new TitledBorder("Select columns for MAJOR component of key for table " + selectedTableName));

        tableFieldsPanel.add(fieldsTable.getTableHeader(), "dock north");
        tableFieldsPanel.add(fieldsTable, "north");
        tableFieldsPanel.add(showMajorKey, "align center");
        tableFieldsPanel.add(showMinorKey, "align center");
        tableFieldsPanel.add(showValue, "align center");
        tableFieldsPanel.add(warning, "align center");
        tableFieldsPanel.add(nextToMinor, "align right");
        tableFieldsPanel.add(nextToValue, "align right");
        tableFieldsPanel.add(nextToFinish, "align right");
        tableFieldsPanel.add(connToNoSQLButton, "align right");
    }

    //main
    PartsOfKeyforNoSQL()
    {
        ClearSelection();
        try {
            TableModel.data = DatabaseWrapper.descriptionTable(selectedTableName);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    tableFieldsPanel,
                    "Ошибка: " + e.getErrorCode() + ". " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }


        CreateTable();

        setTitle("Prepare for converting");
        setContentPane(tableFieldsPanel);
        setLocation(300,
                100);
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        fieldsTable.getModel().addTableModelListener(this);//Реакция на событие по изменению в таблице

        //Реакция на кнопку Next
        nextToMinor.addActionListener(new AbstractAction()
        {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent ae)
            {
                if (colNameForMajor.isEmpty()) {
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
                    nextToValue.setVisible(true);
                    showMinorKey.setVisible(true);
                    nextToMinorButtonClicked = true;
                    showMajorKey.setText(
                            "Your major part of key: " + TableModel.isAlreadySelectedMajor.toString().replaceAll(
                                    ",",
                                    "/"));
                }
            }
        });

        nextToValue.addActionListener(new AbstractAction()
        {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e)
            {
                tableFieldsPanel.setBorder(new TitledBorder(
                        "Select columns for VALUES of table " + selectedTableName));
                tableFieldsPanel.remove(nextToValue);
                TableModel.isAlreadySelectedMinor.addAll(colNameForMinor);
                showMinorKey.setText(
                        "Your minor part of key: " + TableModel.isAlreadySelectedMinor.toString().replaceAll(
                                ",",
                                "/"));
                nextToMinorButtonClicked = false;
                nextToValueButtonClicked = true;
                nextToFinish.setVisible(true);
            }
        });


        nextToFinish.addActionListener(new AbstractAction()
        {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent ae)
            {
                tableFieldsPanel.remove(fieldsTable);
                tableFieldsPanel.remove(fieldsTable.getTableHeader());
                tableFieldsPanel.updateUI();

                TableModel.isAlredySelectedValues.addAll(colNameForValue);
                nextToValueButtonClicked = false;
                showValue.setVisible(true);
                showValue.setText(
                        "Your selected columns for value: " + TableModel.isAlredySelectedValues.toString().replaceAll(
                                ",",
                                "/"));
                tableFieldsPanel.remove(nextToFinish);
                tableFieldsPanel.setBorder(new TitledBorder("Chech your choice before continue"));
                connToNoSQLButton.setVisible(true);
            }
        });
        connToNoSQLButton.addActionListener(new AbstractAction()
        {
            private String[] args;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Process proc;
                try {
                    proc = Runtime.getRuntime().exec("java -jar NoSQL_Storage\\kv-ee-2.0.26\\kv-2.0.26\\lib\\kvstore.jar kvlite");
                } catch (Throwable ex) {
                    System.out.println(ex.getMessage());

                }
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
    public void tableChanged(TableModelEvent e)
    {
        int row = e.getFirstRow();
        Boolean checkBoxState = (Boolean) fieldsTable.getValueAt(row, 3);
        if (!nextToMinorButtonClicked && checkBoxState && !nextToValueButtonClicked) {
            colNameForMajor.add(fieldsTable.getValueAt(row, 0));
        } else if (!nextToMinorButtonClicked && !checkBoxState && !nextToValueButtonClicked) {
            colNameForMajor.remove(fieldsTable.getValueAt(row, 0));
        } else if (nextToMinorButtonClicked && checkBoxState && !nextToValueButtonClicked) {
            colNameForMinor.add(fieldsTable.getValueAt(row, 0));
        } else if (nextToMinorButtonClicked && !checkBoxState && !nextToValueButtonClicked) {
            colNameForMinor.remove(fieldsTable.getValueAt(row, 0));
        } else if (!nextToMinorButtonClicked && checkBoxState && nextToValueButtonClicked) {
            colNameForValue.add(fieldsTable.getValueAt(row, 0));
        } else if (!nextToMinorButtonClicked && !checkBoxState && nextToValueButtonClicked) {
            colNameForValue.remove(fieldsTable.getValueAt(row, 0));
        }
    }
}
