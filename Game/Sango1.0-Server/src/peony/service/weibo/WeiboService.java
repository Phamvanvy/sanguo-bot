package peony.service.weibo;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.pip.weibo.WeiBoSystem;
import com.pip.weibo.WeiboUser;
import peony.game.Actor;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.Packet;
import peony.service.Service;
import weibo4j.WeiboException;

public class WeiboService implements Service{
	
	Map<Integer,WeiboUser> users = new HashMap<Integer,WeiboUser>();
    
	//表情
	String[] characters = {peony.Messages.STRING_00573,peony.Messages.STRING_00574,peony.Messages.STRING_00575,peony.Messages.STRING_00576,peony.Messages.STRING_00577,peony.Messages.STRING_00578,peony.Messages.STRING_00579,peony.Messages.STRING_00580,peony.Messages.STRING_00581,peony.Messages.STRING_00582,peony.Messages.STRING_00583,peony.Messages.STRING_00584};
	
	public static String CONTENT = peony.Messages.STRING_01985;
	public static String CONTENT_IPHONE = "#明珠三国#最受欢迎智能平台免费手机网游。免费下载地址：http://itunes.apple.com/cn/app//id483077063?ls=1&mt=8. 请关注@明珠三国";
	
	public static String[] VERSIONNAME = {"JAVA","ANDROID","SYSBAIN","IPHONE"};
	
	public static String WEIBOHOST = "明珠三国";
	
	public void shutdown() {
		
	}

	public void startup() throws Exception {
		WeiBoSystem.init(false);
	}
	
	public void bindWeibo(Player p,String name,String password) throws WeiboException{
		if(p!=null){
			LogUtil.logBindWeiboTry(p,name);
			WeiboUser weiboUser = new WeiboUser(name,password);
			if(weiboUser!=null){
				if(weiboUser.needAuthorization()){
					try{
					   weiboUser.doAuthorization();
					   if(p.pool.getString(Player.PROPERTY_WEIBO_TOKEN).equals(weiboUser.getToken())){
							throw new WeiboException(peony.Messages.STRING_00585);
						} else {
							if(weiboUser.getToken()!=null){
								p.pool.setString(Player.PROPERTY_WEIBO_NAME, name);
								p.pool.setString(Player.PROPERTY_WEIBO_PASSWORD, password);
								p.pool.setString(Player.PROPERTY_WEIBO_TOKEN, weiboUser.getToken());
								p.pool.setString(Player.PROPERTY_WEIBO_TOKENSECRET, weiboUser.getTokenSecret());
								//记录绑定微博日志
								LogUtil.logBindWeibo(p, name);
							} else {
								throw new WeiboException(peony.Messages.STRING_00586);
							}
						}
					} catch (WeiboException e){
						throw new WeiboException(peony.Messages.STRING_00586);
					}
				}
			}
	    }
	}
	
	public void unBindWeibo(Player p) throws WeiboException {
		if(p!=null){
			if(p.pool.getString(Player.PROPERTY_WEIBO_TOKEN).equals("")){
				throw new WeiboException(peony.Messages.STRING_00587);
			} else{
				String name = p.pool.getString(Player.PROPERTY_WEIBO_NAME);
				p.pool.setString(Player.PROPERTY_WEIBO_TOKEN, "");
				p.pool.setString(Player.PROPERTY_WEIBO_TOKENSECRET, "");
				p.pool.setString(Player.PROPERTY_WEIBO_NAME, "");
				p.pool.setString(Player.PROPERTY_WEIBO_PASSWORD, "");
				p.pool.setInt(Player.PROPERTY_WEIBO_ACTIVE, 0);
				//记录解绑微博日志
				LogUtil.logUnBindWeibo(p, name);
			}
		}
	}
	
	public void loginWeibo(Player p,String name,String password)throws WeiboException{
		if(p!=null){
			WeiboUser weiboUser = new WeiboUser(name,password);
			if(weiboUser!=null){
				if(weiboUser.needAuthorization()){
					weiboUser.doAuthorization();
				}
				if(p.pool.getString(Player.PROPERTY_WEIBO_TOKEN).equals("")){
				    users.put(p.id, weiboUser);
				}
			}
		}
	}
	
	public void sendWeibo(Player p,String message,int type,int sourceId,int version)throws WeiboException{
		if(p!=null){
			LogUtil.logSendWeiboTry(p, p.pool.getString(Player.PROPERTY_WEIBO_NAME), message);
			if(message.contains("<") || message.contains("{")){
				message = getRetex(message);	
			}
			String extroMessage = getExtraMessage(p,type,sourceId);
			if(version<VERSIONNAME.length && VERSIONNAME[version].equals("IPHONE")){
				CONTENT = CONTENT_IPHONE;
			}
			if(p.pool.getString(Player.PROPERTY_WEIBO_TOKEN).equals("")){
				if(users!=null && users.size()>0){
					WeiboUser weiboUser = users.get(p.id);
					if(weiboUser!=null){
						weiboUser.sendWeibo(extroMessage+message+CONTENT);
						LogUtil.logSendWeibo(p, p.pool.getString(Player.PROPERTY_WEIBO_NAME), message);
					}
				}
			} else {
				WeiboUser weiboUser = new WeiboUser(p.pool.getString(Player.PROPERTY_WEIBO_NAME),p.pool.getString(Player.PROPERTY_WEIBO_PASSWORD));
				if(weiboUser!=null){
					weiboUser.setAuthorization(p.pool.getString(Player.PROPERTY_WEIBO_TOKEN), p.pool.getString(Player.PROPERTY_WEIBO_TOKENSECRET));
					weiboUser.sendWeibo(extroMessage+message+CONTENT);
					if(p.pool.getInt(Player.PROPERTY_WEIBO_ADDFRIENDSHIP, 0)==0){
						try{
						   weiboUser.createFriendshipByScreenName(WEIBOHOST);
						   p.pool.setInt(Player.PROPERTY_WEIBO_ADDFRIENDSHIP, 1);
						   LogUtil.logWeiboAddFriendShip(p, p.pool.getString(Player.PROPERTY_WEIBO_NAME), WEIBOHOST, "SUCCESS");
						} catch(Exception e){
						   LogUtil.logWeiboAddFriendShip(p, p.pool.getString(Player.PROPERTY_WEIBO_NAME), WEIBOHOST, "ALLREADY FOLLOWED");
						}
					}
					LogUtil.logSendWeibo(p, p.pool.getString(Player.PROPERTY_WEIBO_NAME), message);
				}
			}
		}
	}
	
	
	public void showWeiboUI(Player p,String message){
		if(p!=null){
			Packet pt = new Packet(OpCode.SHOW_WEIBO_SERVER);
			pt.put(p.pool.getString(Player.PROPERTY_WEIBO_TOKEN).equals("")?0:1);
			pt.putString(message);
			p.send(pt);
		}
	}
	
	public int getDitrict(){
		int dis = 1;
		try{
			String district = Server.server.gameCode;
			if(!district.equals("")){
				String[] temp = district.split("_");
				String num = temp[1];
				if(num.startsWith("0")){
					num = num.substring(1);
				}
				dis = Integer.parseInt(num);
			}
		} catch (Exception e){
			
		}
		return dis;
	}
	
	public String getRetex(String data){
		String strContent = data;
		try{
			String re = "\\<\\/*[[a-zA-Z]*[0-9]*[a-zA-Z]*]*\\>";
			Pattern pattern = Pattern.compile(re);
			Matcher matcher = pattern.matcher(strContent);
			strContent = matcher.replaceAll("");
			String re2 = "\\{\\#[[a-zA-Z]*[0-9]*[a-zA-Z]*]*,[0-9]*\\}";
			pattern = Pattern.compile(re2);
			matcher = pattern.matcher(strContent);
	    	boolean m = matcher.find();
	    	StringBuffer result = new StringBuffer();
	    	String replacement = "";
	    	while(m){
	    		String g = matcher.group();
	    		//处理表情
	    		if(g.startsWith("{#VarEmotionRes,")){
	    		    String[] str = g.split(",");
	    		    String in = str[1].substring(0, str[1].length()-1);
	    			replacement = characters[Integer.parseInt(in)];
	    		} else {
	    			replacement = "";
	    		}
	    		matcher.appendReplacement(result,replacement);
				m = matcher.find();
	    	}
	    	matcher.appendTail(result);
	    	return result.toString();
		} catch (Exception e){
			return strContent;
		}
	}
	
	protected String getExtraMessage(Player p,int type,int sourceId){
		String extroMessage = "";
		Player sourcePlayer = ObjectAccessor.getPlayer(sourceId);
		String sourceName = "";
		int ditract=1;
		if(sourcePlayer==null){
			Actor sourceActor = Server.server.getServiceRegistry().getActorCacheService().find(sourceId);
			if(sourceActor != null){
				sourceName = sourceActor.name;
				ditract = getDitrict();
			}
		} else {
			sourceName = sourcePlayer.name;
			ditract = getDitrict();
		}
		if(type == 0){//转发他人聊天
			extroMessage = MessageFormat.format(peony.Messages.STRING_01986,ditract,sourceName);
		} else if(type == 1){//转发对他人聊天
			extroMessage = MessageFormat.format(peony.Messages.STRING_01987,ditract,p.name,sourceName);
		} else if(type ==2){ //转发系统聊天
			extroMessage = MessageFormat.format(peony.Messages.STRING_01988,ditract);
		}
		return extroMessage;
	}
}
