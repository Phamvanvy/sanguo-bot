package peony.mobiphone;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import com.pip.net.message.gameaccount.AddBalanceMessage;
import peony.game.CommonUtil;
import peony.game.LogUtil;
import peony.game.Player;
import peony.game.Server;
import peony.service.Service;
import peony.service.account.AccountAsyncCall;
import vdconline.library.StringUtil;
import vdconline.library.tripleDES;
import vn.vdconline.secondtelcoAPI.ws.ChargeResponse;
import vn.vdconline.secondtelcoAPI.ws.LoginResponse;
import vn.vdconline.secondtelcoAPI.ws.TelcoWebServiceProxy;

public class TelcoChargeService implements Service {

	public static final int TYPE_MOBI = 0;
	public static final int TYPE_VINA = 1;
	
	protected static int partnerID = 37;
	protected static String user="mcgames";
	protected static String pass="mcgame";
	protected static String Mpin="mcgame.vn";
	protected static String encodeMobi = ":VMS";
	protected static String encodeVina = ":VNP";
	
	protected HashMap<Integer, String> messages = new HashMap<Integer, String>();
	
	protected int[][] chargerate = {
			{10000,40},
				 
			{20000,80},
				 
			{30000,120},
				 
			{50000,200},
				 
			{100000,400},
				 
			{200000,800},
			
			{300000,1200},
				 
			{500000,2000}
	};
	
	private static final Logger log = Logger.getLogger(TelcoChargeService.class);
	
	public void startup() throws Exception {
		try {
			byte[] bytes = Server.server.getServiceRegistry().getDataService().data
					.findFile("telcomessage.xml");
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			parse(doc);
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		Element root = doc.getRootElement();
		List<Element> mess = root.elements("m");
		for(Element e : mess){
			int id = Integer.parseInt(e.attributeValue("id"));
			String message = e.attributeValue("m");
			messages.put(id, message);
		}
	}
	
	public void charge(Player player,int type,String cardCode,AccountAsyncCall call) throws MobilePhoneChargeException{
		if(player!=null){
			String cardNum = cardCode;
			log.info("[TELCOCARDTRY]"+LogUtil.getPlayerLogString(player)+"]TYPE["
					+getTypeString(type)+"]CARDCODE["+cardCode+"]");
			String userName = player.getAccount().getName();
			TelcoWebServiceProxy ws = new TelcoWebServiceProxy();		
			try {	
				//Encrypt password SHA
				  String enpass= StringUtil.encrypt(pass,"SHA");
				//Function login get sessionkey  
				  LoginResponse lr = ws.logIn(user, enpass, partnerID);	
				//Encoding mpin 
				  String mpin_encrypt = tripleDES.EncryptVMS(lr.getSessionid(), Mpin);

				//Parameters of the user load the card		
				  String mobile	= player.getAccount().getPhone(); // phone of user in game
				  if(mobile.equals(""))
					  mobile = "13260071135";
				  
				//Get cardcode + :VMS or VNP . 
				//If VINA then cardcode=cardcode + ":VNP"
				//If MOBI then cardcode=cardcode + ":VMS" 
				  if(cardCode.length()>12 && type==TYPE_MOBI)
					  cardCode = cardCode + encodeMobi;
				  else if(cardCode.length()<=12 || type==TYPE_VINA)
					  cardCode = cardCode + encodeVina;
				  else
					  cardCode = cardCode + encodeMobi;

				  String en_data = tripleDES.EncryptVMS(lr.getSessionid(), cardCode);
				//Function get price 
				  ChargeResponse chr = ws.cardCharge(user, partnerID, mpin_encrypt, en_data, userName, userName+"@mcgame.vn", mobile);
				  int status = chr.getStatus();
				  if(status==1){
					  //充值成功
					  float value = Float.parseFloat(tripleDES.DecryptVMS(lr.getSessionid(), chr.getDRemainAmount()));
					  int imoney = getimoney(Math.round(value));
					  addBalance(player.getAccount().getId(), imoney, call, cardNum);
					  log.info("[TELCOCARDDOK]"+LogUtil.getPlayerLogString(player)+"]TYPE["
							  +getTypeString(type)+"]AMOUNT["+imoney+"]CARDCODE["+cardCode+"]");
				  }else{
					  log.info("[TELCOCARDDFAIL]"+LogUtil.getPlayerLogString(player)+"]TYPE["
							  +getTypeString(type)+"]CARDCODE["+cardCode+"]STATUS["+getResponseMessage(status)+"]");
//					  throw new MobilePhoneChargeException(chr.getMessage());
					  throw new MobilePhoneChargeException(getResponseMessage(status));
				  }
			} catch (Exception e) {
//				log.error(e, e);
				if(e instanceof MobilePhoneChargeException)
					throw new MobilePhoneChargeException(e.getMessage());
				else
					throw new MobilePhoneChargeException("充值失败");
			}
		}
	}
	
	protected String getTypeString(int type){
		switch(type){
			case TYPE_MOBI:
				return "MOBI";
			case TYPE_VINA:
				return "VINA";
			default:
				return "";
		}
	}
	
	protected void addBalance(int accountId, int imoney, AccountAsyncCall call, String cardNum) {
		Server.server.getServiceRegistry().getAccountService().sendAndRegister(new AddBalanceMessage(accountId,imoney,"telco_"+cardNum),call);
	}
	
	protected int getimoney(int value) {
		for(int i=0;i<chargerate.length;i++) {
			if(chargerate[i][0] == value) {
				return chargerate[i][1]*3600;
			}
		}
		return 0;
	}
	
	public String getResponseMessage(int status){
		String mess = messages.get(status);
		if(mess==null || mess.equals(""))
			return messages.get(10);
		return mess;
	}

	
	public void shutdown() {
		
	}

}
