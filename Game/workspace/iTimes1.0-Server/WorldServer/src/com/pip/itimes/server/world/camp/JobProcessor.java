package com.pip.itimes.server.world.camp;

public interface JobProcessor{
    public void processStart(int id, long time);
    public void processEnd(int id, long time);
}
