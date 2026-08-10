package pip;


public class Random{

    public Random(){
        this(System.currentTimeMillis());
    }

    public Random(long seed){
        setSeed(seed);
    }

    public synchronized void setSeed(long seed){
        this.seed = (seed ^ 25214903917L) & 281474976710655L;
    }

    protected synchronized int next(int bits){
        long nextseed = seed * 25214903917L + 11L & 281474976710655L;
        seed = nextseed;
        return (int)(nextseed >>> 48 - bits);
    }

    public int nextInt(){
        return next(32);
    }

    public int nextInt(int n){
        if(n <= 0)
            throw new IllegalArgumentException("n must be positive");
        if((n & -n) == n)
            return (int)((long)n * (long)next(31) >> 31);
        int bits;
        int val;
        do{
            bits = next(31);
            val = bits % n;
        }while((bits - val) + (n - 1) < 0);
        return val;
    }

    public long nextLong(){
        return ((long)next(32) << 32) + (long)next(32);
    }

    private long seed;
    //    private static final long multiplier = 25214903917L;
    //    private static final long addend = 11L;
    //    private static final long mask = 281474976710655L;
    //    private static final int BITS_PER_BYTE = 8;
    //    private static final int BYTES_PER_INT = 4;
}