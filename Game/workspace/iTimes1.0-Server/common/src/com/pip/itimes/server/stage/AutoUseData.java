package com.pip.itimes.server.stage;


public class AutoUseData{
    private int id;
    private String message;
    private String command;
    private int taskId;
    private String[] commands;
    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getMessage(){
        return message;
    }
    
    public String getCommand(){
        return command;
    }

    public void setMessage(String message){
        this.message = message;
    }
    
    public void setCommand(String command){
        this.command = command;
    }

    public int getTaskId(){
        return taskId;
    }

    public void setTaskId(int taskId){
        this.taskId = taskId;
    }
    
    public String[] getCommands() {
		return commands;
	}

	public void setCommands(String[] commands) {
		this.commands = commands;
	}

	public String[] getTaskStrings(){
        if(message.trim().length() > 0){
        	if (message.equalsIgnoreCase("31048")){
        		
        		String[] results = new String[commands.length + 1];
        		results[0] = String.valueOf(commands.length);
        		for(int i = 1; i < commands.length+1; i++){
        			results[i] = commands[i-1];
        		}
        		return results;
        	}else{
        		return new String[]{
                        message, command
        		};
        	}
        }else{
            return new String[]{
                            command
            };
        }
    }
}
