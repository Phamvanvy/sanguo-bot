package com.pip.security;

public class DES_ECB_PKCS5 {
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
    public DES_ECB_PKCS5() throws Exception {
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
        ROUNDS         = 16,  // number of encryption/decryption rounds
        KEY_LENGTH     =  8,  // DES key length in bytes
        ALT_KEY_LENGTH =  7,  // Alternate DES key length in bytes
        INTERNAL_KEY_LENGTH = 2 * ROUNDS; // number of elements in key schedule


    /** Table for PC2 permutations in key schedule computation. */
    private static final int[] SKB = new int[8 * 64];


    /** Table for S-boxes and permutations, used in encrypt_base. */
    private static final int SP_TRANS[] = new int[8 * 64];


    /** Build the SKB and SP_TRANS tables */
    static
    {
        // build the SKB table
        // represent the bit number that each permutated bit is derived from
        // according to FIPS-46
        String cd =
            "D]PKESYM`UBJ\\@RXA`I[T`HC`LZQ"+"\\PB]TL`[C`JQ@Y`HSXDUIZRAM`EK";
        int j, s, bit;
        int count = 0;
        int offset = 0;
        for (int i = 0; i < cd.length(); i++)
        {
            s = cd.charAt(i) - '@';
            if (s != 32)
            {
                bit = 1 << count++;
                for (j = 0; j < 64; j++)
                    if ((bit & j) != 0) SKB[offset + j] |= 1 << s;
                if (count == 6)
                {
                    offset += 64;
                    count = 0;
                }
            }
        }


        // build the SP_TRANS table
        // I'd _really_ like to just say 'SP_TRANS = { ... }', but
        // that would be terribly inefficient (code size + time).
        // Instead we use a compressed representation --GK
        String spt =
            "g3H821:80:H03BA0@N1290BAA88::3112aIH8:8282@0@AH0:1W3A8P810@22;22"+
            "A18^@9H9@129:<8@822`?:@0@8PH2H81A19:G1@03403A0B1;:0@1g192:@919AA"+
            "0A109:W21492H@0051919811:215011139883942N8::3112A2:31981jM118::A"+
            "101@I88:1aN0<@030128:X;811`920:;H0310D1033@W980:8A4@804A3803o1A2"+
            "021B2:@1AH023GA:8:@81@@12092B:098042P@:0:A0HA9>1;289:@1804:40Ph="+
            "1:H0I0HP0408024bC9P8@I808A;@0@0PnH0::8:19J@818:@iF0398:8A9H0<13@"+
            "001@11<8;@82B01P0a2989B:0AY0912889bD0A1@B1A0A0AB033O91182440A9P8"+
            "@I80n@1I03@1J828212A`A8:12B1@19A9@9@8^B:0@H00<82AB030bB840821Q:8"+
            "310A302102::A1::20A1;8"; // OK, try to type _that_!
            // [526 chars, 3156 bits]
        // The theory is that each bit position in each int of SP_TRANS is
        // set in exactly 32 entries. We keep track of set bits.
        offset = 0;
        int k, c, param;
        for (int i = 0; i < 32; i++) // each bit position
        {
            k = -1; // pretend the -1th bit was set
            bit = 1 << i;
            for (j = 0; j < 32; j++) // each set bit
            {
                // Each character consists of two three-bit values:
                c = spt.charAt(offset >> 1) - '0' >> (offset & 1) * 3 & 7;
                offset++;
                if (c < 5)
                {
                    // values 0...4 indicate a set bit 1...5 positions
                    // from the previous set bit
                    k += c + 1;
                    SP_TRANS[k] |= bit;
                    continue;
                }
                // other values take at least an additional parameter:
                // the next value in the sequence.
                param = spt.charAt(offset >> 1) - '0' >> (offset & 1) * 3 & 7;
                offset++;
                if (c == 5)
                {
                    // indicates a bit set param+6 positions from
                    // the previous set bit
                    k += param + 6;
                    SP_TRANS[k] |= bit;
                }
                else if (c == 6)
                {
                    // indicates a bit set (param * 64) + 1 positions
                    // from the previous set bit
                    k += (param << 6) + 1;
                    SP_TRANS[k] |= bit;
                }
                else
                {
                    // indicates that we should skip (param * 64) positions,
                    // then process the next value which will be in the range
                    // 0...4.
                    k += param << 6;
                    j--;
                }
            }
        }
    }

// Instance variables
// ...................................................................

    /** The internal key schedule */
    private int[] sKey = new int[INTERNAL_KEY_LENGTH];


// BPI methods
// ...................................................................

    public void coreInit(RawSecretKey key, boolean decrypt)
    throws Exception
    {
        byte[] userkey = key.getEncoded();
        if (userkey == null)
            throw new Exception("Null user key");

        if (userkey.length == ALT_KEY_LENGTH) {

            byte[] temp = new byte[KEY_LENGTH];

            temp[0] = (byte)(                     userkey[0]                );
            temp[1] = (byte)( userkey[0] << 7  |  userkey[1] >>> 1  &  0x7f );
            temp[2] = (byte)( userkey[1] << 6  |  userkey[2] >>> 2  &  0x3f );
            temp[3] = (byte)( userkey[2] << 5  |  userkey[3] >>> 3  &  0x1f );
            temp[4] = (byte)( userkey[3] << 4  |  userkey[4] >>> 4  &  0x0f );
            temp[5] = (byte)( userkey[4] << 3  |  userkey[5] >>> 5  &  0x07 );
            temp[6] = (byte)( userkey[5] << 2  |  userkey[6] >>> 6  &  0x03 );
            temp[7] = (byte)( userkey[6] << 1                               );

            userkey = temp;
        }

        if (userkey.length != KEY_LENGTH)
            throw new Exception("Invalid user key length");

        int i = 0;
        int c = (userkey[i++] & 0xFF)       |
                (userkey[i++] & 0xFF) <<  8 |
                (userkey[i++] & 0xFF) << 16 |
                (userkey[i++]       ) << 24;
        int d = (userkey[i++] & 0xFF)       |
                (userkey[i++] & 0xFF) <<  8 |
                (userkey[i++] & 0xFF) << 16 |
                (userkey[i  ]       ) << 24;

        int t = ((d >>> 4) ^ c) & 0x0F0F0F0F;
        c ^= t;
        d ^= t << 4;
        t = ((c << 18) ^ c) & 0xCCCC0000;
        c ^= t ^ t >>> 18;
        t = ((d << 18) ^ d) & 0xCCCC0000;
        d ^= t ^ t >>> 18;
        t = ((d >>> 1) ^ c) & 0x55555555;
        c ^= t;
        d ^= t << 1;
        t = ((c >>> 8) ^ d) & 0x00FF00FF;
        d ^= t;
        c ^= t << 8;
        t = ((d >>> 1) ^ c) & 0x55555555;
        c ^= t;
        d ^= t << 1;

        d = (d & 0x000000FF) <<  16 |
            (d & 0x0000FF00)        |
            (d & 0x00FF0000) >>> 16 |
            (c & 0xF0000000) >>>  4;
        c &= 0x0FFFFFFF;

        int s;
        int j = 0;

        for (i = 0; i < ROUNDS; i++)
        {
            if ((0x7EFC >> i & 1) == 1)
            {
                c = (c >>> 2 | c << 26) & 0x0FFFFFFF;
                d = (d >>> 2 | d << 26) & 0x0FFFFFFF;
            }
            else
            {
                c = (c >>> 1 | c << 27) & 0x0FFFFFFF;
                d = (d >>> 1 | d << 27) & 0x0FFFFFFF;
            }
            s = SKB[           c         & 0x3F                        ] |
                SKB[0x040 | (((c >>>  6) & 0x03) | ((c >>>  7) & 0x3C))] |
                SKB[0x080 | (((c >>> 13) & 0x0F) | ((c >>> 14) & 0x30))] |
                SKB[0x0C0 | (((c >>> 20) & 0x01) | ((c >>> 21) & 0x06)
                                                 | ((c >>> 22) & 0x38))];
            t = SKB[0x100 | ( d         & 0x3F                      )] |
                SKB[0x140 | (((d >>>  7) & 0x03) | ((d >>>  8) & 0x3c))] |
                SKB[0x180 | ((d >>> 15) & 0x3F                      )] |
                SKB[0x1C0 | (((d >>> 21) & 0x0F) | ((d >>> 22) & 0x30))];

            sKey[j++] = t <<  16 | (s & 0x0000FFFF);
            s         = s >>> 16 | (t & 0xFFFF0000);
            sKey[j++] = s <<   4 |  s >>> 28;
        }


        // Reverse the subkeys if we're decrypting
        // Best illustrated by example: 1 2 3 4 5 6 7 8  ->  7 8 5 6 3 4 1 2
        if(decrypt)
        {
            for(i=0; i<16; i++)
            {
                j = 30 - i + ( i%2 * 2 );
                t = sKey[i];  sKey[i] = sKey[j];  sKey[j] = t;
            }
        }
    }



    /**
     * Perform a DES encryption or decryption operation of a single block.
     */
    public void coreCrypt(byte[] in, int inOffset, byte[] out, int outOffset)
    {
        int L = (in[inOffset++] & 0xFF)       |
                (in[inOffset++] & 0xFF) <<  8 |
                (in[inOffset++] & 0xFF) << 16 |
                (in[inOffset++]       ) << 24;
        int R = (in[inOffset++] & 0xFF)       |
                (in[inOffset++] & 0xFF) <<  8 |
                (in[inOffset++] & 0xFF) << 16 |
                (in[inOffset  ]       ) << 24;

        // Initial permutation
        int t = ((R >>> 4) ^ L) & 0x0F0F0F0F;
        L ^= t;
        R ^= t << 4;
        t = ((L >>> 16) ^ R) & 0x0000FFFF;
        R ^= t;
        L ^= t << 16;
        t = ((R >>> 2) ^ L) & 0x33333333;
        L ^= t;
        R ^= t << 2;
        t = ((L >>> 8) ^ R) & 0x00FF00FF;
        R ^= t;
        L ^= t << 8;
        t = ((R >>> 1) ^ L) & 0x55555555;
        L ^= t;
        R ^= t << 1;


        // look! we fit all four variables (plus the class itself)
        // into short byte-codes!
        int u = R << 1 | R >>> 31;
        R = L << 1 | L >>> 31;
        L = u;

        for (int i = 0; i < INTERNAL_KEY_LENGTH;)
        {
            u = R ^ sKey[i++];
            t = R ^ sKey[i++];
            t = t >>> 4 | t << 28;
            L ^= (SP_TRANS[0x040 | ( t         & 0x3F)] |
                  SP_TRANS[0x0C0 | ((t >>>  8) & 0x3F)] |
                  SP_TRANS[0x140 | ((t >>> 16) & 0x3F)] |
                  SP_TRANS[0x1C0 | ((t >>> 24) & 0x3F)] |
                  SP_TRANS[          u         & 0x3F ] |
                  SP_TRANS[0x080 | ((u >>>  8) & 0x3F)] |
                  SP_TRANS[0x100 | ((u >>> 16) & 0x3F)] |
                  SP_TRANS[0x180 | ((u >>> 24) & 0x3F)]);

            u = L ^ sKey[i++];
            t = L ^ sKey[i++];
            t = t >>> 4 | t << 28;
            R ^= (SP_TRANS[0x040 | ( t         & 0x3F)] |
                  SP_TRANS[0x0C0 | ((t >>>  8) & 0x3F)] |
                  SP_TRANS[0x140 | ((t >>> 16) & 0x3F)] |
                  SP_TRANS[0x1C0 | ((t >>> 24) & 0x3F)] |
                  SP_TRANS[          u         & 0x3F ] |
                  SP_TRANS[0x080 | ((u >>>  8) & 0x3F)] |
                  SP_TRANS[0x100 | ((u >>> 16) & 0x3F)] |
                  SP_TRANS[0x180 | ((u >>> 24) & 0x3F)]);
        }
        R = R >>> 1 | R << 31;
        L = L >>> 1 | L << 31;


        // Final permutation
        t = (R >>> 1 ^ L) & 0x55555555;
        L ^= t;
        R ^= t << 1;
        t = (L >>> 8 ^ R) & 0x00FF00FF;
        R ^= t;
        L ^= t << 8;
        t = (R >>> 2 ^ L) & 0x33333333;
        L ^= t;
        R ^= t << 2;
        t = (L >>> 16 ^ R) & 0x0000FFFF;
        R ^= t;
        L ^= t << 16;
        t = (R >>> 4 ^ L) & 0x0F0F0F0F;

        L ^= t;
        R ^= (t << 4);

        out[outOffset++] = (byte)(L      );
        out[outOffset++] = (byte)(L >>  8);
        out[outOffset++] = (byte)(L >> 16);
        out[outOffset++] = (byte)(L >> 24);
        out[outOffset++] = (byte)(R      );
        out[outOffset++] = (byte)(R >>  8);
        out[outOffset++] = (byte)(R >> 16);
        out[outOffset  ] = (byte)(R >> 24);
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
