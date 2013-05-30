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

	public static class KV {
		final Key k;
		Value v;
		InputStream s;
		Durability d;
		long l;
		TimeUnit tu;

		public KV( Key k, Value v ) {
			this.k = k;
			this.v = v;
		}

		public KV( Key k, InputStream s, Durability d, long l, TimeUnit tu ) {
			this.k = k;
			this.s = s;
			this.d = d;
			this.l = l;
			this.tu = tu;

		}

	}

}
