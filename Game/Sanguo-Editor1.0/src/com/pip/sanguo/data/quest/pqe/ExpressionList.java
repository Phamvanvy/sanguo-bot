package com.pip.sanguo.data.quest.pqe;

import java.io.StringReader;

import com.pip.sanguo.data.ProjectData;

/**
 * 表达式列表是一组表达式的集合，这些表达式之间用逗号隔开。
 * @author lighthu
 */
public class ExpressionList extends SimpleNode {
    public ExpressionList(int id) {
        super(id);
    }

    public ExpressionList(Parser p, int id) {
        super(p, id);
    }

    /**
     * 转换为原始字符串表示。
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < jjtGetNumChildren(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(jjtGetChild(i).toString());
        }
        return buf.toString();
    }

    /**
     * 转换为自然语言表示。
     */
    public String toNatureString() {
        if (jjtGetNumChildren() == 0) {
            return "无";
        }
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < jjtGetNumChildren(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(getExpr(i).toNatureString());
        }
        return buf.toString();
    }

    /**
     * 取得表达式数量。
     */
    public int getExprCount() {
        return jjtGetNumChildren();
    }

    /**
     * 取得指定位置的表达式。
     */
    public Expression getExpr(int index) {
        return (Expression) jjtGetChild(index);
    }

    /**
     * 删除指定位置的表达式。
     */
    public void deleteExpr(int index) {
        Node[] newarr = new Node[children.length - 1];
        System.arraycopy(children, 0, newarr, 0, index);
        System.arraycopy(children, index + 1, newarr, index, newarr.length - index);
        children = newarr;
    }

    /**
     * 在指定位置添加一个新的表达式。
     */
    public void addExpr(int index, Expression expr) {
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
     * 从字符串中解析出ExpressionList对象。
     */
    public static ExpressionList fromString(String str) {
        try {
            Parser t = new Parser(new StringReader(str));
            return t.Parse();
        } catch (Throwable e) {
             System.err.println(str);
             e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 把字符串的ExpressionList转换为自然语言。
     */
    public static String toNatureString(String str) {
        ExpressionList list = fromString(str);
        if (list == null) {
            return "语法错误";
        } else {
            return list.toNatureString();
        }
    }
    
    /**
     * 检查一个表达式是否存在语法错误或不可识别的变量、函数，函数的参数数量是否正确。
     * @exception 如果表达式无法编译，抛出异常，包含错误信息
     */
    public void checkSyntax(String[] localVars, boolean isCondition) throws PQEException {
        for (int i = 0; i < getExprCount(); i++) {
            int dataType = getExpr(i).checkSyntax(localVars);
            if (isCondition && dataType != 0) {
                throw new PQEException("作为条件的表达式必须返回整型结果：" + getExpr(i).toString());
            }
        }
    }
    
    /**
     * 检查一个表达式是否能够由客户端执行。本方法不检查表达式是否有效，首先必须通过
     * checkSyntax的检查才能调用此方法。
     */
    public boolean isClientSupport(String[] localVars) {
        for (int i = 0; i < getExprCount(); i++) {
            if (!getExpr(i).isClientSupport(localVars)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 搜索表达式中可能会修改的变量名字。
     * @param retList
     */
    public void searchAffectLocalVar(java.util.Set<String> retList) {
        for (int i = 0; i < getExprCount(); i++) {
            getExpr(i).searchAffectLocalVar(retList);
        }
    }
    
    /**
     * 搜索表达式中可能与之有交互的NPC。
     * @param retList
     */
    public void searchRelateNPC(java.util.Set<Integer> retList) {
        for (int i = 0; i < getExprCount(); i++) {
            getExpr(i).searchRelateNPC(retList);
        }
    }
    
    /**
     * 把表达式中用到的局部变量名转换为索引。
     */
    public void convertVarNameToIndex(String[] localVars) {
        for (int i = 0; i < getExprCount(); i++) {
            getExpr(i).convertVarNameToIndex(localVars);
        }
    }
    
    /**
     * 把表达式中用到的局部变量名转换为索引。
     */
    public static String convertVarNameToIndex(String str, String[] localVars) {
        ExpressionList exprList = ExpressionList.fromString(str);
        if (exprList != null) {
            exprList.convertVarNameToIndex(localVars);
            return exprList.toString();
        }
        return str;
    }
 
    /**
     * 计算表达式触发依赖的事件掩码。一个表达式列表中可能包含多个表达式，这些表达式的触发事件的交集
     * 就是表达式列表的触发事件。
     */
    public int getEventMask() {
        int mask = 0;
        for (int i = 0; i < getExprCount(); i++) {
            int m = getExpr(i).getEventMask();
            if (m == PQEUtils.EVENT_MASK_CYCLE) {
                return m;
            }
            mask |= m;
        }
        return mask;
    }
    
    /**
     * 把混合格式字符串中用到的NPC引用的场景地点引用更新一下。
     * @throws Exception
     */
    public void validateMixedText(ProjectData proj) throws Exception {
        for (int i = 0; i < getExprCount(); i++) {
            getExpr(i).validateMixedText(proj);
        }
    }
}
