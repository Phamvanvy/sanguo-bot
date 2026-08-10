/* $Id: RawKeyGenerator.java,v 1.1 2007/06/07 12:53:19 jlin Exp $
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


/*import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;*/


/**
 *
 * @version $Revision: 1.1 $
 * @author  Jeroen C. van Gelderen (gelderen@cryptix.org)
 */
abstract class RawKeyGenerator /*extends KeyGeneratorSpi*/
{
    private final String algorithm;
    private final int defaultKeySize;

    private SecureRandom random = null;
    private int keySize         = 0;


    protected RawKeyGenerator(String algorithm, int defaultKeySize)
    {
        this.algorithm      = algorithm;
        this.defaultKeySize = defaultKeySize;
    }


    public void engineInit(SecureRandom random)
    {
        this.random  = random;
        this.keySize = this.defaultKeySize;
    }


    public void engineInit(byte[] params, SecureRandom random) throws Exception
    {
        throw new Exception(
            "No AlgorithmParameterSpec supported.");
    }


    public void engineInit(int keysize, SecureRandom random) throws Exception
    {
        if(!isValidSize(keysize))
            throw new Exception(
                "Key size not supported [" + keysize + "]");

        this.random  = random;
        this.keySize = keysize;
    }



    public RawSecretKey engineGenerateKey()
    {
        if(random==null)
            random = new SecureRandom();

        byte[] keyBytes = new byte[(strengthToBits(this.keySize)+7)/8];
        do {
            random.nextBytes(keyBytes);
            keyBytes = fixUp(keyBytes);
        } while( isWeak(keyBytes) );

        return new RawSecretKey(this.algorithm, keyBytes);
    }


// Overridables and abstract methods
//............................................................................

    /**
     * Translates strength (complexity, 56 for DES) to bit length
     * (64 for DES).
     */
    protected int strengthToBits(int strength) {
        return strength;
    }


    /**
     * Fix up the generated and purely random bytes. For DES this would
     * fix the parity.
     */
    protected byte[] fixUp( byte[] key ) {
        return key;
    }


    /**
     * Returns true if the given key is weak.
     */
    protected abstract boolean isWeak(byte[] keyBytes);


    /**
     * Is the given keysize valid for this algorithm?
     */
    protected abstract boolean isValidSize(int size);
}
