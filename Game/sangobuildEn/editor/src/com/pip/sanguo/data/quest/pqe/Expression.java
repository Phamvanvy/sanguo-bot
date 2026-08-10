package com.pip.sanguo.data.quest.pqe;

import com.pip.sanguo.data.ProjectData;

/**
 * 表达式。
 * @author lighthu
 */
public class Expression extends SimpleNode {
    /**
     * 操作符。
     */
    public int op;

    public Expression(int id) {
        super(id);
    }

    public Expression(Parser p, int id) {
        super(p, id);
    }

    /**
     * 转换为原始字符串表示。
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(jjtGetChild(0).toString());
        if (jjtGetNumChildren() == 2) {
        	buf.append(" ");
        	buf.append(PQEUtils.op2str(op));
        	buf.append(" ");
            buf.append(getRightExpr().toString());
        }
        return buf.toString();
    }
    
    /**
     * 转换为自然语言表示。
     */
    public String toNatureString() {
        StringBuffer buf = new StringBuffer();
        buf.append(getLeftExpr().toNatureString());
        if (jjtGetNumChildren() == 2) {
        	buf.append(" ");
        	buf.append(PQEUtils.op2nstr(op));
        	buf.append(" ");
            buf.append(getRightExpr().toNatureString());
        }
        return buf.toString();
    }

    /**
     * 取得左边表达式。
     */
    public Expr0 getLeftExpr() {
        return (Expr0)jjtGetChild(0);
    }
    
    /**
     * 取得操作符。
     */
    public int getOp() {
        return op;
    }
    
    /**
     * 取得右边表达式。
     */
    public Expr0 getRightExpr() {
        return (Expr0)(this.jjtGetNumChildren() == 1 ? null : this.jjtGetChild(1));
    }
    
    /**
     * 设置左边表达式。
     */
    public void setLeftExpr(Expr0 expr) {
        if (children == null) {
            children = new Node[1];
        }
        expr.parent = this;
        children[0] = expr;
    }
    
    /**
     * 设置操作符。
     */
    public void setOp(int value) {
        op = value;
    }
    
    /**
     * 设置右边表达式。
     */
    public void setRightExpr(Expr0 expr) {
        expr.parent = this;
        if (children == null) {
            children = new Node[] { null, expr };
        } else if (children.length == 1) {
            Node oldChild = children[0];
            children = new Node[] { oldChild, expr };
        } else {
            children[1] = expr;
        }
    }

    /**
     * 检查一个表达式是否存在语法错误或不可识别的变量、函数，函数的参数数量是否正确。
     * @return 返回表达式数据类型
     * @exception 如果表达式无法编译，抛出异常，包含错误信息
     */
    public int checkSyntax(String[] localVars) throws PQEException {
        if (getRightExpr() == null) {
            return getLeftExpr().checkSyntax(localVars);
        } else {
            int type1 = getLeftExpr().checkSyntax(localVars);
            int type2 = getRightExpr().checkSyntax(localVars);
            if (type1 != 0 || type2 != 0) {
                throw new PQEException("只有整型表达式能用用于比较。");
            }
            return 0;
        }
    }

    /**
     * 检查一个表达式是否能够由客户端执行。本方法不检查表达式是否有效，首先必须通过
     * checkSyntax的检查才能调用此方法。
     */
    public boolean isClientSupport(String[] localVars) {
        if (getRightExpr() == null) {
            return getLeftExpr().isClientSupport(localVars);
        } else {
            return getLeftExpr().isClientSupport(localVars) && getRightExpr().isClientSupport(localVars);
        }
    }

    /**
     * 搜索表达式中可能会修改的变量名字。
     * @param retList
     */
    public void searchAffectLocalVar(java.util.Set<String> retList) {
        getLeftExpr().searchAffectLocalVar(retList);
        if (getRightExpr() != null) {
            getRightExpr().searchAffectLocalVar(retList);
        }
    }

    /**
     * 搜索表达式中可能与之有交互的NPC。
     * @param retList
     */
    public void searchRelateNPC(java.util.Set<Integer> retList) {
        getLeftExpr().searchRelateNPC(retList);
        if (getRightExpr() != null) {
            getRightExpr().searchRelateNPC(retList);
        }
    }
    
    /**
     * 把表达式中用到的局部变量名转换为索引。
     */
    public void convertVarNameToIndex(String[] localVars) {
        getLeftExpr().convertVarNameToIndex(localVars);
        if (getRightExpr() != null) {
            getRightExpr().convertVarNameToIndex(localVars);
        }
    }
    
    /**
     * 计算表达式触发依赖的事件掩码。如果这个条件依赖某个事件才有可能成立，那么表达式的事件掩码就是这个
     * 事件对应的掩码值。如果这个条件的成立可能不依赖于任何一个事件，那么它的掩码是EVENT_MASK_CYCLE，
     * 表示它每一个CYCLE都可能被触发。
     */
    public int getEventMask() {
        // 如果右值存在，我们无法判断这个条件什么时候可能成立，返回EVENT_MASK_CYCLE。
        if (getRightExpr() == null) {
            return getLeftExpr().getEventMask();
        } else {
            return PQEUtils.EVENT_MASK_CYCLE;
        }
    }
    
    /**
     * 生成实现表达式功能对应的GTL代码。
     */
    public String toGTL() {
        if (getRightExpr() != null) {
            return "(" + getLeftExpr().toGTL() + " " + PQEUtils.op2str(op) + " " + getRightExpr().toGTL() + ")";
        } else {
            return getLeftExpr().toGTL();
        }
    }

    /**
     * 把混合格式字符串中用到的NPC引用的场景地点引用更新一下。
     * @throws Exception
     */
    public void validateMixedText(ProjectData proj) throws Exception {
        if (getRightExpr() == null) {
             getLeftExpr().validateMixedText(proj);
        } else {
            getLeftExpr().validateMixedText(proj);
             getRightExpr().validateMixedText(proj);
        }
    }
}
