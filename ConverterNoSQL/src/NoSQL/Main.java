package NoSQL;

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JDialog;
import oracle.kv.*;

/**
 *
 * @author agavrilov
 */
public class Main extends JDialog
{

  static String port = "5000";
  static String host = "localhost";
  static String store = "kvstore";

  public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException
  {
    ConnectionNoSQLStorage orastore = new ConnectionNoSQLStorage(store, host, port);
    KVStore myStore = orastore.getStore();

    myStore.close();
    System.out.println("Store closed");
  }
}
