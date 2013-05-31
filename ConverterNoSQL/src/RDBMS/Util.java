package RDBMS;
/**
 * Created with IntelliJ IDEA.
 * User: agavrilov
 * Date: 17.04.13
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */

import net.miginfocom.swing.MigLayout;
import oracle.kv.Durability;
import oracle.kv.Key;
import oracle.kv.Value;

import javax.swing.*;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class Util {
	@SuppressWarnings("serial")
	public static class MigPanel extends JPanel {

		public MigPanel() {
			setLayout(new MigLayout());
		}

		public MigPanel( String constraints ) {
			setLayout(new MigLayout(constraints));
		}

		public MigPanel( String constraints, String rowColConstraints ) {
			setLayout(new MigLayout(constraints,
			                        rowColConstraints));
		}
	}

	public static class KV<V> {
		final Key k;
		final V v;

		public KV( Key k, V v ) {
			this.k = k;
			this.v = v;
		}


	}

}
