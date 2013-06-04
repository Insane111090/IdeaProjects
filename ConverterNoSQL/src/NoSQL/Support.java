package NoSQL;

import oracle.kv.*;

import java.util.ArrayList;
import java.util.List;

/** @author agavrilov */
public class Support {
	public static KVStore makeNoSQLConnection(String name, String host, int port) {
		KVStoreConfig kvConfig = new KVStoreConfig(name,
						host + ':' + port);
		return KVStoreFactory.getStore(kvConfig);
	}

	public static class ParseKey {
		public static Key parsedKey;
		public static Value parsedValue;
		static String valueString = "";

		public static Key ParseKey(String notParsedKey, boolean flag) {

			int endOfString;
			if ( notParsedKey.indexOf(":") != - 1 ) {
				endOfString = notParsedKey.indexOf(":") - 1;
			}
			else {
				endOfString = notParsedKey.length();
			}

			String keysString;
			if ( flag )
				keysString = notParsedKey.substring(0,
								endOfString) + ".lob";
			else
				keysString = notParsedKey.substring(0,
								endOfString);
			List<String> majorComponents = new ArrayList<>();
			List<String> minorComponents = new ArrayList<>();

			String[] keysArray = keysString.split("/");
			boolean isMajor = true;
			for ( int i = 0; i < keysArray.length; i++ ) {
				if ( keysArray[i].equals("-") ) {
					isMajor = false;
					continue;
				}
				if ( isMajor ) {
					majorComponents.add(keysArray[i]);
				}
				else {
					minorComponents.add(keysArray[i]);
				}
			}
			if ( (majorComponents.size() > 0) && (minorComponents.size() > 0) ) {
				parsedKey = Key.createKey(majorComponents,
								minorComponents);
			}
			else if ( (majorComponents.size() > 0) & (minorComponents.size() <= 0) ) {
				parsedKey = Key.createKey(majorComponents);
			}
			else {
				System.out.println("Error");
			}

			return parsedKey;
		}

		public static Value ParseValue(Object noParsedValue) {
				valueString = noParsedValue.toString().substring(noParsedValue.toString().indexOf(":") + 1);
				parsedValue = Value.createValue(valueString.toString().getBytes());

			return parsedValue;
		}
	}
}
