package com.pip.server.account.util;



import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * Created by IntelliJ IDEA.
 * User: xuronghua
 * Date: 2008-6-11
 * Time: 13:13:01
 * Description:
 */
public class DES {
    private final String Algorithm = "DES";
    private KeyGenerator keygen;
    private SecretKey deskey;
    private Cipher c;
    private byte[] cipherByte;



    public DES() {
        init();
    }

    public void init() {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        try {
            keygen = KeyGenerator.getInstance(Algorithm);
            deskey = keygen.generateKey();
            c = Cipher.getInstance(Algorithm);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (NoSuchPaddingException ex) {
            ex.printStackTrace();
        }
    }



    public byte[] createEncryptor(String str) {
        try {
            c.init(Cipher.ENCRYPT_MODE, deskey);
            cipherByte = c.doFinal(str.getBytes());
        } catch (java.security.InvalidKeyException ex) {
            ex.printStackTrace();
        } catch (javax.crypto.BadPaddingException ex) {
            ex.printStackTrace();
        } catch (javax.crypto.IllegalBlockSizeException ex) {
            ex.printStackTrace();
        }
        return cipherByte;
    }



    public String createDecryptor(byte[] buff) {
        try {
            c.init(Cipher.DECRYPT_MODE, deskey);
            cipherByte = c.doFinal(buff);
        } catch (java.security.InvalidKeyException ex) {
            ex.printStackTrace();
        } catch (javax.crypto.BadPaddingException ex) {
            ex.printStackTrace();
        } catch (javax.crypto.IllegalBlockSizeException ex) {
            ex.printStackTrace();
        }
        return (new String(cipherByte));
    }



    public static String encode(String str, String key) throws Exception {
        SecureRandom sr = new SecureRandom();
        byte[] rawKey = Base64.decode(key);

        DESKeySpec dks = new DESKeySpec(rawKey);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(dks);

        javax.crypto.Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, sr);

        byte data[] = str.getBytes("UTF8");
        byte encryptedData[] = cipher.doFinal(data);
        return new String(Base64.encode(encryptedData));
    }



    public static String decode(String str, String key) throws Exception {
        SecureRandom sr = new SecureRandom();
        byte[] rawKey = Base64.decode(key);
        DESKeySpec dks = new DESKeySpec(rawKey);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, sr);
        byte encryptedData[] = Base64.decode(str);
        byte decryptedData[] = cipher.doFinal(encryptedData);
        return new String(decryptedData, "UTF8");
    }



    public static String generatorDESKey() throws NoSuchAlgorithmException {
        KeyGenerator keygen = KeyGenerator.getInstance("DES");
        SecretKey DESKey = keygen.generateKey();
        return new String(DESKey.getEncoded());

    }


    public static void main(String args[]) {

        String result = null;
        try {
            result = generatorDESKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        System.out.println("result = " + result);
    }
}
