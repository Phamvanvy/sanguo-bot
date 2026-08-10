package com.pip.itimes.server.stage;

public class SportNpcType extends TaskNpcType {

    private String[] choices;
    private String[] commands;

    private String question;

    public SportNpcType(int id, String name, int type) {
        super(id, name, type);
    }



    public void setChoices(String[] choices){
        this.choices = choices;
    }

    public void setCommands(String[] commands){
        this.commands = commands;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String[] getChoices(){
        return choices;
    }

    public String[] getCommands(){
        return commands;
    }

    public String getQuestion() {
        return question;
    }
}
