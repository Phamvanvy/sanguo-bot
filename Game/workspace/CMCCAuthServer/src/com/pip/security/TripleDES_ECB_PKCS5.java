package com.pip.security;

public class TripleDES_ECB_PKCS5 {
    public static final int ENCRYPT_MODE = 1;
    public static final int DECRYPT_MODE = 2;

    /** Our blocksize */
    private final int BLOCK_SIZE = 8;

    /** Our key. We need it when we reset ourselves. */
    private RawSecretKey key;

    /**
     * Construct a new BlockCipher (CipherSpi) with an zero-length name ""
     * and given block size.
     */
    public TripleDES_ECB_PKCS5() throws Exception {
        buf = new byte[BLOCK_SIZE];
    }

    /**
     * Get block size.
     */
    public int getBlockSize() {
        return BLOCK_SIZE;
    }

    /**
     * Returns the length in bytes that an output buffer would need to be in
     * order to hold the result of the next update or doFinal operation, given
     * the input length <code>inputLen</code> (in bytes).
     *
     * This call takes into account any unprocessed (buffered) data from a
     * previous update call(s), and padding.
     *
     * The actual output length of the next <code>update or doFinal</code> call
     * may be smaller than the length returned by this method. For ciphers with
     * a padding, calling the update method will generally return less data
     * than predicted by this function.
     *
     * @param  inputLen the length in bytes.
     *
     * @return the maximum amount of data that the cipher will return.
     */
    protected final int getOutputSize(int inputLen) {
        int padLen = inputLen + getPadSize(inputLen);
        return ((bufCount + padLen) / 8) * 8;
    }

    /**
     * Initialize this blockcipher for encryption or decryption.
     *
     * If the cipher requires randomness, it is taken from <code>random</code>.
     * Randomness is required for modes that use IVs and might be required for
     * some padding schemes.
     *
     * @param opmode Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE.
     * @param key    secret key
     * @param random source of randomness
     */
    public final void init(int opmode, RawSecretKey key) throws Exception {
        decrypt = ( opmode == DECRYPT_MODE );
        coreInit(key, decrypt);
    }

    /**
     * Implemented in terms of engineDoFinal(byte[], int, int, byte[], int)
     *
     * @throws BadPaddingException
     *         (decryption only) if padding is expected but not found at the
     *         end of the data.
     * @throws IllegalBlockSizeException
     *         if no padding is specified and the input data is not a multiple
     *         of the blocksize.
     */
    public final byte[] doFinal(byte[] input, int inputOffset, int inputLen)
        throws Exception
    {
        byte[] tmp  = new byte[this.getOutputSize(inputLen)];
        int i = this.doFinal(input, inputOffset, inputLen, tmp, 0);
        if (i != tmp.length)
        {
            byte [] t = new byte[i];
            System.arraycopy(tmp, 0, t, 0, i);
            tmp = t;
        }

        return tmp;
    }


// Static variables and constants
// ...................................................................

    private static final int
        KEY_LENGTH     =  24,
        ALT_KEY_LENGTH =  21,
        DES_KEY_LENGTH = 8;

    private DES_ECB_PKCS5 des1 = new DES_ECB_PKCS5(), des2 = new DES_ECB_PKCS5(),
        des3 = new DES_ECB_PKCS5();


// BPI methods
// ...................................................................

    public void coreInit(RawSecretKey key, boolean decrypt)
    throws Exception
    {
        byte[] userkey = key.getEncoded();
        if (userkey == null)
            throw new Exception("Null user key");

        int len = 0;

        if (userkey.length == KEY_LENGTH) {
            len = 8;
        } else if (userkey.length == ALT_KEY_LENGTH) {
            len = 7;
        } else {
            throw new Exception("Invalid user key length");
        }

        byte[] k = new byte[len];
        System.arraycopy(userkey, 0, k, 0, len);
        RawSecretKey sk = new RawSecretKey("DES", k);
        des1.coreInit(sk, decrypt);

        System.arraycopy(userkey, len, k, 0, len);
        sk = new RawSecretKey("DES", k);
        des2.coreInit(sk, !decrypt);

        System.arraycopy(userkey, len+len, k, 0, len);
        sk = new RawSecretKey("DES", k);
        des3.coreInit(sk, decrypt);

        if(decrypt) {
            DES_ECB_PKCS5 des = des1;
            des1 = des3;
            des3 = des;
        }
    }

    /**
     * Perform a DES encryption or decryption operation of a single block.
     */
    public void coreCrypt(byte[] in, int inOffset, byte[] out, int outOffset)
    {
        des1.coreCrypt(in,  inOffset,  out, outOffset);
        des2.coreCrypt(out, outOffset, out, outOffset);
        des3.coreCrypt(out, outOffset, out, outOffset);
    }





    /** Decrypting? */
    protected boolean decrypt;

    /** How many bytes the buffer holds */
    protected int bufCount;

    /** buffers incomplete blocks */
    private final byte[] buf; // we count the buffer with bufCount from Mode.java

    final int getBufSize() {
        return bufCount;
    }

    int update(byte[] input, int inputOffset, int inputLen,
               byte[] output, int outputOffset)
    {
        // Invariant: bufCount < CIPHER_BLOCK_SIZE bytes

        int ret = 0;
        int remainder;
        while(inputLen >= (remainder = 8 - bufCount)) {
            System.arraycopy(input, inputOffset, buf, bufCount, remainder);
            coreCrypt(buf, 0, output, outputOffset);
            inputLen     -= remainder;
            inputOffset  += remainder;
            outputOffset += 8;
            ret          += 8;
            bufCount      = 0;
        }

        // Invariant: bufCount < CIPHER_BLOCK_SIZE bytes

        System.arraycopy(input, inputOffset, buf, bufCount, inputLen);
        bufCount += inputLen;

        // Invariant: bufCount < CIPHER_BLOCK_SIZE bytes

        return ret;
    }










    private byte[] scratchBuf = new byte[BLOCK_SIZE];
    private int blSize = BLOCK_SIZE;
    private boolean isBuffered = false;


    /**
     * @throws BadPaddingException
     *         If the padding data is corrupt or not found (decrypt only).
     * @throws IllegalBlockSizeException
     *         If no padding is specified *and* the input data was not a
     *         multiple of the Cipher's blocksize.
     * @throws ShortBufferException
     *         If output is too short to hold the result.
     */
    final int doFinal(byte[] input, int inputOffset, int inputLen,
                      byte[] output, int outputOffset) throws Exception
    {
        if (output.length < this.getOutputSize(inputLen))
            throw new Exception("The output buffer is too short");
        byte [] t;
        if (decrypt) {
           if (input == null && !isBuffered) return 0;
           if (input != null && inputLen < this.getPadSize(inputLen))
              throw new Exception("Input data not bounded by the "+
                                            "padding size");
           int i = 0;
           if (isBuffered) {
               i = update(scratchBuf, 0, blSize,
                               output, outputOffset);
               if (input != null)
                   i += update(input, inputOffset,
                                    inputLen, output, outputOffset + blSize);
           } else {
               i = update(input, inputOffset, inputLen,
                               output, outputOffset);
           }
           isBuffered = false;
           return coreUnPad(output,i);
        }
        t = this.corePad(input, inputLen);
        return update(t, inputOffset, t.length, output, outputOffset);
    }

   final byte [] corePad(byte [] input, int inputLen)
      throws Exception {
        if (input == null) input = new byte[0];
        int pad = getPadSize(inputLen);
        byte [] b = new byte[pad + inputLen];
        System.arraycopy(input, 0, b, 0, inputLen);
        for (int i = 0; i<pad; i++)
           b[inputLen + i] = (byte) pad;

        return b;
   }

   final int coreUnPad(byte [] input, int inputLen) {
       return inputLen - ((int) input[inputLen - 1]);
   }

   final int getPadSize(int inputLen) {
        int bs = getBlockSize();
        return bs - (inputLen + getBufSize())%bs;
   }
}