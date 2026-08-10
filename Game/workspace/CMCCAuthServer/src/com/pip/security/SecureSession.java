package com.pip.security;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates encryption/decryption functions required by a
 * secure session. The algorithms include:
 *   session key exchange: RSA with PKCS1 padding;
 *   data exchange: Triple DES in CBC mode with PKCS5 padding and zero
 *                  initial vector;
 *   message digest: MD5.
 */
public class SecureSession {
    // The RSA private key of the server.
    protected RSAPrivateKey privateKey;
    // Session key.
    protected RawSecretKey sessionKey;

    /**
     * Create a new session.
     * @param pkf      The path name of private key file
     * @param keyData  The session key encrypted by the other party using public key.
     * @throws SecureSessionException If the key file or key data is invalid.
     */
    public SecureSession(String pkf, byte[] keyData) throws SecureSessionException {
        try {
            privateKey = new RSAPrivateKey(readFile(pkf));
        } catch (Exception e) {
            throw new SecureSessionException("Loading private key failed.");
        }
        try {
            decryptSessionKey(keyData);
        } catch (Exception e) {
            throw new SecureSessionException("Invalid session key.");
        }
    }

    /**
     * Encrypt a data buffer using session key. This method add timestamp and
     * MD5 digest after the orignial data.
     * @param inbuf The input buffer.
     * @return The output buffer.
     * @throws SecureSessionException If encryption failed.
     */
    public byte[] encryptData(byte[] inbuf) throws SecureSessionException {
        try {
            // Generate MD5 digest.

            byte[] rawDataDigest = new byte[inbuf.length + 16 + 4];
            System.arraycopy(inbuf, 0, rawDataDigest, 0, inbuf.length);
            intToBytes((int)(System.currentTimeMillis() / 1000), rawDataDigest, inbuf.length);
            MD5 md = new MD5();
            md.update(rawDataDigest, 0, inbuf.length + 4);
            byte[] digest = md.digest();
            System.arraycopy(digest, 0, rawDataDigest, inbuf.length + 4, 16);

            // Encrypt the data using session key.

            TripleDES_CBC_PKCS5 des3 = new TripleDES_CBC_PKCS5();
            des3.init(DES_CBC_PKCS5.ENCRYPT_MODE, sessionKey);
            return des3.doFinal(rawDataDigest, 0, rawDataDigest.length);
        } catch (Exception e) {
            throw new SecureSessionException("Encryption fail.");
        }
    }

    /**
     * Decrypt a data buffer using session key. The method checks the MD5 digest
     * and time stamp of the data.
     * @param inbuf The input buffer.
     * @return The output buffer.
     * @throws SecureSessionException If the input data is invalid.
     */
    public byte[] decryptData(byte[] inbuf) throws SecureSessionException {
        try {
            TripleDES_CBC_PKCS5 des3 = new TripleDES_CBC_PKCS5();
            des3.init(DES_CBC_PKCS5.DECRYPT_MODE, sessionKey);
            byte[] decData = des3.doFinal(inbuf, 0, inbuf.length);

            // Check MD5 digest of the data.

            MD5 md = new MD5();
            md.update(decData, 0, decData.length - 16);
            byte[] digest = md.digest();
            byte[] oldDigest = new byte[16];
            System.arraycopy(decData, decData.length - 16, oldDigest, 0, 16);
            if (!dataEqual(digest, oldDigest)) {
                throw new Exception("Digest mismatched.");
            }

            int ts = bytesToInt(decData, decData.length - 16 - 4);
            int now = (int)(System.currentTimeMillis() / 1000L);
            if (now - ts > 60 || now - ts < 0) {
                throw new Exception("Obsolete data");
            }

            byte[] ret = new byte[decData.length - 16 - 4];
            System.arraycopy(decData, 0, ret, 0, decData.length - 16 - 4);
            return ret;
        } catch (Exception e) {
            throw new SecureSessionException("Decryption failed.");
        }
    }

    /**
     * Decrypt the session key encrypted by the other party using public key.
     */
    protected void decryptSessionKey(byte[] keyData) throws Exception {
        // Read session key file and dencrypt it.

        RSA rsa = new RSA();
        rsa.init(RSA.DECRYPT_MODE, privateKey);
        byte[] decSessionKey = rsa.doFinal(keyData, 0, keyData.length);
        if (decSessionKey.length != 44) {
            throw new Exception("Invalid session key.");
        }

        // Check MD5 digest of the session key.

        MD5 md = new MD5();
        md.update(decSessionKey, 0, 28);
        byte[] digest = md.digest();
        byte[] oldDigest = new byte[16];
        System.arraycopy(decSessionKey, 28, oldDigest, 0, 16);
        if (!dataEqual(digest, oldDigest)) {
            throw new Exception("Invalid session key.");
        }

        // Check timestamp of the session key.

        int ts = bytesToInt(decSessionKey, 24);
        int now = (int)(System.currentTimeMillis() / 1000);
        if (now - ts > 60 || now - ts < 0) {
            throw new Exception("Obsolete data.");
        }

        // Generate sesson key object.

        byte[] sessionKeyData = new byte[24];
        System.arraycopy(decSessionKey, 0, sessionKeyData, 0, 24);
        sessionKey = new RawSecretKey("RAW", sessionKeyData);
    }

    /**
     * Read all content of a file.
     */
    public static byte[] readFile(String fname) throws IOException {
        InputStream is = new RSA().getClass().getResourceAsStream(fname);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[256];
        int len;
        while ((len = is.read(buf)) >= 0) {
            if (len == 0) {
                continue;
            }
            bos.write(buf, 0, len);
        }
        is.close();
        return bos.toByteArray();
    }

    /**
     * Convert 4 bytes into a integer(little endian).
     */
    private int bytesToInt(byte[] buf, int offset) {
        byte t1 = buf[offset];
        byte t2 = buf[offset + 1];
        byte t3 = buf[offset + 2];
        byte t4 = buf[offset + 3];
        int ts = ((int)t1 & 0xFF) + (((int)t2 & 0xFF) << 8) +
            (((int)t3 & 0xFF) << 16) + (((int)t4 & 0xFF) << 24);
        return ts;
    }

    /**
     * Convert a integer into 4 bytes(little endian).
     */
    private void intToBytes(int value, byte[] buf, int offset) {
        buf[offset] = (byte)(value & 0xFF);
        buf[offset + 1] = (byte)((value >> 8) & 0xFF);
        buf[offset + 2] = (byte)((value >> 16) & 0xFF);
        buf[offset + 3] = (byte)((value >> 24) & 0xFF);
    }

    /**
     * Generate RSA key pair and save them into files.
     * @param length The key length.
     * @param pubkf The file name of public key file.
     * @param prikf The file name of private key file.
     * @throws SecureSessionException If fail to generate key pair.
     */
    public static void generateKeyPair(int length, String pubkf, String prikf)
        throws SecureSessionException {
        try {
            RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();
            kpg.initialize(length);
            KeyPair kp = kpg.generateKeyPair();
            RSAPrivateKey prik = kp.getPrivateKey();
            RSAPublicKey pubk = kp.getPublicKey();

            /*FileOutputStream fos = new FileOutputStream(pubkf);
            fos.write(pubk.getEncoded());
            fos.close();

            fos = new FileOutputStream(prikf);
            fos.write(prik.getEncoded());
            fos.close();*/
        } catch (Exception e) {
            throw new SecureSessionException("Generating key pair failed.");
        }
    }

    public static boolean dataEqual(byte[] data1, byte[] data2) {
        if (data1.length != data2.length) {
            return false;
        }
        for (int i = 0; i < data1.length; i++) {
            if (data1[i] != data2[i]) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        try {
            System.out.println(System.currentTimeMillis());
            generateKeyPair(512, "c:/public.key", "c:/private.key");
            System.out.println(System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
