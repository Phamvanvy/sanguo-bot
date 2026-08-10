package com.pip.gtl.remotedebugger;

public class VariableItem implements Comparable {
	public static final int TYPE_PARAM = 0;
	public static final int TYPE_LOCAL = 1;
	public static final int TYPE_GLOBAL = 2;
	public static final int TYPE_MEMBER = 3;
	public static final int TYPE_ARRAYMEMBER = 4;
	
	public VariableItem parent;    // 如果是成员变量或数组成员，这里指向父对象
	public int variableType;       // 变量类型：0 - 全局变量, 1 - 参数, 2 - 局部变量, 3 - 成员变量, 4 - 数组成员
    public int type;               // 变量数据类型
    public String name;            // 变量名称
    public String typeName;        // 如果type是4或者20，这里保存结构体名称，""表示非结构体
    public int address;            // 如果是全局变量、局部变量或者参数，这里存储变量地址；如果是成员变量，这里存储成员
                                   // 变量在结构体中的地址；如果是数组成员，这里存储数组下标。
    
    public String toString() {
    	return name;
    }
    
    public int compareTo(Object o) {
    	if (o == null) {
    		return 1;
    	}
    	if (!(o instanceof VariableItem)) {
    		return -1;
    	}
    	VariableItem oo = (VariableItem)o;
    	if (variableType < oo.variableType) {
    		return -1;
    	} else if (variableType > oo.variableType) {
    		return 1;
    	}
    	return name.compareTo(oo.name);
    }
}
