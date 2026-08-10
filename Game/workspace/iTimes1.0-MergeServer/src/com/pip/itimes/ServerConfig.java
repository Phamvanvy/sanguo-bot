package com.pip.itimes;

import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

public class ServerConfig{
    private static final Logger log = Logger.getLogger(ServerConfig.class);

    public static final int SERVER_TYPE_MAIN = 0;
    public static final int SERVER_TYPE_ASSISTANT = 1;

    private int type;
    private String name;
    private String connectAddress;
    private String connectUser;
    private String connectPassword;

    private Connection connection;

    public ServerConfig(SubnodeConfiguration config) throws Exception{
        String tmp = config.getString("type");
        if(tmp.equals("main")){
            type = SERVER_TYPE_MAIN;
        }else{
            type = SERVER_TYPE_ASSISTANT;
        }

        name = config.getString("name");
        connectAddress = config.getString("dbConnect");
        connectUser = config.getString("dbUser");
        connectPassword = config.getString("dbPassword");
    }

    public Connection getConnection(){
        if(connection == null){
            try{
                Class.forName("com.mysql.jdbc.Driver");
                Class.forName("com.mysql.jdbc.Driver").newInstance();

                connection = DriverManager.getConnection(getConnectAddress(), getConnectUser(), getConnectPassword());
                connection.setAutoCommit(false);
                log.info("JDBC Connect To " + getName() + " Created Successful");

                return connection;
            }catch(Exception e){
                log.error("JDBC Connect To " + getName() + " Created error", e);
            }
        }

        return connection;
    }

    public int getType(){
        return type;
    }

    public String getName(){
        return name;
    }

    public String getConnectAddress(){
        return connectAddress;
    }

    public String getConnectUser(){
        return connectUser;
    }

    public String getConnectPassword(){
        return connectPassword;
    }

    public void close(){
        try{
            connection.close();
        }catch(Exception e){
        }
    }
}
