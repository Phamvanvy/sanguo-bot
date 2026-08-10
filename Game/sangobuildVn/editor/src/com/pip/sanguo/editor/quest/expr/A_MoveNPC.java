package com.pip.sanguo.editor.quest.expr;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.pqe.Expr0;
import com.pip.sanguo.data.quest.pqe.Expression;
import com.pip.sanguo.data.quest.pqe.FunctionCall;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.data.quest.pqe.ParserConstants;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.LocationPropertyDescriptor;
import com.pip.sanguo.editor.property.NPCPropertyDescriptor;

/**
 * 表达式模板：强制传送NPC。
 * @author lighthu
 */
public class A_MoveNPC extends AbstractExpr {
	public int npcID;
	public int mapID;
	public int x;
	public int y;
	
	/**
	 * 构造指定全局变量的模板。
	 * @param name 全局变量名称
	 */
	public A_MoveNPC() {
		npcID = -1;
		mapID = -1;
		x = 0;
		y = 0;
	}
	
	/**
	 * 判断这个模板是一个条件还是一个动作。
	 */
	public boolean isCondition() {
		return false;
	}

	/**
	 * 用模板创建新的表达式片段。
	 */
	public IExpr createNew(QuestInfo qinfo) {
		return new A_MoveNPC();
	}

	/**
	 * 取得模板名称。
	 */
	public String getName() {
		return "传送NPC...";
	}

	/**
	 * 转换为自然语言表示。
	 */
	public String toNatureString() {
	    String targetName = GameMapInfo.locationToString(EditorApplication.getProj(), new int[] { mapID, x, y }, false);
		return "传送NPC " + GameMapNPC.toStringShort(EditorApplication.getProj(), npcID) + 
		    " 到 " + targetName;
	}
	
	/**
	 * 取得生成的表达式。
	 */
	public String getExpression() {
		return "MoveNPC(" + npcID + ", " + mapID + ", " + x + ", " + y + ")";
	}

	/**
	 * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
	 */
	public IExpr recognize(QuestInfo qinfo, Expression expr) {
		if (expr.getLeftExpr().type == Expr0.TYPE_FUNC && expr.getLeftExpr().getFunctionCall().funcName.equals("MoveNPC") && expr.getRightExpr() == null) {
			FunctionCall fc = expr.getLeftExpr().getFunctionCall();
			if (fc.getParamCount() != 4) {
				return null;
			}
			Expression param1 = fc.getParam(0);
			Expression param2 = fc.getParam(1);
			Expression param3 = fc.getParam(2);
			Expression param4 = fc.getParam(3);
			if (param1.getRightExpr() == null && param1.getLeftExpr().type == Expr0.TYPE_NUMBER &&
				param2.getRightExpr() == null && param2.getLeftExpr().type == Expr0.TYPE_NUMBER &&
				param2.getRightExpr() == null && param3.getLeftExpr().type == Expr0.TYPE_NUMBER &&
				param3.getRightExpr() == null && param4.getLeftExpr().type == Expr0.TYPE_NUMBER) {
				A_MoveNPC ret = (A_MoveNPC)createNew(qinfo);
				ret.npcID = PQEUtils.translateNumberConstant(param1.getLeftExpr().value);
				ret.mapID = PQEUtils.translateNumberConstant(param2.getLeftExpr().value);
				ret.x = PQEUtils.translateNumberConstant(param3.getLeftExpr().value);
				ret.y = PQEUtils.translateNumberConstant(param4.getLeftExpr().value);
				return ret;
			}
		}
		return null;
	}

	// 下面是IPropertySource接口的实现

	/**
	 * 取得属性描述符。这个模板有2个参数：字符串参数和整数参数。
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return new IPropertyDescriptor[] { 
				new NPCPropertyDescriptor("npcID", "目标NPC"),
                new LocationPropertyDescriptor("location", "目标位置")
		};
	}

	/**
	 * 取得属性当前值。
	 */
	public Object getPropertyValue(Object id) {
		if ("npcID".equals(id)) {
			return new Integer(npcID);
		} else if ("location".equals(id)) {
            return new int[] { mapID, x, y };
        }
		return null;
	}

	/**
	 * 设置属性当前值。
	 */
	public void setPropertyValue(Object id, Object value) {
		if ("npcID".equals(id)) {
			int newValue = ((Integer)value).intValue();
			if (newValue != npcID) {
				npcID = newValue;
				fireValueChanged();
			}
		} else if ("location".equals(id)) {
            int[] newValue = (int[])value;
            if (newValue[0] != mapID || newValue[1] != x || newValue[2] != y) {
                mapID = newValue[0];
                x = newValue[1];
                y = newValue[2];
                fireValueChanged();
            }
        }
	}
}
