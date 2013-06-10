package NoSQL;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.KVStore;
import oracle.kv.Value;
import oracle.kv.exttab.Formatter;

/**
 * Created with IntelliJ IDEA.
 * User: Андрей
 * Date: 10.06.13
 * Time: 20:51
 * To change this template use File | Settings | File Templates.
 */
public class MyFormatter implements Formatter {

	private static final String USER_OBJECT_TYPE = ExternalTable.tableNameText.getText();

	@Override
	public String toOracleLoaderFormat(KeyValueVersion kvv, KVStore kvStore) {
		final Key KeyFromStorage = kvv.getKey();
		final Value ValueFromStorage = kvv.getValue();
		final String stringValue = new String(ValueFromStorage.getValue());

		final List<String> majorPath = KeyFromStorage.getMajorPath();
		final List<String> minorPath = KeyFromStorage.getMinorPath();
		final String objectType = majorPath.get(0);




		return null;
	}
}
