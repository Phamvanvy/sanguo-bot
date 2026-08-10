package com.pip.server.billing.alipay;

public class SecurityManagerImpl implements SecurityManager  {

	private Encrypt encrypt;
	private Signable signature;
	
	public String decrypt(String algoType, String content, String key)
			throws Exception {
		if (algoType.equals("RSA")) {
            //rsa解密
            encrypt = new RSAEncrypt();
        } 
		else {
            throw new Exception("本应用不支持的算法");
        }
        return encrypt.decrypt(content, key);
	}

	public String encrypt(String algoType, String content, String key)
			throws Exception {
		return null;
	}

	public String sign(String algoType, String content, String key)
			throws Exception {
		if (algoType.equals("RSA")) {
            //rsa解密
            signature = new RSASignature();
        }
		else {
            throw new Exception("本应用不支持的算法");
        }
        return signature.sign(content, key);
	}

	public boolean verify(String algoType, String content, String sign,
			String key) throws Exception {
		if (algoType.equals("RSA")) {
            //rsa解密
            signature = new RSASignature();
        } 
		else {
            throw new Exception("本应用不支持的算法");
        }
        return signature.verify(content, sign, key);
	}

}
