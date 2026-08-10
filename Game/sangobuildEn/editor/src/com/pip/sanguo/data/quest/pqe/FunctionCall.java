package com.pip.sanguo.data.quest.pqe;

import java.util.HashMap;
import java.text.*;

/**
 * 函数调用。
 * @author lighthu
 */
public class FunctionCall extends SimpleNode {
    /**
     * 函数名。
     */
    public String funcName;
    
    public FunctionCall(int id) {
        super(id);
    }

    public FunctionCall(Parser p, int id) {
        super(p, id);
    }

    /**
     * 转换为原始字符串表示。
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(funcName);
        buf.append("(");
        for (int i = 0; i < jjtGetNumChildren(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(jjtGetChild(i).toString());
        }
        buf.append(")");
        return buf.toString();
    }
    
    /**
     * 转换为自然语言表示。
     */
    public String toNatureString() {
        try {
        	String desc;
        	if (PQEUtils.SYSTEM_FUNCS_MAP.containsKey(funcName)) {
        	    desc = PQEUtils.SYSTEM_FUNCS_MAP.get(funcName).description;
        	} else {
        	    desc = "未知函数" + funcName;
        	}
        	Object[] params = new Object[getParamCount()];
        	for (int i = 0; i < params.length; i++) {
        		params[i] = getParam(i).toNatureString();
        	}
        	return MessageFormat.format(desc, params);
        } catch (Exception e) {
        	return "语法错误";
        }
    }

    /**
     * 取得参数数量。
     */
    public int getParamCount() {
        return jjtGetNumChildren();
    }
    
    /**
     * 取得指定位置的参数。
     */
    public Expression getParam(int index) {
        return (Expression)jjtGetChild(index);
    }
    
    /**
     * 删除指定位置的参数。
     */
    public void deleteParam(int index) {
        Node[] newarr = new Node[children.length - 1];
        System.arraycopy(children, 0, newarr, 0, index);
        System.arraycopy(children, index + 1, newarr, index, newarr.length - index);
        children = newarr;
    }
    
    /**
     * 在指定位置添加一个新的参数。
     */
    public void addParam(int index, Expression expr) {
        if (index < 0 || index > children.length) {
            index = children.length;
        }
        Node[] newarr = new Node[children.length + 1];
        System.arraycopy(children, 0, newarr, 0, index);
        expr.parent = this;
        newarr[index] = expr;
        System.arraycopy(children, index, newarr, index + 1, children.length - index);
        children = newarr;
    }
    
    /**
     * 生成实现表达式功能对应的GTL代码。
     */
    public String toGTL() {
        // Set, Inc, Dec函数需要处理成普通的变量访问语句
        if ("Set".equals(funcName) || "Inc".equals(funcName) || "Dec".equals(funcName)) {
            String varName = PQEUtils.translateStringConstant(getParam(0).getLeftExpr().value);
            String rightValue = getParam(1).toGTL();
            String op = "";
            if ("Set".equals(funcName)) {
                op = " = ";
            } else if ("Inc".equals(funcName)) {
                op = " += ";
            } else if ("Dec".equals(funcName)) {
                op = " -= ";
            }
            return varName + op + rightValue;
        }
        
        // Random函数可直接实现
        if ("Random".equals(funcName)) {
            return "Random()";
        }
        
        // If函数需要特殊处理一下，转换为一条if语句
        if ("If".equals(funcName)) {
            String param1 = getParam(1).toGTL();
            String param2 = getParam(2).toGTL();
            if ("1".equals(param1) || "0".equals(param1)) {
                param1 = "";
            } else {
                param1 += ";";
            }
            if ("1".equals(param2) || "0".equals(param2)) {
                param2 = "";
            } else {
                param2 += ";";
            }
            return "if (" + getParam(0).toGTL() + ") { " + param1 + " } else { " + 
                param2 + " }";
        }
        
        // 其他函数在前面加上PQE_前缀即可。
        StringBuffer buf = new StringBuffer();
        buf.append("PQE_");
        buf.append(funcName);
        buf.append("(");
        for (int i = 0; i < getParamCount(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(getParam(i).toGTL());
        }
        buf.append(")");
        return buf.toString();
    }
}
