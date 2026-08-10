package pip;


public class Account{
    private String name;
    private String password;
    private boolean online;
    
    public Account(){
        name = null;
        password = null;
        online = false;
    }
    
    public Account(String name, String password){
        this.name = name;
        this.password = password;
        online = false;
    }
    
    public synchronized void login(){
        this.online = true;
    }
    
    public synchronized void logout(){
        this.online = false;
    }
    
    public synchronized boolean online(){
        return online;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public void setPassword(String password){
        this.password = password;
    }

    public String getName(){
        return name;
    }
    
    public String getPassword(){
        return password;
    }

    public boolean equals(Object obj){
        if(obj instanceof Account){
            return name.equals(((Account)obj).getName());
        }
        
        return false;
    }
    
    public String toString(){
        return name + " , " + password;
    }
}
