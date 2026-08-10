package com.pip.security;

public class MD5 {
    private static final int DEFAULT_BLOCKSIZE = 64;

    /** Size (in bytes) of the blocks. */
    private final int blockSize = DEFAULT_BLOCKSIZE;

    /** Size (in bytes) of the digest */
    private final int hashSize = 16;

    /** 64 byte buffer */
    private final byte[] buf = new byte[blockSize];


    /** Buffer offset */
    private int bufOff = 0;

    /** Number of bytes hashed 'till now. */
    private long byteCount = 0;

    /** Mode */
    private final int mode = MODE_MD;


    /*package*/ static final int
        MODE_MD    = 0,
        MODE_SHA   = 1,
        MODE_TIGER = 2;


// Constructors
//...........................................................................

    /**
     * Construct a 64 or 128-byte PaddingMD in MD-like, SHA-like or Tiger-like
     * padding mode.
     *
     * @param hashSize  Length of the hash in bytes.
     */
    public MD5() {
        coreReset();
    }

// Implementation
//...........................................................................

    public int getDigestLength() {
        return this.hashSize;
    }


    public void update(byte input) {
        //#ASSERT(this.bufOff < blockSize);

        byteCount += 1;
        buf[bufOff++] = input;
        if( bufOff==blockSize ) {
            coreUpdate(buf, 0);
            bufOff = 0;
        }

        //#ASSERT(this.bufOff < blockSize);
    }


    public void update(byte[] input, int offset, int length) {
        byteCount += length;

        //#ASSERT(this.bufOff < blockSize);

        int todo;
        while( length >= (todo = blockSize - this.bufOff) ) {
            System.arraycopy(input, offset, this.buf, this.bufOff, todo);
            coreUpdate(this.buf, 0);
            length -= todo;
            offset += todo;
            this.bufOff = 0;
        }

        //#ASSERT(this.bufOff < blockSize);

        System.arraycopy(input, offset, this.buf, this.bufOff, length);
        bufOff += length;
    }


    public byte[] digest() {
        byte[] tmp = new byte[hashSize];
        privateDigest(tmp, 0, hashSize);
        return tmp;
    }


    public int digest(byte[] buf, int offset, int len)
    throws Exception
    {
        if(len<hashSize)
            throw new Exception();

        return privateDigest(buf, offset, len);
    }


    /**
     * Same as protected int engineDigest(byte[] buf, int offset, int len)
     * except that we don't validate arguments.
     */
    private int privateDigest(byte[] buf, int offset, int len)
    {
        //#ASSERT(this.bufOff < blockSize);

        this.buf[this.bufOff++] = (mode==MODE_TIGER) ? (byte)0x01 : (byte)0x80;

        int lenOfBitLen = (blockSize==128) ? 16 : 8;
        int C = blockSize - lenOfBitLen;
        if(this.bufOff > C) {
            while(this.bufOff < blockSize)
                this.buf[this.bufOff++] = (byte)0x00;

            coreUpdate(this.buf, 0);
            this.bufOff = 0;
        }

        while(this.bufOff < C)
            this.buf[this.bufOff++] = (byte)0x00;

        long bitCount = byteCount * 8;
        if(blockSize==128)
            for(int i=0; i<8; i++)
                this.buf[this.bufOff++] = 0x00;

        if(mode==MODE_SHA) {
            // 64-bit length is appended in big endian order
            for(int i=56; i>=0; i-=8)
                this.buf[this.bufOff++] = (byte)(bitCount >>> (i) );
        } else {
            // 64-bit length is appended in little endian order
            for(int i=0; i<64; i+=8)
                this.buf[this.bufOff++] = (byte)(bitCount >>> (i) );
        }

        coreUpdate(this.buf, 0);
        coreDigest(buf, offset);

        reset();
        return hashSize;
    }


    public void reset() {
        this.bufOff    = 0;
        this.byteCount = 0;
        coreReset();
    }


// Instance variables
//...........................................................................

    /** 4 32-bit words (interim result) */
    private int[] context = new int[4];

    /** 512 bits work buffer = 16 x 32-bit words */
    private int[] X = new int[16];

    protected void coreDigest(byte[] buf, int off)
    {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                buf[off+(i * 4 + j)] = (byte)(context[i] >>> (8 * j));
    }


    protected void coreReset()
    {
        // initial values of MD5 i.e. A, B, C, D
        context[0] = 0x67452301;
        context[1] = 0xEFCDAB89;
        context[2] = 0x98BADCFE;
        context[3] = 0x10325476;
    }


    protected void coreUpdate(byte[] block, int offset)
    {
        // encodes 64 bytes from input block into an array of
        // 16 32-bit entities.
        for (int i = 0; i < 16; i++)
            X[i] = (block[offset++] & 0xFF)       |
                   (block[offset++] & 0xFF) <<  8 |
                   (block[offset++] & 0xFF) << 16 |
                   (block[offset++] & 0xFF) << 24;

        int a = context[0];
        int b = context[1];
        int c = context[2];
        int d = context[3];

        a = FF(a,b,c,d,X[ 0], 7,0xd76aa478);
        d = FF(d,a,b,c,X[ 1],12,0xe8c7b756);
        c = FF(c,d,a,b,X[ 2],17,0x242070db);
        b = FF(b,c,d,a,X[ 3],22,0xc1bdceee);
        a = FF(a,b,c,d,X[ 4], 7,0xf57c0faf);
        d = FF(d,a,b,c,X[ 5],12,0x4787c62a);
        c = FF(c,d,a,b,X[ 6],17,0xa8304613);
        b = FF(b,c,d,a,X[ 7],22,0xfd469501);
        a = FF(a,b,c,d,X[ 8], 7,0x698098d8);
        d = FF(d,a,b,c,X[ 9],12,0x8b44f7af);
        c = FF(c,d,a,b,X[10],17,0xffff5bb1);
        b = FF(b,c,d,a,X[11],22,0x895cd7be);
        a = FF(a,b,c,d,X[12], 7,0x6b901122);
        d = FF(d,a,b,c,X[13],12,0xfd987193);
        c = FF(c,d,a,b,X[14],17,0xa679438e);
        b = FF(b,c,d,a,X[15],22,0x49b40821);

        a = GG(a,b,c,d,X[ 1], 5,0xf61e2562);
        d = GG(d,a,b,c,X[ 6], 9,0xc040b340);
        c = GG(c,d,a,b,X[11],14,0x265e5a51);
        b = GG(b,c,d,a,X[ 0],20,0xe9b6c7aa);
        a = GG(a,b,c,d,X[ 5], 5,0xd62f105d);
        d = GG(d,a,b,c,X[10], 9,0x02441453);
        c = GG(c,d,a,b,X[15],14,0xd8a1e681);
        b = GG(b,c,d,a,X[ 4],20,0xe7d3fbc8);
        a = GG(a,b,c,d,X[ 9], 5,0x21e1cde6);
        d = GG(d,a,b,c,X[14], 9,0xc33707d6);
        c = GG(c,d,a,b,X[ 3],14,0xf4d50d87);
        b = GG(b,c,d,a,X[ 8],20,0x455a14ed);
        a = GG(a,b,c,d,X[13], 5,0xa9e3e905);
        d = GG(d,a,b,c,X[ 2], 9,0xfcefa3f8);
        c = GG(c,d,a,b,X[ 7],14,0x676f02d9);
        b = GG(b,c,d,a,X[12],20,0x8d2a4c8a);

        a = HH(a,b,c,d,X[ 5], 4,0xfffa3942);
        d = HH(d,a,b,c,X[ 8],11,0x8771f681);
        c = HH(c,d,a,b,X[11],16,0x6d9d6122);
        b = HH(b,c,d,a,X[14],23,0xfde5380c);
        a = HH(a,b,c,d,X[ 1], 4,0xa4beea44);
        d = HH(d,a,b,c,X[ 4],11,0x4bdecfa9);
        c = HH(c,d,a,b,X[ 7],16,0xf6bb4b60);
        b = HH(b,c,d,a,X[10],23,0xbebfbc70);
        a = HH(a,b,c,d,X[13], 4,0x289b7ec6);
        d = HH(d,a,b,c,X[ 0],11,0xeaa127fa);
        c = HH(c,d,a,b,X[ 3],16,0xd4ef3085);
        b = HH(b,c,d,a,X[ 6],23,0x04881d05);
        a = HH(a,b,c,d,X[ 9], 4,0xd9d4d039);
        d = HH(d,a,b,c,X[12],11,0xe6db99e5);
        c = HH(c,d,a,b,X[15],16,0x1fa27cf8);
        b = HH(b,c,d,a,X[ 2],23,0xc4ac5665);

        a = II(a,b,c,d,X[ 0], 6,0xf4292244);
        d = II(d,a,b,c,X[ 7],10,0x432aff97);
        c = II(c,d,a,b,X[14],15,0xab9423a7);
        b = II(b,c,d,a,X[ 5],21,0xfc93a039);
        a = II(a,b,c,d,X[12], 6,0x655b59c3);
        d = II(d,a,b,c,X[ 3],10,0x8f0ccc92);
        c = II(c,d,a,b,X[10],15,0xffeff47d);
        b = II(b,c,d,a,X[ 1],21,0x85845dd1);
        a = II(a,b,c,d,X[ 8], 6,0x6fa87e4f);
        d = II(d,a,b,c,X[15],10,0xfe2ce6e0);
        c = II(c,d,a,b,X[ 6],15,0xa3014314);
        b = II(b,c,d,a,X[13],21,0x4e0811a1);
        a = II(a,b,c,d,X[ 4], 6,0xf7537e82);
        d = II(d,a,b,c,X[11],10,0xbd3af235);
        c = II(c,d,a,b,X[ 2],15,0x2ad7d2bb);
        b = II(b,c,d,a,X[ 9],21,0xeb86d391);

        context[0] += a;
        context[1] += b;
        context[2] += c;
        context[3] += d;
    }


// Helpers
// ..........................................................................

    private static int F(int x,int y,int z) { return (z ^ (x & (y^z))); }
    private static int G(int x,int y,int z) { return (y ^ (z & (x^y))); }
    private static int H(int x,int y,int z) { return (x ^ y ^ z);       }
    private static int I(int x,int y,int z) { return (y  ^  (x | ~z));  }

    private static int FF(int a,int b,int c,int d,int k,int s,int t)
    {
        a += k+t+F(b,c,d);
        a = (a << s | a >>> -s);
        return a+b;
    }

    private static int GG(int a,int b,int c,int d,int k,int s,int t)
    {
        a += k+t+G(b,c,d);
        a = (a << s | a >>> -s);
        return a+b;
    }

    private static int HH(int a,int b,int c,int d,int k,int s,int t)
    {
        a += k+t+H(b,c,d);
        a = (a << s | a >>> -s);
        return a+b;
    }

    private int II(int a,int b,int c,int d,int k,int s,int t)
    {
        a += k+t+I(b,c,d);
        a = (a << s | a >>> -s);
        return a+b;
    }
}
