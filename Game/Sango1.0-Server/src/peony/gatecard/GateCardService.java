package peony.gatecard;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.log4j.Logger;

import peony.game.LogUtil;
import peony.game.Player;
import peony.game.Server;
import peony.service.Service;
import peony.service.account.AccountAsyncCall;
import sun.misc.BASE64Encoder;

import com.pip.net.message.gameaccount.AddBalanceMessage;

public class GateCardService implements Service {
	
//	protected int partner_id = 76; //≤‚ ‘”√
	protected int partner_id = 66;
	protected String localip = "210.221.99.54";
//	protected String private_key_password = "123456"; //≤‚ ‘”√
	protected String private_key_password = "fsfuhgc5is6t67uy4m8g";
	protected String key_file_name = "server.p12";
	protected String user_name = "dangnn";
//	protected String secret_key = "7w0dg55vym4mebhjoy4hhvb3rvljg4ug"; //≤‚ ‘”√
	protected String secret_key = "hgb9ni6rlnx1g8gevost4a1mavttra3b";
	protected Signature signature = null;
	protected BASE64Encoder base64encoder = new BASE64Encoder();
	protected PrivateKey privateKey = null;
	
	private static final Logger log = Logger.getLogger(GateCardService.class);
	
	protected int[][] chargerate = {
			{10000,40},
				 
			{20000,80},
				 
			{30000,120},
				 
			{40000,160},
				 
			{50000,200},
				 
			{60000,240},
				 
			{70000,280},
				 
			{80000,320},
				 
			{90000,360},
				 
			{100000,400},
				 
			{180000,720},
				 
			{200000,800},
				 
			{500000,2000},
				 
			{1000000,4000},	
	};

	public void shutdown() {

	}

	public void startup() throws Exception {
		try {
			localip = Server.server.getConfig().configurationAt("jetty").getString("ip");
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		try {
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(key_file_name);
			keyStore.load(fis, private_key_password.toCharArray());
			Enumeration en = keyStore.aliases();
			String alias = "";
			Vector vectaliases = new Vector();

			while (en.hasMoreElements()) {
				vectaliases.add(en.nextElement());
				String[] aliases = (String[]) (vectaliases.toArray(new String[0]));
				for (int i = 0; i < aliases.length; i++)
					if (keyStore.isKeyEntry(aliases[i])) {
						alias = aliases[i];
						break;
					}
			}
			X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
			privateKey = (PrivateKey) keyStore.getKey(alias, private_key_password
					.toCharArray());
			Provider p = keyStore.getProvider();
			signature = Signature.getInstance("SHA1withRSA", p);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	
	
	public void charge(Player player, int accountId,String serial,String pin,AccountAsyncCall call) throws GateCardChargeException{
//		if (true) {
//			throw new GateCardChargeException("not supported");
//		}
		try {
			user_name = player.getAccount().getName();
		} catch (Exception e1) {
		}
		log.info("[GATECARDTRY]ACC["+accountId+"]SERIAL["+serial+"]PIN["+pin+"]");
		String OriginData = user_name + serial + partner_id + pin + secret_key;
		byte[] bSignature;
		try {
			signature.initSign(privateKey);
			signature.update(OriginData.getBytes("UTF-8"));
			bSignature = signature.sign();
		} catch (Exception e) {
			throw new GateCardChargeException(e.getMessage());
		} 
		BASE64Encoder base64 = new BASE64Encoder();
		String signature = base64.encode(bSignature);

		log.info(signature);
		
		
		vn.gate.psp.SrvCard service = new vn.gate.psp.SrvCard();
		vn.gate.psp.SrvCardSoap port = service.getSrvCardSoap();
		vn.gate.psp.ArrayOfAnyType result = port.gateCardInput(user_name,
				serial, pin, partner_id, localip, signature);
		if("0".equals(result.getAnyType().get(0).toString())){
			float fValue = Float.parseFloat(result.getAnyType().get(2).toString());
			int value = Math.round(fValue);
			int imoney = getimoney(value);
			if(imoney == 0) {
				log.info("[GATECARDFAIL]SERIAL["+serial+"]PIN["+pin
						+"]PARTNERID["+partner_id+"]PRICE["+imoney+"]"
						+LogUtil.getPlayerLogString(player)+"CAUSE[CHARGERATE]");
				throw new GateCardChargeException("");
			}
			addBalance(accountId,imoney,value,call,serial);
			log.info("[GATECARDOK]SERIAL["+serial+"]PIN["+pin+"]PARTNERID["
					+partner_id+"]"+LogUtil.getPlayerLogString(player)+"]AMOUNT["+imoney+"]");
		}else{
			log.info("[GATECARDFAIL]SERIAL["+serial+"]PIN["+pin+"]PARTNERID["
					+partner_id+"]"+LogUtil.getPlayerLogString(player)+"CAUSE["+result.getAnyType().get(1).toString()+"]");
			throw new GateCardChargeException(result.getAnyType().get(1).toString());
		}
	}
	
	public int vietNamWebcharge(String accountName,int accountId,String serial,String pin,AccountAsyncCall call) throws GateCardChargeException{
		try {
			user_name = accountName;
		} catch (Exception e1) {
		}
		log.info("[WEBGATECARDTRY]ACC["+accountName+"]SERIAL["+serial+"]PIN["+pin+"]");
		String OriginData = user_name + serial + partner_id + pin + secret_key;
		byte[] bSignature;
		try {
			signature.initSign(privateKey);
			signature.update(OriginData.getBytes("UTF-8"));
			bSignature = signature.sign();
		} catch (Exception e) {
			throw new GateCardChargeException(e.getMessage());
		} 
		BASE64Encoder base64 = new BASE64Encoder();
		String signature = base64.encode(bSignature);

		log.info(signature);
		
		
		vn.gate.psp.SrvCard service = new vn.gate.psp.SrvCard();
		vn.gate.psp.SrvCardSoap port = service.getSrvCardSoap();
		vn.gate.psp.ArrayOfAnyType result = port.gateCardInput(user_name,
				serial, pin, partner_id, localip, signature);
		if("0".equals(result.getAnyType().get(0).toString())){
			float fValue = Float.parseFloat(result.getAnyType().get(2).toString());
			int value = Math.round(fValue);
			int imoney = getimoney(value);
			if(imoney == 0) {
				log.info("[WEBGATECARDFAIL]SERIAL["+serial+"]PIN["+pin
						+"]PARTNERID["+partner_id+"]PRICE["+imoney+"]ACC["
						+accountName+"]CAUSE[CHARGERATE]");
				throw new GateCardChargeException("");
			}
			addBalance(accountId,imoney,value,call,serial);
			log.info("[WEBGATECARDOK]SERIAL["+serial+"]PIN["+pin+"]PARTNERID["
					+partner_id+"]ACC["+accountName+"]AMOUNT["+imoney+"]");
			return value;
		}else{
			log.info("[WEBGATECARDFAIL]SERIAL["+serial+"]PIN["+pin+"]PARTNERID["
					+partner_id+"]ACC["+accountName+"]CAUSE["+result.getAnyType().get(1).toString()+"]");
			throw new GateCardChargeException(result.getAnyType().get(1).toString());
		}
	}
	
	protected int getimoney(int value) {
		for(int i=0;i<chargerate.length;i++) {
			if(chargerate[i][0] == value) {
				return chargerate[i][1]*3600;
			}
		}
		return 0;
	}
	
	protected void addBalance(int accountId, int imoney, int money, AccountAsyncCall call, String cardNum) {
//		Server.server.getServiceRegistry().getAccountService().postMessage(new AddBalanceMessage(accountId,imoney,"gatecard"));
		Server.server.getServiceRegistry().getAccountService().sendAndRegister(new AddBalanceMessage(accountId,imoney,"gatecard_"+cardNum,"",money),call);
	}
	
}
