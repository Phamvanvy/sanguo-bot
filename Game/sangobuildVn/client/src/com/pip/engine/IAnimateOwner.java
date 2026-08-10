package com.pip.engine;


import com.pip.image.PipAnimateSet;


public interface IAnimateOwner{
    public byte getType();

    public int getId();

    public void animateReady(String animateName, PipAnimateSet animate);
}