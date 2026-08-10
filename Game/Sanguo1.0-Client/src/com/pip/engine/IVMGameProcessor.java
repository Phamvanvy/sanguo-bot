package com.pip.engine;


public interface IVMGameProcessor{
    public Object readGameData(String dataName);

    public void saveGameData(String dataName, Object data);

    public void removeGameData(String dataName);
}
