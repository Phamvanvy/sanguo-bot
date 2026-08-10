package com.pip.security;

public final class RSA {
    public static final int ENCRYPT_MODE = 1;
    public static final int DECRYPT_MODE = 2;

    private BigInteger n, e;

    private boolean decrypt;

    public RSA() {}

    protected final int engineGetBlockSize() {
        return (n.bitLength()+7)/8;
    }

    protected final int engineGetOutputSize(int inputLen) {
        return (inputLen < this.engineGetBlockSize()+1) ?
            this.engineGetBlockSize() + 1: inputLen;
    }

    public final void init(int opmode, RSAPublicKey key) throws Exception {
        decrypt = (opmode == DECRYPT_MODE);
        n = key.getModulus();
        e = key.getPublicExponent();
    }

    public final void init(int opmode, RSAPrivateKey key) throws Exception {
        decrypt = (opmode == DECRYPT_MODE);
        n = key.getModulus();
        e = key.getPrivateExponent();
    }

    public final byte[] doFinal(byte[] input, int inputOffset, int inputLen)
            throws Exception {
        byte [] o = new byte[this.engineGetOutputSize(inputLen)];
        int ret = this.doFinal(input, inputOffset, inputLen, o, 0);
        if (ret == o.length)
            return o;

        // If the buffer returned is smaller than what we allocated first.
        byte [] r = new byte[ret];
        System.arraycopy(o, 0, r, 0, ret);
        return r;
    }


    public final int doFinal(byte[] input, int inputOffset, int inputLen,
                             byte[] output, int outputOffset) throws Exception {
        if (output.length < this.engineGetOutputSize(inputLen))
            throw new Exception("Output buffer too small!");

        byte[] blub = new byte[inputLen];
        System.arraycopy(input, inputOffset, blub, 0, inputLen);

        byte [] b;
        BigInteger bi, res;
        if (decrypt) {

            bi = new BigInteger(1, blub);
            if(bi.compareTo(n)!=-1)
                throw new RuntimeException("TT");
            //res = RSAAlgorithm.rsa(bi, n, e, p, q, u);
            res = RSAAlgorithm.rsa(bi, n, e);
            b = res.toByteArray();
            return unpad(b, b.length, 0,
                         output, outputOffset);
        } else {

            /* FIXME: Do so we choose right block type out of the keytype?
             * (pw)
             */
            bi = new BigInteger(1, pad(blub, blub.length, 0, 0x02));
            if(bi.compareTo(n)!=-1)
                throw new RuntimeException("TT");

            res = RSAAlgorithm.rsa(bi, this.n, this.e);
            if(res.compareTo(n)!=-1)
                throw new RuntimeException("TT");

            int blockSize = engineGetBlockSize();

            b = res.toByteArray();
            if( b.length-1 > blockSize )
                throw new RuntimeException("YY");

            if( b.length > blockSize ) {
                byte[] t = new byte[blockSize];
                System.arraycopy(b, 1, t, 0, blockSize);
                b = t;
            }

            for(int i=0; i<blockSize; i++)
                output[outputOffset+i] = 0x00;

            int bOff = blockSize - b.length;

            System.arraycopy(b, 0, output, outputOffset + bOff, b.length);
            return b.length + bOff;
        }
    }

    /*
     * Private methods below.
     *
     * This is PKCS1 padding as described in the PKCS1 v 1.5
     * standard section 8 from RSALabs:
     * EB = 00 || BT || PS || 00 || D.
     *
     * But since BigInteger actually removes any leading zero
     * the encrypted buffer will be without the first 00.
     *
     * Both pad and unpad assumes us to have check so that the
     * output buffer is of valid size.
     *
     * I have done so we may use both private and public keys
     * as input, ie BT may be either 0x00, 0x01 or 0x02. (pw)
     */
    private byte[] pad(byte [] input, int inputLen, int offset, int bt) throws Exception {
        int k = (n.bitLength() + 7)/8;
        if (inputLen > k-11)
            throw new Exception("Data too long for this modulus!");

        byte [] ed = new byte[k];
        int padLen = k - 3 - inputLen;
        ed[0] = ed[2 + padLen] = 0x00;

        switch (bt) {
          case 0x00:
            for (int i = 1; i < (2 + padLen); i++)
                ed[i] = 0x00;
            break;
          case 0x01:
            ed[1] = 0x01;
            for (int i = 2; i < (2 + padLen); i++)
                ed[i] = (byte)0xFF;
            break;
          case 0x02:
            ed[1] = 0x02;
            byte [] b = new byte[1];
            SecureRandom sr = new SecureRandom();
            for (int i = 2; i < (2 + padLen); i++) {
                b[0] = 0;
                while (b[0] == 0)
                    sr.nextBytes(b);
                ed[i] = b[0];
            }
            break;
          default:
            throw new Exception("Wrong block type!");
        }

        System.arraycopy(input, offset, ed, padLen + 3, inputLen);
        //byte[] temp = new byte[ed.length - 1];
        //System.arraycopy(ed, 1, temp, 0, ed.length - 1);
        //return temp;
        return ed;
    }

    private int unpad(byte [] input, int inputLen, int inOffset,
                      byte [] output, int outOffset) throws Exception {
        int bt = input[inOffset];

        int padLen = 1;
        switch (bt) {
          case 0x00:
            for (;; padLen++)
                if (input[inOffset + padLen + 1] != (byte)0x00) break;
            break;
          case 0x01:
          case 0x02:
            for (;; padLen++)
                if (input[inOffset + padLen] == (byte)0x00) break;
            break;
          default:
            throw new Exception("Wrong block type!");
        }
        padLen++;

        int len = inputLen - inOffset - padLen;
        System.arraycopy(input, inOffset + padLen, output, outOffset, len);
        return len;
    }
}
