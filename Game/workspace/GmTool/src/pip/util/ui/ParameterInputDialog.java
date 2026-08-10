package pip.util.ui;

import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.*;
import javax.swing.*;

import java.util.*;
import javax.swing.border.*;
import java.io.*;

public class ParameterInputDialog implements ActionListener {
	private LayoutUtil layout = new LayoutUtil();
	private Object obj;
	private ParameterFilled callBack;
	private JDialog dialog;
	private JFileChooser fileChooser;
	private File dir = new File(System.getProperty("user.dir"));
	ArrayList<DataChangeListener> listeners = new ArrayList<DataChangeListener>();
	private HashMap<Class,ParameterDescripter[]> propFlds = new  HashMap<Class,ParameterDescripter[]>();
	private HashMap<String,ParameterDescripter[]> propSpecialFlds = new  HashMap<String,ParameterDescripter[]>();
	public String confirmBtnTitle = "Ok";
	public String cancelBtnTitle = "Back";
	public void setParameterNames(Class kls, ParameterDescripter names[]) {
		propFlds.put(kls, names);
	}
	public void setParameterNames(Class kls, String prop, ParameterDescripter names[]) {
		propSpecialFlds.put(kls.getName() + "." + prop, names);
	}

	public void setParameterNames(Class kls, String[][] names) {
		ParameterDescripter params[] = new ParameterDescripter[names.length];
		for (int i = 0; i < params.length; i++) {
			params[i] = new ParameterDescripter();
			params[i].name = names[i][0];
			if (names[i].length > 1) {
				params[i].title = names[i][1];
			}
			if (names[i].length > 2) {
				params[i].tips = names[i][2];
			}
			if (names[i].length > 3) {
				params[i].type = names[i][3];
			}
		}
		propFlds.put(kls, params);
	}
	public void setParameterNames(Class kls, String prop, String[][] names) {
		ParameterDescripter params[] = new ParameterDescripter[names.length];
		for (int i = 0; i < params.length; i++) {
			params[i] = new ParameterDescripter();
			params[i].name = names[i][0];
			if (names[i].length > 1) {
				params[i].title = names[i][1];
			}
			if (names[i].length > 2) {
				params[i].tips = names[i][2];
			}
			if (names[i].length > 3) {
				params[i].type = names[i][3];
			}
		}
		propSpecialFlds.put(kls.getName() + "." + prop, params);
	}
	public static class ParameterDescripter {
		public String name;
		public String type = null;
		public String title;
		public String tips;
		public Class validater = null;
		public ParameterDescripter() {
		}
		public ParameterDescripter(String name, String title, String tips) {
			this.name = name;
			this.title = title;
			this.tips = tips;
		}
	}
	public void openDialog(String title, Window winOwner, Object obj, ParameterFilled callBack) {
		this.obj = obj;
		this.callBack = callBack;
		listeners.clear();
		dialog = new JDialog(winOwner);
		dialog.setTitle(title);
		dialog.setLayout(new BorderLayout());
		dialog.add(new JScrollPane(getComponent(obj, null)), BorderLayout.CENTER);
		dialog.add(new JLabel(" "), BorderLayout.NORTH);
		dialog.add(new JLabel(" "), BorderLayout.EAST);
		dialog.add(new JLabel(" "), BorderLayout.WEST);

		JPanel pp = new JPanel();
		JButton btn = new JButton(confirmBtnTitle);
		btn.setActionCommand("OK");
		btn.addActionListener(this);
		pp.add(btn);
		btn = new JButton(cancelBtnTitle);
		btn.setActionCommand("Back");
		btn.addActionListener(this);
		pp.add(btn);
		dialog.add(pp, BorderLayout.SOUTH);
		dialog.pack();
		layout.setWindowCentrallize(dialog, winOwner);
		dialog.setVisible(true);
	}
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("OK")) {
			dialog.setVisible(false);
			for (DataChangeListener l : listeners) {
				l.dataChanged();
			}
			if (callBack != null) {
				callBack.finished(obj);
			}
		} else if (cmd.equals("Back")) {
			dialog.setVisible(false);
		}
	}
	private class FileActionListener implements ActionListener {
		JTextField textField;
		String []filters;
		String msg;
		public FileActionListener(JTextField fld,String flt[]) {
			textField = fld;
			if (flt != null && flt.length > 1) {
				msg = flt[1];
				if (flt.length > 2) {
					filters = new String[flt.length - 2];
					for (int i = 0; i < filters.length; i++) {
						filters[i] = flt[i+2];
					}
				}
			}
		}
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			String fileName = textField.getText();
			File ff = new File(fileName);
			if (cmd.equals("...")) {
				if (ff.exists()) {
					fileChooser = new JFileChooser(ff);
				} else {
					fileChooser = new JFileChooser(dir);
				}
				fileChooser.addActionListener(this);
				if (filters != null && filters.length > 0) {
					FileNameExtensionFilter filter = new FileNameExtensionFilter(msg, filters);
					fileChooser.setFileFilter(filter);
				}
				int returnVal = fileChooser.showOpenDialog(dialog);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					textField.setText(file.getAbsolutePath());
					dir = file.getParentFile();
				}
			}
		}
	}
	private JComponent getComponent(Object obj, ParameterDescripter flds[]) {
		Class ownerClass = obj.getClass();
		if (flds == null) {
			flds = propFlds.get(ownerClass);
			if (flds == null) {
				if (obj instanceof ParameterCap) {
					String [][]vs = ((ParameterCap)obj).getParameterDescs();
					flds = new ParameterDescripter[vs.length];
					for (int i = 0; i < flds.length; i++) {
						flds[i] = new ParameterDescripter();
						flds[i].name = vs[i][0];
						if (vs[i].length > 1) {
							flds[i].title = vs[i][1];
						}
						if (vs[i].length > 2) {
							flds[i].tips = vs[i][2];
						}
						if (vs[i].length > 3) {
							flds[i].type = vs[i][3];
						}
					}
				}
			}
		}
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		int row = 0;
		for (ParameterDescripter s : flds) {
			try {
				Field fld = ownerClass.getDeclaredField(s.name);
				Class kls = fld.getType();
				if (s.type == null) {
					if (kls == String.class) {
						String value = (String)fld.get(obj);
						JTextField tf = new JTextField(value);
						listeners.add(new StringFieldListener(obj, s.name, value, tf));
						if (s.title != null) {
							JLabel lbl = new JLabel(s.title);
							lbl.setHorizontalAlignment(SwingConstants.RIGHT);
							p.add(lbl, layout.getConstrains(0, row, 1, 1, 1, 1));
							p.add(tf, layout.getConstrains(1, row++, 2, 1, 10, 1));
						} else {
							p.add(tf, layout.getConstrains(0, row++, 3, 1, 11, 1));
						}
						if (s.tips != null) {
							tf.setToolTipText(s.tips);
						}
					} else if (kls == int.class || kls == Integer.class || kls == byte.class || kls == Byte.class || kls == short.class || 
							kls == Short.class || kls == Long.class || kls == long.class || kls == double.class || kls == Double.class ||
							kls == Float.class || kls == float.class) {
							String value = null;
							if (kls == int.class || kls == Integer.class) {
								value = String.valueOf(fld.getInt(obj));
							} else if (kls == byte.class || kls == Byte.class) {
								value = String.valueOf(fld.getByte(obj));
							} else if (kls == short.class || kls == Short.class) {
								value = String.valueOf(fld.getShort(obj));
							} else if (kls == Long.class || kls == long.class) {
								value = String.valueOf(fld.getLong(obj));
							} else if (kls == double.class || kls == Double.class) {
								value = String.valueOf(fld.getDouble(obj));
							} else if (kls == Float.class || kls == float.class) {
								value = String.valueOf(fld.getFloat(obj));
							}
							JTextField tf = new JTextField(value);
							listeners.add(new NumberFieldListener(obj, s.name, value, tf));
							if (s.title != null) {
								JLabel lbl = new JLabel(s.title);
								lbl.setHorizontalAlignment(SwingConstants.RIGHT);
								p.add(lbl, layout.getConstrains(0, row, 1, 1, 1, 1));
								p.add(tf, layout.getConstrains(1, row++, 2, 1, 10, 1));
							} else {
								p.add(tf, layout.getConstrains(0, row++, 3, 1, 11, 1));
							}
							if (s.tips != null) {
								tf.setToolTipText(s.tips);
							}
					} else {
						JPanel jp = new JPanel(new BorderLayout());
						Border border = BorderFactory.createTitledBorder(s.title); 
						jp.setBorder(border);   
						jp.add(getComponent(fld.get(obj), propSpecialFlds.get(ownerClass.getName() + "." + s.name)), BorderLayout.CENTER);
						p.add(jp, layout.getConstrains(0, row++, 3, 1, 11, 1));
					}
				} else if (s.type.startsWith("File:")) {
					String ss[] = s.type.split(":");
					String value = (String)fld.get(obj);
					JTextField tf = new JTextField(value);
					listeners.add(new StringFieldListener(obj, s.name, value, tf));
					if (s.title != null) {
						JLabel lbl = new JLabel(s.title);
						lbl.setHorizontalAlignment(SwingConstants.RIGHT);
						p.add(lbl, layout.getConstrains(0, row, 1, 1, 1, 1));
						p.add(tf, layout.getConstrains(1, row, 1, 1, 9, 1));
					} else {
						p.add(tf, layout.getConstrains(0, row, 2, 1, 10, 1));
					}
					JButton btn = new JButton("...");
					btn.addActionListener(new FileActionListener(tf, ss));
					p.add(btn, layout.getConstrains(2, row++, 1, 1, 1, 1));
					if (s.tips != null) {
						tf.setToolTipText(s.tips);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return p;
	}
	public static interface ParameterCap {
		public String[][] getParameterDescs();
	}
	public static interface ParameterFilled {
		public void finished(Object obj);
	}
	/**  */
	public static interface ParameterConstrict {
		public String checkValid(Object obj);
	}
	
	private static interface DataChangeListener {
		public void dataChanged();
	}
	
	private class StringFieldListener implements DataChangeListener {
		Object obj;
		String fldName;
		String oldValue;
		JTextField textFld;
		public StringFieldListener(Object obj, String fldName, String value, JTextField fld) {
			this.obj = obj;
			this.fldName = fldName;
			oldValue = value;
			textFld = fld;
		}
		public void dataChanged() {
			try {
				String s = textFld.getText();
				if (!s.equals(oldValue)) {
					Field fld = obj.getClass().getDeclaredField(fldName);
					fld.set(obj, s);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
	}
	private class NumberFieldListener implements DataChangeListener {
		Object obj;
		String fldName;
		String oldValue;
		JTextField textFld; 
		public NumberFieldListener(Object obj, String fldName, String oldValue, JTextField fld) {
			this.obj = obj;
			this.fldName = fldName;
			this.oldValue = oldValue;
			textFld = fld;
		}
		public void dataChanged() {
			try {
				String s = textFld.getText();
				if (!s.equals(oldValue)) {
					Field fld = obj.getClass().getDeclaredField(fldName);
					Class kls = fld.getType();
					if (kls == Integer.class || kls == int.class) {
						fld.setInt(obj, Integer.parseInt(s));
					} else if (kls == byte.class || kls == Byte.class) {
						fld.setByte(obj, Byte.parseByte(s));
					} else if (kls == short.class || kls == Short.class) {
						fld.setShort(obj, Short.parseShort(s));
					} else if (kls == Long.class || kls == long.class) {
						fld.setLong(obj, Long.parseLong(s));
					} else if (kls == double.class || kls == Double.class) {
						fld.setDouble(obj, Double.parseDouble(s));
					} else if (kls == Float.class || kls == float.class) {
						fld.setFloat(obj, Float.parseFloat(s));
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
	}
}
