/**
 * Created with IntelliJ IDEA.
 * User: agavrilov
 * Date: 17.04.13
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
public class Util {
    @SuppressWarnings("serial")
    public static class MigPanel extends JPanel
    {

        public MigPanel()
        {
            setLayout(new MigLayout());
        }

        public MigPanel(String constraints)
        {
            setLayout(new MigLayout(constraints));
        }
    }


}
