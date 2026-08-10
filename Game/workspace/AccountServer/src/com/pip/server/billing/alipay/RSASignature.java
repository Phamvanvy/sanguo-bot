package com.pip.server.billing.alipay;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import org.apache.commons.codec.binary.Base64;


public class RSASignature implements Signable {

	public static final String SIGN_ALGORITHMS = "SHA1WithRSA";

	/**
	 * 签名
	 */
	public String sign(String content, String privateKey) throws Exception {

		PrivateKey prikey = KeyReader.getPrivateKeyFromPKCS8("RSA",
				new ByteArrayInputStream(privateKey.getBytes()));

		Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
		signature.initSign(prikey);
		signature.update(content.getBytes("utf-8"));

		byte[] signBytes = signature.sign();

		String sign = new String(Base64.encodeBase64(signBytes));

		return sign;
	}

	/**
	 * 验证签名 (non-Javadoc)
	 * 
	 * @see com.alipay.api.security.Signature#verify(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      com.alipay.api.enums.SignatureStyleEnum)
	 */
	public boolean verify(String content, String sign, String publicKey)
			throws Exception {

		PublicKey pubKey = KeyReader.getPublicKeyFromX509("RSA",
				new ByteArrayInputStream(publicKey.getBytes()));

		byte[] signed = Base64.decodeBase64(sign.getBytes());

		Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
		signature.initVerify(pubKey);
		signature.update(content.getBytes("utf-8"));

		boolean verify = signature.verify(signed);

		return verify;
	}
}
