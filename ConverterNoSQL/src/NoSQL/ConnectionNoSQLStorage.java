package NoSQL;

import oracle.kv.*;

/**
 *
 * @author agavrilov
 */
public class ConnectionNoSQLStorage
{

  private  KVStore oraStore;
	private static boolean _isConnectedToStore;
  public ConnectionNoSQLStorage(String name,
                                String host,
                                String port) throws FaultException, NullPointerException
  {
    KVStoreConfig kvConfig = new KVStoreConfig(name, host + ':' + port);
		  oraStore = KVStoreFactory.getStore(kvConfig);
		  _isConnectedToStore = oraStore != null;
  }

  public KVStore getStore()
  {
    return oraStore;
  }

	public static Boolean isConenctedToStore()
	{
		return _isConnectedToStore;
	}
}
