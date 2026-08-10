package peony.vtc.charge;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.axis2.transport.http.HTTPConstants;
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
import rechargeswsdl.Cardinfo;
import rechargeswsdl.RechargesStub;
import rechargeswsdl.Result;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import vtccardwsdl.Vtccard;
import vtccardwsdl.VtccardResponse;

/**
 * Ô½ÄÏVTC¿¨³äÖµ
 * @author dchen
 * Cardid(Seri)                      Cardcode   
 */
public class VtcCardChargeService implements Service {
	
	public static String PARTNER_KEY = "trung886640";
	public static int PARTNER_ID = 886640;
	public static String URL = "http://mcgame.vn/webservice/service.php?wsdl";
	
	protected int[][] chargerate = {
			{10000,40},
			{20000,80},
			{50000,200},
			{100000,400},
			{200000,800},
			{300000,1200},
			{500000,2000},
	};
	
	protected Map<Integer, String> resultTexts = new HashMap<Integer, String>();
	private static final Logger log = Logger.getLogger(VtcCardChargeService.class);
	
	public void startup() throws Exception {
		try {
			byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("vtcmessages.xml");
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
			String message = e.attributeValue("s");
			resultTexts.put(id, message);
		}
	}
	
	public String encrypt(String key, String data) throws Exception {
		Cipher cipher = Cipher.getInstance("TripleDES");
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(key.getBytes(), 0, key.length());
		String keymd5 = new BigInteger(1, md5.digest()).toString(16).substring(0, 24);
		SecretKeySpec keyspec = new SecretKeySpec(keymd5.getBytes(),"TripleDES");
		cipher.init(Cipher.ENCRYPT_MODE, keyspec);
		byte[] stringBytes = data.getBytes();
		byte[] raw = cipher.doFinal(stringBytes);
		BASE64Encoder encoder = new BASE64Encoder();
		String base64 = encoder.encode(raw);
		return base64;
	}

	public String decrypt(String key, String data) throws Exception {
		Cipher cipher = Cipher.getInstance("TripleDES");
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(key.getBytes(), 0, key.length());
		String keymd5 = new BigInteger(1, md5.digest()).toString(16).substring(0, 24);
		SecretKeySpec keyspec = new SecretKeySpec(keymd5.getBytes(),"TripleDES");
		cipher.init(Cipher.DECRYPT_MODE, keyspec);
		BASE64Decoder decoder = new BASE64Decoder();
		byte[] raw = decoder.decodeBuffer(data);
		byte[] stringBytes = cipher.doFinal(raw);
		String result = new String(stringBytes);
		return result;
	}
	
	public void vtccard(String CARDID, String CARDCODE, Player player, AccountAsyncCall call) throws VtcCardChargeException {
		if(player!=null){
			try{
				log.info("[VTNCHARGETRY]" + LogUtil.getPlayerLogString(player) + "CARDID[" + CARDID + "]CARDCODE[" + CARDCODE + "]");
				String PARTNERID = PARTNER_ID + "";
				String KEYSEED = PARTNER_KEY;
				String USERNAME = player.getAccount().getName();
				Vtccard vtccard = new Vtccard();
				String parnerid = encrypt(KEYSEED,PARTNERID);
				String cardid = encrypt(KEYSEED,CARDID);
				String cardcode = encrypt(KEYSEED,CARDCODE);
				String description = encrypt(KEYSEED,USERNAME);
				Cardinfo cardinfo = new Cardinfo();
				cardinfo.setParnerid(parnerid);
				cardinfo.setCardid(cardid);
				cardinfo.setCardcode(cardcode);
				cardinfo.setDescription(description);
				vtccard.setCardinfo(cardinfo);
				RechargesStub stub = new RechargesStub(URL);
				stub._getServiceClient().getOptions().setProperty(HTTPConstants.CHUNKED, false);
				VtccardResponse res = stub.vtccard(vtccard);
				Result result = res.get_return();
				int status = 0;			
				String result_text = "";
				int vtc_price = Integer.parseInt(decrypt(KEYSEED, result.getAmount()));
				status = result.getStatus();
				if(status>0){
					addBalance(player.accountId, getimoney(vtc_price), call, CARDCODE);
					log.info("[VTNCHARGESUCCEED]" + LogUtil.getPlayerLogString(player) + "CARDID[" + CARDID + "]CARDCODE[" + CARDCODE + "]AMOUNT[" + getimoney(vtc_price) +"]");
				}else{
					result_text = resultTexts.get(status);
					if(result_text==null || result_text.equals(""))
						result_text = resultTexts.get(-200);
					log.info("[VTNCHARGEFAIL]" + LogUtil.getPlayerLogString(player) + "CARDID[" + CARDID + "]CARDCODE[" + CARDCODE + "]CASE[" + result_text + "]");
					throw new VtcCardChargeException(result_text);
				}
			}catch(VtcCardChargeException e){
				throw new VtcCardChargeException(e.getMessage());
			}catch(Exception e){
				log.info("[VTNCHARGEFAIL]" + LogUtil.getPlayerLogString(player) + "CARDID[" + CARDID + "]CARDCODE[" + CARDCODE + "]CASE[" + e.getMessage() + "]");
				e.printStackTrace();
			}
		}
	}

	protected void addBalance(int accountId, int imoney, AccountAsyncCall call, String cardNum) {
		String msg = "vtccard_"+ cardNum;
		Server.server.getServiceRegistry().getAccountService().sendAndRegister(new AddBalanceMessage(accountId,imoney,"vtccard_"+ cardNum),call);
	}
	
	protected int getimoney(int value) {
		for(int i=0;i<chargerate.length;i++) {
			if(chargerate[i][0] == value) {
				return chargerate[i][1]*3600;
			}
		}
		return 0;
	}

	public void shutdown() {
		
	}

}
