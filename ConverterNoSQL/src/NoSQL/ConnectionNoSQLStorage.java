package NoSQL;

import oracle.kv.*;

/**
 *
 * @author agavrilov
 */
public class ConnectionNoSQLStorage
{

  private final KVStore oraStore;

  public ConnectionNoSQLStorage(String name,
                                String host,
                                String port)
  {
    KVStoreConfig kvConfig = new KVStoreConfig(name, host + ':' + port);
    oraStore = KVStoreFactory.getStore(kvConfig);
    System.out.println("Store opened");
  }

  public KVStore getStore()
  {
    return oraStore;
  }
}
