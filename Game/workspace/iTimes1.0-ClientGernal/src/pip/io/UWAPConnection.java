package pip.io;


public interface UWAPConnection{
    public int writeSegment(UWAPSegment segment);
    public void close();
    public void start();
    public void cut(boolean cut);
    public void cycleSegmentsDoingQueue();
}