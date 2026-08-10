package com.pip.itimes.server.stage;

public class InvestigationEffect extends Effect {

    private int scriptId;
    private String[] parameters;

    public InvestigationEffect() {
        super();
    }


    public byte getType() {
        return 28;
    }

    public int getScriptId() {
        return scriptId;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    public void setScriptId(int scriptId) {
        this.scriptId = scriptId;
    }

    public String[] getParameters() {
        return parameters;
    }
}
