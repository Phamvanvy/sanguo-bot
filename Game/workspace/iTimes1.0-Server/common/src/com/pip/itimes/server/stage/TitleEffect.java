package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TitleEffect extends Effect {

    private String title;

    public TitleEffect() {
    }

    public byte getType() {
        return 7;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getTitle(){
        return title;
    }
}
