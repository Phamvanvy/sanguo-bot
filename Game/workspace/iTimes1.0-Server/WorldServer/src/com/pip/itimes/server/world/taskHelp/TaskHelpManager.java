package com.pip.itimes.server.world.taskHelp;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author wpjiang 管理要生成脚本ui辅助和关联的类
 */
public class TaskHelpManager {

	/**
	 * 用于存放ui演示的按键键值
	 */
	public static  Map taskHelpMap = new HashMap();

	public void loadTaskHelp(File file) throws DocumentException {
		taskHelpMap.clear();
		String stageDirName = file.getAbsolutePath();
		String dirName = FilenameUtils.concat(stageDirName,
				"Tasks/taskHelp.xml");
		SAXReader reader = new SAXReader();
		Document doc = reader.read(dirName);
		Element root = doc.getRootElement();
		for (Iterator i = root.elementIterator("TaskHelp"); i.hasNext();) {
			Element el = (Element) i.next();

			String uiStringName = el.attributeValue("UiString");
			TaskHelp taskHelp = new TaskHelp();
			taskHelp.setUiName(uiStringName);

			short taskId = Short.parseShort(el.attributeValue("taskId"));
			taskHelp.setTaskId(taskId);

			short taskRewardId = Short.parseShort(el
					.attributeValue("taskRewardId"));
			taskHelp.setTaskRewardId(taskRewardId);
			
			int fastKey = Integer.parseInt(el.attributeValue("fastKey"));
			taskHelp.setFastKey(fastKey);
			
			String showFastKeyMessage = el.attributeValue("FastKeyMessage");
			
			int showFastKeyTime = Integer.parseInt(el.attributeValue("FastKeyTime"));
			
			int level = Integer.parseInt(el.attributeValue("showLevel"));
			taskHelp.setHelpLevel(level);
			//读取非快捷键下的所有键值，提示，还有时间长度
			Vector showKeyVector = new Vector();
			Vector showMessageVector = new Vector();
			Vector showKeyTimeVector = new Vector();
			
			for (Iterator j = el.elementIterator("ShowKey"); j.hasNext();) {
				Element node = (Element)j.next();
				Attribute keyAttribute = node.attribute("key");
				int key = Integer.parseInt(keyAttribute.getValue());
				showKeyVector.add(key);
			}
			
			for (Iterator j = el.elementIterator("ShowMessage"); j.hasNext();) {
				Element node = (Element)j.next();
				Attribute messageAttribute = node.attribute("message");
				String message = messageAttribute.getValue();
				showMessageVector.add(message);
			}
			
			for (Iterator j = el.elementIterator("ShowKeyTime"); j.hasNext();) {
				Element node = (Element)j.next();
				Attribute keyTimeAttribute = node.attribute("time");
				int keyTime = Integer.parseInt(keyTimeAttribute.getValue());
				showKeyTimeVector.add(keyTime);
			}
			
			//if(fastKey != -1){//存在快捷键，强制将按键和提示放在第一个
			showKeyVector.insertElementAt(fastKey, 0);
			showMessageVector.insertElementAt(showFastKeyMessage, 0);
			showKeyTimeVector.insertElementAt(showFastKeyTime, 0);
			//}
			taskHelp.setKeyVector(showKeyVector);
			taskHelp.setUiWaitMessage(showMessageVector);
			taskHelp.setKeyTimeVector(showKeyTimeVector);
			taskHelpMap.put(taskId, taskHelp);	
		}
	}
}
