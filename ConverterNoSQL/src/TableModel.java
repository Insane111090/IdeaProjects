/**
 * Created with IntelliJ IDEA.
 * User: agavrilov
 * Date: 17.04.13
 * Time: 15:43
 * To change this template use File | Settings | File Templates.
 */
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.table.AbstractTableModel;

/**
 * Модель таблицы
 *
 * @author agavrilov
 */
public final class TableModel extends AbstractTableModel
{

    static String[] columnsName = {"Column Name", "Type", "NULL", "Major/Minor key"};
    public static Object[][] data;
    /**
     *
     */
    public final static Set isAlreadySelectedMajor = new LinkedHashSet();
    /**
     *
     */
    public final static Set isAlreadySelectedMinor = new LinkedHashSet();
    public final static Set isAlredySelectedValues = new LinkedHashSet();
  /*
   *
   * @param col
   * @return
   */

    @Override
    public String getColumnName(int col)
    {
        return columnsName[col].toString();
    }

    @Override
    public int getRowCount()
    {
        return data.length;
    }

    @Override
    public int getColumnCount()
    {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row,
                             int col)
    {
        return data[row][col];
    }

    @Override
    public boolean isCellEditable(int row,
                                  int col)
    {
        boolean isCheckBox = col >= 3;
        return isCheckBox && !isAlreadySelectedMajor.contains(getValueAt(row,
                0))
                && !isAlreadySelectedMinor.contains(getValueAt(row,
                0))
                && !isAlredySelectedValues.contains(getValueAt(row, 0));
    }

    @Override
    public Class getColumnClass(int c)
    {
        return getValueAt(0,
                c).getClass();
    }

    @Override
    public void setValueAt(Object value,
                           int row,
                           int col)
    {
        data[row][col] = value;
        fireTableCellUpdated(row,
                col);
    }
}
