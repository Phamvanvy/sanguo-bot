package com.pip.itimes.server.stage;

public class SecondGenerationPetEffect extends Effect{
	private byte mainPerceptionLevel;
    private byte secondPerceptionLevel;
    private byte setPerceptionLevel;

    public SecondGenerationPetEffect (byte mainPerceptionLevel, byte secondPerceptionLevel, byte setPerceptionLevel) {
        this.mainPerceptionLevel = mainPerceptionLevel;
        this.secondPerceptionLevel = secondPerceptionLevel;
        this.setPerceptionLevel = setPerceptionLevel;
    }
    
    public byte getType () {
        return 74;
    }

    public byte getMainPerceptionLevel () {
        return mainPerceptionLevel;
    }

    public byte getSecondPerceptionLevel () {
        return secondPerceptionLevel;
    }
    
    public byte getSetPerceptionLevel(){
    	return setPerceptionLevel;
    }
}
