package com.pip.sanguo.editor.quest.expr;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.pqe.Expr0;
import com.pip.sanguo.data.quest.pqe.Expression;
import com.pip.sanguo.data.quest.pqe.FunctionCall;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.data.quest.pqe.ParserConstants;
import com.pip.sanguo.editor.property.OptionListPropertyDescriptor;
import com.pip.sanguo.editor.property.RichTextPropertyDescriptor;
import com.pip.sanguo.editor.quest.QuestFlowViewer;
import com.pip.sanguo.editor.quest.RichTextEditor;

/**
 * 表达式模板：显示提问。
 * @author lighthu
 */
public class A_Question extends AbstractNotifyAction {
	public String message;
	public String options;
	
	/**
	 * 构造指定全局变量的模板。
	 * @param name 全局变量名称
	 */
	public A_Question() {
		message = "";
		options = "";
		notifyID = 0;
	}
	
	/**
	 * 用模板创建新的表达式片段。
	 */
	public IExpr createNew(QuestInfo qinfo) {
		return new A_Question();
	}

	/**
	 * 取得模板名称。
	 */
	public String getName() {
		return "向玩家提问";
	}

	/**
	 * 转换为自然语言表示。
	 */
	public String toNatureString() {
		return "提问：" + message + "\n" + options;
	}
	
	/**
	 * 取得生成的表达式。
	 */
	public String getExpression() {
		return "Question(\"" + PQEUtils.reverseConv(message) + "\", \"" + PQEUtils.reverseConv(options) + "\", " + notifyID + ")";
	}

	/**
	 * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
	 */
	public IExpr recognize(QuestInfo qinfo, Expression expr) {
		if (expr.getLeftExpr().type == Expr0.TYPE_FUNC && expr.getLeftExpr().getFunctionCall().funcName.equals("Question") && expr.getRightExpr() == null) {
			FunctionCall fc = expr.getLeftExpr().getFunctionCall();
			if (fc.getParamCount() != 3) {
				return null;
			}
			Expression param1 = fc.getParam(0);
			Expression param2 = fc.getParam(1);
			Expression param3 = fc.getParam(2);
			if (param1.getRightExpr() == null && param1.getLeftExpr().type == Expr0.TYPE_STRING &&
				param2.getRightExpr() == null && param2.getLeftExpr().type == Expr0.TYPE_STRING &&
				param3.getRightExpr() == null && param3.getLeftExpr().type == Expr0.TYPE_NUMBER) {
				A_Question ret = (A_Question)createNew(qinfo);
				ret.message = PQEUtils.translateStringConstant(param1.getLeftExpr().value);
				ret.options = PQEUtils.translateStringConstant(param2.getLeftExpr().value);
				ret.notifyID = PQEUtils.translateNumberConstant(param3.getLeftExpr().value);
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
				new RichTextPropertyDescriptor("message", "消息", questInfo),
				new OptionListPropertyDescriptor("options", "选项"),
				new ComboBoxPropertyDescriptor("notify", "是否通知", new String[] { "是", "否" })
		};
	}

	/**
	 * 取得属性当前值。
	 */
	public Object getPropertyValue(Object id) {
		if ("options".equals(id)) {
			return options;
		} else if ("message".equals(id)) {
			return message;
		} else if ("notify".equals(id)) {
            return new Integer(notifyID != -1 ? 0 : 1);
        }
		return null;
	}

	/**
	 * 设置属性当前值。
	 */
	public void setPropertyValue(Object id, Object value) {
		if ("options".equals(id)) {
			String newValue = (String)value;
			if (!newValue.equals(options)) {
				options = newValue;
				fireValueChanged();
			}
		} else if ("message".equals(id)) {
			String newValue = (String)value;
			if (!newValue.equals(message)) {
				message = newValue;
				fireValueChanged();
			}
		} else if ("notify".equals(id)) {
            int newValue = ((Integer)value).intValue();
            if (newValue == 0) {
                newValue = 0;
            } else {
                newValue = -1;
            }
            if (newValue != notifyID) {
                notifyID = newValue;
                fireValueChanged();
            }
        }
	}
}
