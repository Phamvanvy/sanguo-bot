package pip.util.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

/**
 * 公共待办事宜窗口.
 */
public class TodoTable extends JTable {
	
	MyTableModel mdl = null;
	JPopupMenu jpop = new JPopupMenu();
	Todo focusingTodo = null;

	public TodoTable() {
		super(new MyTableModel());
		// 得到TableModel以便访问其数据
		mdl = (MyTableModel)getModel();
		setAutoCreateRowSorter(true);
		addMouseListener(new PopHelper());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 
	}
	public boolean isPaintingTitle() {
		return true;
	}
	private ActionListener todosActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			jpop.setVisible(false);
			if (focusingTodo != null) {
				String s = ((JMenuItem)e.getSource()).getText();
				if (UiRES.hideScreen.equals(s)) {
					mdl.todos.remove(focusingTodo);
					mdl.fireTableDataChanged();
				} else {
					for (String ss[] : focusingTodo.actions) {
						if (ss[0].equals(s)) {
							String command = ss[1];
							if (command.startsWith("!")) {
								command = command.substring(1);
								focusingTodo.server.processTodo(focusingTodo.id, command);
								removeTodo(focusingTodo.server, focusingTodo.id);
							} else {
								focusingTodo.server.processTodo(focusingTodo.id, command);
							}
							return;
						}
					}
				}
			}
		}
	};
	public class PopHelper implements MouseListener {
		public void mouseClicked(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3) {
				java.awt.Point p = e.getPoint();
		        int rowIndex = rowAtPoint(p);
		        if (rowIndex >= 0) { // 当点中没有条目的空间时，rowIndex取值-1 
			        rowIndex = convertRowIndexToModel(rowIndex);
			        if (rowIndex >= 0 && rowIndex < mdl.todos.size()) {
			        	
			        	focusingTodo = mdl.todos.get(rowIndex);
			        	jpop.removeAll();
			        	for (String s[] : focusingTodo.actions) {
			        		JMenuItem mi = new JMenuItem(s[0]);
			        		jpop.add(mi);
			        		mi.addActionListener(todosActionListener);
			        	}
		        		JMenuItem mi = new JMenuItem(UiRES.hideScreen);
		        		jpop.add(mi);
		        		mi.addActionListener(todosActionListener);
			        	
			        	jpop.show(TodoTable.this,  e.getX(), e.getY());
			        }
		        }
			}
		}
		public void mouseReleased(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
	}
	public static interface TodoProcessor {
		public void processTodo(int id, String parameters);
		public String getSourceName();
	}
	/** 待办事宜,结构化数据结构,没有功能 */
	public static class Todo {
		/** serverId */
		public Todo(TodoProcessor server, String title, int id, long time, String [][]params) {
			this.server = server;
			this.title = title;
			this.id = id;
			this.time = time;
			this.actions = params;
		}
		/** 唯一标识,用来删除管理 */
		public int id;
		/** 优先级,可以作为排序的标准,暂未实现 */
		public int priority; 
		/** 优先级,可以作为排序的标准,暂未实现 */
		public long time; 
		/**本待办事宜的处理引擎 */
		public TodoProcessor server = null;
		/** 简要描述,在短列表中显示的内容 */
		public String title = "123";
		/** 二维数组,action[i][0]为显示的弹出菜单选项,action[i][1]为选中弹出菜单时执行的命令 */
		public String actions[][];
	}
	public void addTodo(TodoProcessor server, int id, String title, String [][]params) {
		if (server != null && params != null && params.length > 0) {
			addTodo(new Todo(server, title, id, System.currentTimeMillis(), params));
		}
	}
	/** 增加一个待办事宜 */
	public void addTodo(Todo t) {
		mdl.todos.add(t);
		mdl.fireTableDataChanged();
	}
	/** 将一个待办事宜从列表中删除 */
	public void removeTodo(TodoProcessor server, int id) {
		Todo ins = null;
		for (Todo todo : mdl.todos) {
			if (todo.server == server && todo.id == id) {
				mdl.todos.remove(todo);
				mdl.fireTableDataChanged();
				break;
			}
		}
	}
	public void clearTodo(IGameForm gmForm){
		for(Iterator<Todo> it = mdl.todos.iterator();it.hasNext();){
			Todo todo = (Todo)it.next(); 
			if(todo.server.getSourceName().equalsIgnoreCase(gmForm.getSourceName())){
				it.remove();
			}
		}
	}
	
	public static class MyTableModel extends AbstractTableModel {
		Vector<Todo> todos = new Vector<Todo>();
		String columnNames[] = {UiRES.server, UiRES.todoIssues};
		
		public String getColumnName(int col) {
	        return columnNames[col].toString();
	    }
	    public int getRowCount() { 
	    	return todos.size(); 
	    }
	    public int getColumnCount() { 
	    	return columnNames.length; 
	    }
	    public Object getValueAt(int row, int col) {
	    	if (row < todos.size() && row >= 0) {
	    		Todo todo = todos.get(row);
	    		if (todo == null) {
	    			return null;
	    		}
	    		switch (col) {
	    		case 0:
	    			if (todo.server == null) {
	    				return null;
	    			}
	    			return todo.server.getSourceName();
	    		case 1:
	    			return todo.title;
	    		}
	    	}
	        return null;
	    }
	    public boolean isCellEditable(int row, int col) { 
	    	return false; 
	    }
	    public void setValueAt(Object value, int row, int col) {
	        todos.setElementAt((Todo)value, row);
	        fireTableCellUpdated(row, col);
	    } 
	}
}
