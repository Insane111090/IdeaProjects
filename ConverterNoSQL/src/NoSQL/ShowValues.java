package NoSQL;

import javax.swing.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import oracle.kv.*;
import oracle.kv.impl.api.ops.StoreIterate;

public class ShowValues extends JDialog {
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JTextField showValueTableNameText;
	private JTextField ShowValueKeyTxt;
	private JTextArea showValueTextArea;
	private JButton showButton;
	private JPanel valuesFromNoSQL;

	public ShowValues() {
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);
		showValueTextArea.setEditable(false);
		buttonOK.addActionListener(new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				onOK();
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				onCancel();
			}
		});

		showButton.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				showValueTextArea.removeAll();
				showValueTextArea.setText("");
				try {
					double before = System.currentTimeMillis();
					if ( ShowValueKeyTxt.getText().equals("") ) {
						Key showKey = Support.ParseKey.ParseKey(showValueTableNameText.getText().toUpperCase(),
						                                        false);
						Iterator<KeyValueVersion> showWithoutKey = NoSQLStorage.myStore.storeIterator(Direction.UNORDERED,
						                                                                              0,
						                                                                              showKey,
						                                                                              null,
						                                                                              Depth.PARENT_AND_DESCENDANTS);
						while ( showWithoutKey.hasNext() ) {
							Key myKey = showWithoutKey.next().getKey();
							ValueVersion vv = NoSQLStorage.myStore.get(myKey);
							Value myValue = vv.getValue();
							String data = new String(myValue.getValue());
							List<String> majorPath1 = myKey.getMajorPath();
							List<String> minorPath1 = myKey.getMinorPath();
							showValueTextArea.append(majorPath1 + "-" + minorPath1 + ":" + data + "\n");
						}
					} else {
						Key showKey = Support.ParseKey.ParseKey(showValueTableNameText.getText().toUpperCase() + "/" + ShowValueKeyTxt.getText().toUpperCase(),
						                                        false);
						SortedMap<Key, ValueVersion> myRecords;
						myRecords = NoSQLStorage.myStore.multiGet(showKey,
						                                          null,
						                                          Depth.PARENT_AND_DESCENDANTS,
						                                          Consistency.NONE_REQUIRED,
						                                          1,
						                                          TimeUnit.MILLISECONDS);
						for ( SortedMap.Entry<Key, ValueVersion> entry : myRecords.entrySet() ) {
							ValueVersion vv = entry.getValue();
							Value v = vv.getValue();
							Key myKeyOut = entry.getKey();
							String data = new String(v.getValue());
							List<String> majorPath1 = myKeyOut.getMajorPath();
							List<String> minorPath1 = myKeyOut.getMinorPath();
							showValueTextArea.append(majorPath1 + " - " + minorPath1 + ":" + data + "\n");
						}
					}
					double after = System.currentTimeMillis();
					double diff = after - before;
					System.out.println(diff);
				} catch ( NullPointerException ne ) {
				 showValueTextArea.append("Error: " + ne.getMessage());
				}catch (FaultException fe){
					showValueTextArea.append("Error: " + fe.getMessage());
				}
				catch (Throwable ex) {
					showValueTextArea.append("Error: " + ex.getMessage());
				}
			}
		});
// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing( WindowEvent e ) {
				onCancel();
			}
		});

// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				onCancel();
			}
		},
		                                   KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,
		                                                          0),
		                                   JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	private void onOK() {
// add your code here
		dispose();
	}

	private void onCancel() {
// add your code here if necessary
		dispose();
	}

}
