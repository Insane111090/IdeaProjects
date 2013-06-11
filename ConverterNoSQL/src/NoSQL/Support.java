package NoSQL;

import oracle.kv.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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

		public static List<String> ParseMetaData( String data, String what ){
			List<String> majorKey = new LinkedList<>();
			List<String> minorKey = new LinkedList<>();
			String maj;
			String min;
			String[] res;
			maj = data.substring(data.indexOf(":")+1,data.indexOf(",")).replaceAll("\"","");
			if(maj.indexOf(";")!=-1){
					res =  maj.split(";");
				for (int i = 0; i <res.length;i++)
				{
					majorKey.add(res[i]);
				}
			}
			else
			{
				 majorKey.add(maj);
			}
			String str = data.replace(data.substring(0, data.indexOf(":") + 1), "");
			min = str.substring(str.indexOf(":")+1,str.length()-1).replaceAll("\"","");
			if(min.indexOf(";") != -1){
				res =  min.split(";");
				for (int i = 0; i <res.length;i++)
				{
					minorKey.add(res[i]);
				}
			}else
			{
				minorKey.add(min);
			}
			if (what.equals("major"))
				return majorKey;
			else
				return minorKey;
		}

		public static void SelectAll(KVStore myStore) {
			String data;
			Iterator<KeyValueVersion> myrecords = myStore.storeIterator(Direction.UNORDERED, 0);
			while (myrecords.hasNext()) {
				Key key = myrecords.next().getKey();
				ValueVersion vv = myStore.get(key);
				Value v = vv.getValue();
				data = new String(v.getValue());
				List<String> majorPath1 = key.getMajorPath();
				List<String> minorPath1 = key.getMinorPath();
				System.out.println(majorPath1 + " - " + minorPath1 + ":" + data);
			}
		}

		public static Value ParseValue(Object noParsedValue) {
				valueString = noParsedValue.toString().substring(noParsedValue.toString().indexOf(":") + 1);
				parsedValue = Value.createValue(valueString.toString().getBytes());

			return parsedValue;
		}
	}
}
