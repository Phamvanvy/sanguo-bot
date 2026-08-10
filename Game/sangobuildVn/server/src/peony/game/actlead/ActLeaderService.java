package peony.game.actlead;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.Server;
import peony.service.Service;

/**
 * 活动指引服务
 * @author dchen
 */
public class ActLeaderService implements Service {

	private static final Logger log = Logger.getLogger(ActLeaderService.class);
	protected List<ActLeaderMap> maps = new ArrayList<ActLeaderMap>();
	protected List<ActLeader> acts = new ArrayList<ActLeader>(); //当前活动的活动指引项集合
	protected File file = null; //XML文件
	private int flag = 0;
//	private int[] arr = {754,755,756,757,762,763,764,765,770,771,772,773,803,804,805,806,807,808,809,810,811,812,816,817,818,819,820,821,822,823,824,825,830,831,832,833,834,835,836,838,837,839,840,879,880,957,958,959,960,585,2260,2261,2262,2224,2225,2226,1442,1703,1706,1707,1708,1709,1710,1711,1712,1713,1714,1715,1716,1718,1719,798,799,2106,2107,2108,794,2125,2126,2127,2128,2129,2130,2131,1721,1723,1726,1728,1731,1733,1736,1737,1742,1743,1744,1745,1746,1747,1748,1749,1870,1871,1872,1873,1874,1882,1884,1886,1888,1506,1508,1889,1890,792,50,51,52,53,1942,55,57,58,336,299,1037,1038,1039,1040,391,392,109,110,356,359,362,365,368,940,941,942,943,601,602,75,355,358,361,364,367,718,719,720,721,99,150,152,155,156,985,992,606,607,153,357,360,363,366,369,714,715,716,717,101,103,104,111,106,108,931,944,8,11,12,6,14,16,1032,1041,373,374,375,215,216,217,218,219,220,221,222,1781,224,226,227,337,338,491,492,493,496,498,500,397,339,340,912,913,914,915,916,917,918,919,920,921,113,114,115,117,120,121,122,932,933,934,935,125,127,128,129,131,132,134,136,137,936,937,938,939,170,171,533,534,535,536,537,174,175,176,177,1991,1992,1482,254,255,257,258,259,260,261,262,264,265,266,267,268,269,270,271,538,539,540,541,1981,1982,274,275,276,277,278,279,280,550,281,283,284,287,289,291,292,1977,1976,231,233,235,236,237,238,239,240,242,243,244,245,246,248,249,250,1986,1987,1012,1013,1014,1015,1016,1017,1018,1019,1020,1021,1022,23,24,27,28,29,30,31,1033,1034,1035,1036,33,34,35,37,38,39,41,42,43,46,354,476,477,478,479,480,482,484,486,1481,455,456,458,460,461,463,464,465,466,467,468,469,470,472,1996,1997,414,415,416,418,430,551,419,421,422,423,424,425,427,429,1966,962,963,964,965,986,966,967,968,969,970,975,976,140,139,142,144,146,977,978,979,980,523,524,525,526,527,528,516,517,518,520,433,434,435,436,438,441,442,444,445,448,450,451,431,1971,1972,400,401,402,403,404,406,409,410,411,1961,1962,1480,91,94,95,96,123,182,183,184,185,186,188,189,190,1956,1957,193,194,195,197,301,302,303,306,307,308,309,310,311,312,1951,1952,315,316,319,320,321,1003,322,324,325,326,327,328,330,331,88,98,199,384,385,386,1501,1801,1802,1803,1805,1806,1807,1808,1809,1000,1001,1004,1005,1810,1811,1812,1813,1814,1815,1816,1817,2113,2114,2115,2116,2117,2118,2119,2120,2121,1819,1821,1824,1826,1829,1831,1834,1835,1839,1755,1756,1910,1912,1915,1919,1918,1921,1046,1047,1048,1925,1926,1927,1928,1929,1930,1931,1932,1933,1934,1935,1936,1937,1006,1007,1008,1009,1938,1939,1940,1941,2006,2007,2008,2009,2010,2001,1869,1761,1763,1766,1768,1771,1773,1778,1779,1783,1784,1785,1786,1787,1788,1789,1790,1791,1792,1794,1795,1796,1797,1798,1901,1902,1903,1904,1906,1662,1663,1664,1665,1666,1667,1668,1669,1670,1671,1672,1675,1676,1681,2134,1656,1657,1658,1659,1509,1898,1899,1900,1895,1896,1897,1689,1692,1694,1225,1226,1227,1228,1229,1290,1291,1200,1201,1203,1204,1205,1206,1207,1208,1209,1210,1212,1213,1214,1215,1182,1183,1184,1185,648,649,650,651,652,653,654,645,646,655,553,554,555,560,561,562,564,565,566,1605,1606,1607,1608,1609,1610,1611,1612,1613,1614,1615,1616,1617,1618,1619,1620,1621,1622,1624,1626,1627,1628,1629,1630,1631,1633,1635,1636,1637,1638,1639,1640,1642,1644,1645,1646,1647,1648,1673,1559,1561,1562,1563,1564,1565,1566,1567,1568,1569,1570,1571,1572,1573,1574,2103,2104,2105,790,796,797,1575,2123,2124,1578,1579,1580,1581,1584,1585,1586,1587,1590,1591,1592,1593,1594,1595,1599,1513,1514,1515,1516,1517,1518,1519,1520,1521,1523,1524,1525,1526,1527,1529,1530,1531,1534,1535,1536,1540,1541,1545,1546,1548,1549,1550,1551,1141,1149,1152,735,736,2002,2003,2004,2005,1705,49,393,345,346,347,348,603,341,342,343,344,151,372,608,349,350,351,352,389,102,370,17,371,228,488,489,201,202,203,204,205,206,207,208,209,210,211,212,116,126,62,63,64,65,66,67,68,69,70,71,72,25,503,504,505,506,507,508,509,510,511,512,513,141,621,623,624,625,627,628,622,774,775};
	
	public void startup() throws Exception {
		file = new File(Server.server.getServiceRegistry().getDataService().data.baseDir,"actleader.xml");
		FileInputStream fis = new FileInputStream(file);
		Document doc = CommonUtil.getDocument(fis);
		loadXml(doc);
	}
	
//	private void findPathTest(){
//		for(ActLeader al : acts){
//			int mapId = al.mapId;
//			int x = al.x;
//			int y = al.y;
//			GameMapExit[] exites = Server.server.getServiceRegistry()
//			.getDataService().data.getPathFinder().findPath(
//			2, 240, 300, 300, mapId,x, y);
//			if (exites == null || exites.length == 0) {
//				System.out.println("寻路有问题："+al.name+"  mapId:"+al.mapId+"  x:"+al.x+"  y:"+al.y);
//			}
//		}
//	}
	
//	private boolean isInArr(int id){
//		for(int i : arr){
//			if(id==i)
//				return true;
//		}
//		return false;
//	}
	
//	protected void parseQuest(){
//		int count = 0;
//		Document doc = DocumentHelper.createDocument();
//		 Element root = doc.addElement("acts");
//		for(ASMQuest gq : ASMQuestUtil.quests.values()){
//			if(isInArr(gq.getId()))
//				continue;
//			String desc = gq.getDesc(null);
//			try {
//				String questName = gq.getGameQuest().getName();
////				String mapInfo = desc.substring(desc.indexOf("n>")+2, desc.indexOf("</n>"));
////				String mapName = mapInfo.substring(mapInfo.indexOf(",")+1, mapInfo.indexOf(":"));
////				int x = Integer.parseInt(mapInfo.substring(mapInfo.indexOf(":")+1, mapInfo.indexOf(",",mapInfo.indexOf(":")+1))) * 8;
////				int y = Integer.parseInt(mapInfo.substring(mapInfo.indexOf(",",mapInfo.indexOf(":"))+1, mapInfo.indexOf(")",mapInfo.indexOf(":")+1))) * 8;
////				int mapId = Integer.parseInt(desc.substring(desc.indexOf("n>")+2, desc.indexOf(",")))>>12;
//				int mapId = gq.getGameQuest().getStartNpc()>>12;
//				int npcId = gq.getGameQuest().getStartNpc();
//				int x = VMapUtil.getDefinition(mapId).mapInfo.findObject(npcId & 0xFFF).x;
//				int y = VMapUtil.getDefinition(mapId).mapInfo.findObject(npcId & 0xFFF).y;
//				int factionI = gq.getGameQuest().getFaction();
//				String faction = "";
//				if(factionI==1){
//					faction = "1";
//				}else if(factionI==2){
//					faction = "2";
//				}else if(factionI==3){
//					faction = "3";
//				}else if(factionI==5){
//					faction = "1,2,3";
//				}
//				String mapName = VMapUtil.getDefinition(mapId).mapInfo.findObject(npcId & 0xFFF).owner.name;
//				int minLevel = gq.getGameQuest().getLevel();
//				int maxLevel = gq.getGameQuest().getLevel()+5;
//				String rewardType = "";
//				StringBuffer sb = new StringBuffer();
//				for(QuestRewardBranch b : gq.getGameQuest().getReward().getBranchs().values()){
//					for(QuestRewardEntry e : b.getQuestRewardEntry()){
//						if(e instanceof MoneyRewardEntry){
//							sb.append("1,");
//						}else if(e instanceof ItemRewardEntry){
//							sb.append("2,");
//						}else if(e instanceof HonorRewardEntry){
//							sb.append("3,");
//						}else if(e instanceof CreditRewardEntry){
//							sb.append("4,");
//						}
//					}
//				}
//				sb.deleteCharAt(sb.length()-1);
//				rewardType = sb.toString();
//		       
//		        Element actEl = root.addElement("act");
//	        	actEl.addAttribute("type", "2");
//	        	actEl.addAttribute("name", questName);
//	        	actEl.addAttribute("createTime", "2010-12-1");
//	        	actEl.addAttribute("mapName", mapName);
//	        	actEl.addAttribute("mapId", String.valueOf(mapId));
//	        	actEl.addAttribute("x", String.valueOf(x));
//	        	actEl.addAttribute("y", String.valueOf(y));
//	        	actEl.addAttribute("rewardType", rewardType);
//	        	actEl.addAttribute("minLevel", String.valueOf(minLevel));
//	        	actEl.addAttribute("maxLevel", String.valueOf(maxLevel));
//	        	actEl.addAttribute("faction", faction);
//	        	actEl.addAttribute("instruction", "");
//		        
//			} catch (Exception e) {
//				System.out.println(gq.getId()+"     "+desc);
//			}
//			count++;
//		}
//		try {
//			CommonUtil.saveDocument(doc, new FileWriter(file));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	@SuppressWarnings("unchecked")
	protected void loadXml(Document doc){
		Element root = doc.getRootElement();
		List<Element> mapL = root.elements("map");
		for(Element el : mapL){
			int mapId = Integer.parseInt(el.attributeValue("mapId"));
			String mapName = el.attributeValue("mapName");
			int x = Integer.parseInt(el.attributeValue("x"));
			int y = Integer.parseInt(el.attributeValue("y"));
			int minLevel = Integer.parseInt(el.attributeValue("minLevel"));
			int maxLevel = Integer.parseInt(el.attributeValue("maxLevel"));
			String faction = el.attributeValue("faction");
			ActLeaderMap map;
			try {
				map = new ActLeaderMap(mapId, mapName, x, y, minLevel, maxLevel, faction);
				maps.add(map);
			} catch (Exception e) {
				log.error(e, e);
			}
		}
		List<Element> list = root.elements("act");
		for(Element el : list){
			String type = el.attributeValue("type");
			String name = el.attributeValue("name");
			String createTime = el.attributeValue("createTime");
			String mapName = el.attributeValue("mapName");
			int mapId = Integer.parseInt(el.attributeValue("mapId"));
			int x = Integer.parseInt(el.attributeValue("x"));
			int y = Integer.parseInt(el.attributeValue("y"));
			String rewardType = el.attributeValue("rewardType");
			int minLevel = Integer.parseInt(el.attributeValue("minLevel"));
			int maxLevel = Integer.parseInt(el.attributeValue("maxLevel"));
			String faction = el.attributeValue("faction");
			String instruction = el.attributeValue("instruction");
			ActLeader actLeader = null;
			try {
				actLeader = new ActLeader(type, name, mapName, createTime, mapId, x, y, 
						rewardType, minLevel, maxLevel, instruction, faction);
			} catch (Exception e) {
				log.error(e, e);
			}
			acts.add(actLeader);
		}
	}
	
	/** 存储到XML */
	protected void saveXml() throws IOException{
		Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("acts");
        for(ActLeaderMap map : maps){
        	Element actEl = root.addElement("map");
        	actEl.addAttribute("mapName", map.mapName);
        	actEl.addAttribute("mapId", String.valueOf(map.mapId));
        	actEl.addAttribute("x", String.valueOf(map.x));
        	actEl.addAttribute("y", String.valueOf(map.y));
        	actEl.addAttribute("minLevel", String.valueOf(map.minLevel));
        	actEl.addAttribute("maxLevel", String.valueOf(map.maxLevel));
        	actEl.addAttribute("faction", map.faction);
        }
        for(ActLeader actLeader : acts){
        	Element actEl = root.addElement("act");
        	actEl.addAttribute("type", String.valueOf(actLeader.type));
        	actEl.addAttribute("name", actLeader.name);
        	actEl.addAttribute("createTime", actLeader.createTime);
        	actEl.addAttribute("mapName", actLeader.mapName);
        	actEl.addAttribute("mapId", String.valueOf(actLeader.mapId));
        	actEl.addAttribute("x", String.valueOf(actLeader.x));
        	actEl.addAttribute("y", String.valueOf(actLeader.y));
        	actEl.addAttribute("rewardType", String.valueOf(actLeader.rewardType));
        	actEl.addAttribute("minLevel", String.valueOf(actLeader.minLevel));
        	actEl.addAttribute("maxLevel", String.valueOf(actLeader.maxLevel));
        	actEl.addAttribute("faction", actLeader.faction);
        	actEl.addAttribute("instruction", actLeader.instruction);
        }
        CommonUtil.saveDocument(doc, new FileWriter(file));
	}
	
	/** 添加活动指引项 */
	public synchronized void addAct(String type, String name, String createTime, String mapName, 
			int mapId, int x, int y, String rewardType, int minLevel, 
			int maxLevel, String instruction, String faction) throws Exception{
		ActLeader actLeader = new ActLeader(type, name, mapName, createTime, mapId, x, y, 
				rewardType, minLevel, maxLevel, instruction, faction);
		if(isActiveAct(name))
			throw new ActLeaderException("Hoạt động đã tồn tại!");
		else{
			acts.add(actLeader);
		}
		flag = 1;
	}
	
	/** 移除活动指引项 */
	public synchronized void removeAct(String name) throws ActLeaderException{
		Iterator<ActLeader> it = acts.iterator();
		while(it.hasNext()){
			ActLeader actLeader = it.next();
			if(actLeader.name.equals(name)){
				it.remove();
				flag = 1;
				return;
			}
		}
		throw new ActLeaderException("Hoạt động không tồn tại!");
	}
	
	/** 判断是否存在的活动指引项 */
	public boolean isActiveAct(String name){
		for(ActLeader actLeader : acts){
			if(actLeader.name.equals(name))
				return true;
		}
		return false;
	}
	
	/**
	 * 根据指定参数获取对应的活动指引项
	 * @param timeType, 
	 * @param mapId, 地图ID
	 * @param type, 活动类型
	 * @param rewardType, 奖励类型
	 * @param level, 适用级别
	 * @param faction, 适用阵营
	 * @return
	 */
	public synchronized List<ActLeader> getActLeadersBy(int timeType, int mapId, int type, int rewardType, int level, int faction, int startPage, int pageCount){
		List<ActLeader> maps = new ArrayList<ActLeader>();
		int temp = 0;
		int startIndex = startPage * pageCount;
		int endIndex = startPage * pageCount + pageCount;
		for(ActLeader actLeader : acts){
			if(actLeader.inLevel(level) && actLeader.isInMap(mapId) && actLeader.belongType(type) 
					&& actLeader.belongRewardType(rewardType) && actLeader.isInTime(new Date(), timeType) 
					&& actLeader.isInFaction(faction)){
				if(temp>=startIndex && temp<endIndex){
					maps.add(actLeader);
					if(temp==endIndex-1)
						break;
				}
				temp++;
			}
		}
		return maps;
	}
	
	public synchronized List<ActLeaderMap> getActLeaderMaps(int playerLevel, int faction, int startPage, int pageCount){
		List<ActLeaderMap> list = new ArrayList<ActLeaderMap>();
		int temp = 0;
		int startIndex = startPage * pageCount;
		int endIndex = startPage * pageCount + pageCount;
		for(ActLeaderMap map : maps){
			if(map.inLevel(playerLevel) && map.isInFaction(faction)){
				if(temp>=startIndex && temp<endIndex){
					list.add(map);
					if(temp==endIndex-1)
						break;
				}
				temp++;
			}
		}
		return list;
	}
	
	public int getLevelByLevelType(int levelType, int playerLevel){
		int level = 0;
		if(levelType==ActLeader.LEVEL_TYPE_CURRENT){
			level = playerLevel;
		}else if(levelType==ActLeader.LEVEL_TYPE_MIN){
			level = (playerLevel-5<=0 ? 1 : playerLevel-5);
		}else if(levelType==ActLeader.LEVEL_TYPE_MAX){
			level = playerLevel+5;
		}
		return level;
	}
	
	public synchronized List<ActLeader> getAllActLeader(){
		return acts;
	}

	public void shutdown() {
		try {
			if(flag==1)
				saveXml();
		} catch (IOException e) {
			log.error(e, e);
		}
	}

}
