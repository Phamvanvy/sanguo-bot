package peony.service;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.LogUtil;
import peony.game.Player;
import peony.game.Server;

/**
 * 日志记录玩家动作service
 * @author dchen
 */
public class LogPlayeActionService implements Service {

	//日志模板缓存
	protected Map<Integer, Map<Integer, ActionFormat>> formats = new HashMap<Integer, Map<Integer,ActionFormat>>();
	protected static String logPrefix;
	
	public void startup() throws Exception {
		loadFormat();
	}
	
	/** 加载日志模板 */
	protected void loadFormat() throws Exception{
		try {
			formats.clear();
			byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("action.xml");
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			parse(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		
		Element root = doc.getRootElement();
		logPrefix = root.attributeValue("prefix");
		
		List<Element> list = root.elements("action");
		if(list!=null && list.size()>0){
			for(Element element : list){
				int type = Integer.parseInt(element.attributeValue("type"));
				String log = element.attributeValue("log");
				List<Element> subList = element.elements("sub");
				if(subList!=null && subList.size()>0){
					for(Element subEl : subList){
						int subType = Integer.parseInt(subEl.attributeValue("type"));
						String subLog = subEl.attributeValue("log");
						insertFormat(type, log, subType, subLog);
					}
				}else{
					insertFormat(type, log);
				}
			}
		}
	}
	
	/** 插入没有子类型的日志模板 */
	protected void insertFormat(int type, String log){
		insertFormat(type, log, 0, "");
	}
	
	/** 插入拥有子类型的日志模板 */
	protected void insertFormat(int type, String log, int subType, String subLog){
		ActionFormat actionFormat = new ActionFormat(type, log, subType, subLog);
		Map<Integer, ActionFormat> subMap = formats.get(type);
		if(subMap==null){
			subMap = new HashMap<Integer, ActionFormat>();
			formats.put(type, subMap);
		}
		subMap.put(subType, actionFormat);
	}
	
	/** 记录动作日志 */
	public void logAction(Player player, int type, int subType){
		Map<Integer, ActionFormat> subAction = formats.get(type);
		if(subAction!=null){
			ActionFormat af = subAction.get(subType);
			if(af!=null){
				LogUtil.logAction(player, logPrefix, af.log, af.subLog);
			}
		}
	}
	
	public void shutdown() {
		
	}
	
	class ActionFormat{
		
		int type; //动作类型
		String log; //动作日志
		int subType; //动作子类型
		String subLog; //动作子类型日志

		public ActionFormat(int type, String log) {
			this.type = type;
			this.log = log;
		}

		public ActionFormat(int type, String log, int subType, String subLog) {
			this.type = type;
			this.log = log;
			this.subType = subType;
			this.subLog = subLog;
		}
	}

}
