/* $Id: RawSecretKey.java,v 1.1 2008/11/09 16:31:22 lighthu Exp $
 *
 * Copyright (C) 1995-1999 The Cryptix Foundation Limited.
 * All rights reserved.
 *
 * Use, modification, copying and distribution of this software is subject
 * the terms and conditions of the Cryptix General Licence. You should have
 * received a copy of the Cryptix General Licence along with this library;
 * if not, you can download a copy from http://www.cryptix.org/ .
 */
package com.pip.security;



/**
 * FIXME: make package protected. fix tests first.
 *
 * @version $Revision: 1.1 $
 * @author  Jeroen C. van Gelderen <gelderen@cryptix.org>
 */
public class RawSecretKey
{
    private final String algorithm;
    private final byte[] keyBytes;


    // FIXME: make protected
    public RawSecretKey(String algorithm, byte[] keyBytes)
    {
        this.algorithm = algorithm;
        this.keyBytes = new byte[keyBytes.length];
        System.arraycopy(keyBytes, 0, this.keyBytes, 0, keyBytes.length);
    }


    public String getAlgorithm()
    {
        return algorithm;
    }


    public String getFormat()
    {
        return "RAW";
    }


    public byte[] getEncoded()
    {
        byte[] ret = new byte[keyBytes.length];
        System.arraycopy(keyBytes, 0, ret, 0, keyBytes.length);
        return ret;
    }
}
