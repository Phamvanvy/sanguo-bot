package com.pip.security;

public final class RSAKeyPairGenerator {
    private static final BigInteger
        ONE = BigInteger.valueOf(0x1),
        F4  = BigInteger.valueOf(0x10001L);

    private static final int
        KEYSIZE_MIN     =   384,
        KEYSIZE_DEFAULT =  3072,
        KEYSIZE_MAX     = 16384;


    private static final int CERTAINTY = 80;


// Instance variables
//...........................................................................

    /** Keysize. */
    private int keysize;


    private BigInteger publicExponent;


    private SecureRandom random;


    /** Initialized already? */
    private boolean initialized = false;


// Concreteness
//...........................................................................

    public void initialize(int keysize)
    {
        //ASSERT(random != null);

        if( (keysize < KEYSIZE_MIN) || (keysize > KEYSIZE_MAX) )
            throw new IllegalArgumentException(
                "keysize: invalid size (" + keysize + ")" );

        this.keysize        = keysize;
        this.random         = new SecureRandom();
        this.publicExponent = F4; // default to 4th Fermat Prime

        this.initialized = true;
    }

    public KeyPair generateKeyPair()
    {
        if( !this.initialized )
            initialize(); // defaults are evil but Sun wants 'em...

        int pLen = keysize / 2;
        int qLen = keysize - pLen;
        BigInteger d, e, n, p, q, pMinus1, qMinus1, phi;

        e = this.publicExponent;

        while(true)
        {
            try
            {
                do
                {
                    p = new BigInteger(pLen, CERTAINTY, this.random);
                    q = new BigInteger(qLen, CERTAINTY, this.random);
                    n = p.multiply(q);
                }
                while( (p.compareTo(q) == 0) || (n.bitLength() != keysize) );

                pMinus1 = p.subtract(ONE);
                qMinus1 = q.subtract(ONE);
                phi     = pMinus1.multiply(qMinus1);
                d       = e.modInverse(phi); // expect an exception
                break;                       // no exception thrown
            }
            catch(ArithmeticException ae)
            {
                // gcd(e * phi) != 1. Try again
            }
        }

		BigInteger primeExponentP = d.mod(pMinus1);
		BigInteger primeExponentQ = d.mod(qMinus1);
		BigInteger crtCoefficient = q.modInverse(p);

        BigInteger x = new BigInteger(pLen, this.random);
        BigInteger y = RSAAlgorithm.rsa(x,
                                        n,
                                        e);

        BigInteger z = RSAAlgorithm.rsa(y,
                                        n,
                                        d,
                                        p,
                                        q,
                                        primeExponentP,
                                        primeExponentQ,
                                        crtCoefficient);

        if( !z.equals(x) )
            throw new RuntimeException("RSA KeyPair doesn't work");

        RSAPrivateKey priv = new RSAPrivateKey(n, e, d);
        /*RSAPrivateCrtKeyCryptix priv = new RSAPrivateCrtKeyCryptix(
                                            n,
                                            e,
                                            d,
                                            p,
                                            q,
                                            primeExponentP,
                                            primeExponentQ,
                                            crtCoefficient);*/

        RSAPublicKey pub = new RSAPublicKey(n, e);

        return new KeyPair(pub, priv);
    }


// Private parts
//...........................................................................

    /** Initialize with default values */
    private void initialize()
    {
        this.initialize( KEYSIZE_DEFAULT );
    }
}
