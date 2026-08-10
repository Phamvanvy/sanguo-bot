// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SignEnc.java

package com.umpay;

import java.io.*;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.MissingResourceException;
import java.util.*;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

// Referenced classes of package com.umpay:
//            SignEncException

public class SignEnc
{

    public SignEnc()
    {
    }

    public static String sign(String dataString)
        throws SignEncException
    {
        String merPriKeyPath;
        FileInputStream fis;
        byte kb[];
        Properties rb = null;
        merPriKeyPath = null;
        String signString = null;
        fis = null;
        if(dataString == null)
            throw new SignEncException("the data string to be signed cannot be null");
        FileInputStream fis1 = null;
        try
        {
        	rb = new Properties();
            fis1 = new FileInputStream("SignVerProp.properties");
            rb.load(fis1);
        } catch (Exception e) {
        	throw new SignEncException("cannot find the SignVerProp.properties");
        } finally {
        	if (fis1 != null) {
        		try {
        			fis1.close();
        		} catch (Exception e) {
        		}
        		fis1 = null;
        	}
        }
        try
        {
            merPriKeyPath = rb.getProperty("mer.prikey.path");
        }
        catch(MissingResourceException mre)
        {
            throw new SignEncException("cannot find the value of mer.prikey.path in property file");
        }
        if(merPriKeyPath == null || merPriKeyPath.trim().length() == 0)
            throw new SignEncException("cannot find the value of mer.prikey.path in property file");
        kb = (byte[])null;
        try
        {
            File f = new File(merPriKeyPath);
            kb = new byte[(int)f.length()];
            fis = new FileInputStream(f);
            fis.read(kb);
        }
        catch(Exception e)
        {
            throw new SignEncException("load the primary key failed");
        } finally {
        	if (fis != null) {
        		try {
        			fis.close();
        		} catch (Exception e) {
        		}
        		fis = null;
        	}
        }
        PKCS8EncodedKeySpec peks = null;
        KeyFactory kf = null;
        java.security.PrivateKey pk = null;
        try
        {
            peks = new PKCS8EncodedKeySpec(kb);
            kf = KeyFactory.getInstance("RSA");
            pk = kf.generatePrivate(peks);
        }
        catch(Exception e)
        {
            throw new SignEncException("invalid primary key format");
        }
        Signature sig = null;
        byte sb[] = (byte[])null;
        try
        {
            sig = Signature.getInstance("SHA1withRSA");
            sig.initSign(pk);
            sig.update(dataString.getBytes("gb2312"));
            sb = sig.sign();
        }
        catch(Exception e)
        {
            throw new SignEncException("sign procedure failed");
        }
        String b64Str = null;
        try
        {
            BASE64Encoder base64 = new BASE64Encoder();
            b64Str = base64.encode(sb);
        }
        catch(Exception e)
        {
            throw new SignEncException("base64 generation failed");
        }
        try
        {
            BufferedReader br = new BufferedReader(new StringReader(b64Str));
            String tmpStr = "";
            String tmpStr1;
            for(tmpStr1 = ""; (tmpStr = br.readLine()) != null; tmpStr1 = tmpStr1 + tmpStr);
            b64Str = tmpStr1;
            return b64Str;
        }
        catch(Exception e)
        {
            throw new SignEncException("base64 generation failed");
        }
    }

    public static boolean verify(String dataString, String signString)
        throws SignEncException
    {
        String platCertPath;
        FileInputStream fis;
        byte cb[];
        if(dataString == null)
            throw new SignEncException("the data string to be signed cannot be null");
        Properties rb = null;
        FileInputStream fis1 = null;
        try
        {
            rb = new Properties();
            fis1 = new FileInputStream("SignVerProp.properties");
            rb.load(fis1);
        } catch (Exception e) {
        	throw new SignEncException("cannot find the SignVerProp.properties");
        } finally {
        	if (fis1 != null) {
        		try {
        			fis1.close();
        		} catch (Exception e) {
        		}
        		fis1 = null;
        	}
        }
        try
        {
            platCertPath = rb.getProperty("plat.cert.path");
        }
        catch(MissingResourceException mre)
        {
            throw new SignEncException("cannot find the value of plat.cert.path in property file");
        }
        if(platCertPath == null || platCertPath.trim().length() == 0)
            throw new SignEncException("cannot find the value of plat.cert.path in property file");
        fis = null;
        cb = (byte[])null;
        try
        {
            File f = new File(platCertPath);
            cb = new byte[(int)f.length()];
            fis = new FileInputStream(f);
            fis.read(cb);
        }
        catch(Exception e)
        {
            throw new SignEncException("load the cert failed");
        } finally {
        	if (fis != null) {
        		try {
        			fis.close();
        		} catch (Exception e) {
        		}
        		fis = null;
        	}
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(cb);
        CertificateFactory cf = null;
        X509Certificate cert = null;
        try
        {
            cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate)cf.generateCertificate(bais);
        }
        catch(Exception e)
        {
            throw new SignEncException("load the cert failed");
        }
        try
        {
            BASE64Decoder base64 = new BASE64Decoder();
            byte signed[] = base64.decodeBuffer(signString);
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(cert);
            sig.update(dataString.getBytes());
            return sig.verify(signed);
        }
        catch(Exception e)
        {
            throw new SignEncException("verify procedure failed");
        }
    }
}
